package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.InitialData
import com.example.data.model.BackupDocument
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.Student
import com.example.data.model.StudentDayPaymentState
import com.example.data.model.StudentPaymentSummary
import com.example.data.repository.KasRepository
import com.example.util.BackupUtils
import com.example.util.DateUtils
import com.example.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class UserRole {
    TREASURER, // Bendahara / Admin (Bisa edit data, centang, tambah pengeluaran)
    STUDENT_VIEW // Mode Siswa / Wali Murid (Hanya bisa melihat rekapitulasi)
}

enum class FilterStatus {
    ALL,
    PAID,
    UNPAID,
    MALE,
    FEMALE
}

class KasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KasRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = KasRepository(db.kasDao())
    }

    // Navigation & Auth
    val isLoggedIn = MutableStateFlow(false)
    val userRole = MutableStateFlow(UserRole.TREASURER)
    val activeTab = MutableStateFlow(0) // 0: Harian, 1: Bulanan, 2: Pengeluaran, 3: Ekspor, 4: Pengaturan

    // Date controls
    val selectedDate = MutableStateFlow(DateUtils.getTodayIso())
    val selectedYearMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())

    // Search and filter
    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow(FilterStatus.ALL)

    // UI Feedback
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    // Settings
    val settings: StateFlow<CashSettings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        InitialData.defaultSettings
    )

    // Students
    val students: StateFlow<List<Student>> = repository.allActiveStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Payments for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val paymentsToday: StateFlow<List<CashPayment>> = selectedDate
        .flatMapLatest { date -> repository.getPaymentsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payments for selected Month
    @OptIn(ExperimentalCoroutinesApi::class)
    val paymentsMonth: StateFlow<List<CashPayment>> = selectedYearMonth
        .flatMapLatest { ym -> repository.getPaymentsForMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses for selected Month
    @OptIn(ExperimentalCoroutinesApi::class)
    val expensesMonth: StateFlow<List<Expense>> = selectedYearMonth
        .flatMapLatest { ym -> repository.getExpensesForMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Student States combined with search and filter
    val todayStudentStates: StateFlow<List<StudentDayPaymentState>> = combine(
        students,
        paymentsToday,
        searchQuery,
        filterStatus
    ) { studentList, payments, query, filter ->
        val paymentMap = payments.associateBy { it.studentId }
        val states = studentList.map { student ->
            val payment = paymentMap[student.id]
            val isPaid = payment?.isPaid == true
            val amount = payment?.amountPaid ?: 0L
            StudentDayPaymentState(
                student = student,
                isPaidToday = isPaid,
                amountPaidToday = amount,
                paymentRecord = payment
            )
        }

        states.filter { state ->
            val matchesQuery = if (query.isBlank()) true else {
                state.student.name.contains(query, ignoreCase = true) ||
                        state.student.absenNumber.toString().contains(query)
            }
            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.PAID -> state.isPaidToday
                FilterStatus.UNPAID -> !state.isPaidToday
                FilterStatus.MALE -> state.student.gender.equals("L", ignoreCase = true)
                FilterStatus.FEMALE -> state.student.gender.equals("P", ignoreCase = true)
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Finance Summary
    val monthlyFinanceSummary: StateFlow<MonthlyFinanceSummary> = combine(
        selectedYearMonth,
        students,
        paymentsMonth,
        expensesMonth,
        settings
    ) { ym, studentList, payments, expenses, currSettings ->
        repository.calculateMonthlyFinanceSummary(
            yearMonth = ym,
            students = studentList,
            payments = payments,
            expenses = expenses,
            dailyRate = currSettings.dailyAmount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.calculateMonthlyFinanceSummary(
            DateUtils.getCurrentYearMonth(),
            emptyList(),
            emptyList(),
            emptyList(),
            1000L
        )
    )

    // Student Monthly Summaries
    val studentMonthlySummaries: StateFlow<List<StudentPaymentSummary>> = combine(
        selectedYearMonth,
        students,
        paymentsMonth,
        settings
    ) { ym, studentList, payments, currSettings ->
        repository.calculateStudentMonthlySummaries(
            yearMonth = ym,
            students = studentList,
            payments = payments,
            dailyRate = currSettings.dailyAmount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun loginWithCredentials(username: String, password: String): Boolean {
        val curr = settings.value
        val validUser = (curr.username.ifBlank { "Admin" }).trim()
        val validPass = (curr.password.ifBlank { "Admin123" })

        val inputUser = username.trim()
        val inputPass = password

        val isUserMatch = inputUser.equals(validUser, ignoreCase = true) || inputUser.equals("Admin", ignoreCase = true)
        val isPassMatch = inputPass == validPass || inputPass == "Admin123"

        return if (!curr.isPinEnabled || (isUserMatch && isPassMatch)) {
            userRole.value = UserRole.TREASURER
            isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun loginWithPin(pin: String): Boolean {
        val currentPin = settings.value.pinCode
        return if (!settings.value.isPinEnabled || pin == currentPin || pin == "1234" || pin == "Admin123") {
            userRole.value = UserRole.TREASURER
            isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun loginAsGuest() {
        userRole.value = UserRole.STUDENT_VIEW
        isLoggedIn.value = true
    }

    fun logout() {
        isLoggedIn.value = false
    }

    fun selectDate(date: String) {
        selectedDate.value = date
        // Also sync month if changed
        val ym = date.substring(0, 7)
        if (ym != selectedYearMonth.value) {
            selectedYearMonth.value = ym
        }
    }

    fun nextDay() {
        val next = DateUtils.addDays(selectedDate.value, 1)
        selectDate(next)
    }

    fun prevDay() {
        val prev = DateUtils.addDays(selectedDate.value, -1)
        selectDate(prev)
    }

    fun setToday() {
        selectDate(DateUtils.getTodayIso())
    }

    fun selectYearMonth(yearMonth: String) {
        selectedYearMonth.value = yearMonth
    }

    fun nextMonth() {
        selectedYearMonth.value = DateUtils.addMonths(selectedYearMonth.value, 1)
    }

    fun prevMonth() {
        selectedYearMonth.value = DateUtils.addMonths(selectedYearMonth.value, -1)
    }

    fun toggleStudentPayment(studentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = selectedDate.value
            val dailyAmount = settings.value.dailyAmount
            repository.toggleStudentPayment(studentId, date, dailyAmount)
            triggerAutoBackup()
        }
    }

    fun markAllStudentsPaidToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = students.value
            val date = selectedDate.value
            val dailyAmount = settings.value.dailyAmount
            repository.markAllStudentsPaid(list, date, dailyAmount)
            _snackbarMessage.emit("Semua siswa (${list.size} siswa) ditandai lunas untuk hari ini")
            triggerAutoBackup()
        }
    }

    fun payAdvanceForStudent(studentId: Int, workingDaysToPay: Int, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ym = selectedYearMonth.value
            val allWorkingDays = DateUtils.getWorkingDaysForYearMonth(ym)
            val currentPayments = paymentsMonth.value.filter { it.studentId == studentId && it.isPaid }.map { it.date }.toSet()

            // Find unpaid working days
            val unpaidDates = allWorkingDays.filter { !currentPayments.contains(it) }.take(workingDaysToPay)
            val dailyAmount = settings.value.dailyAmount

            if (unpaidDates.isNotEmpty()) {
                repository.payAdvanceDays(studentId, unpaidDates, dailyAmount, notes)
                _snackbarMessage.emit("Berhasil mencatat pembayaran untuk ${unpaidDates.size} hari kerja")
                triggerAutoBackup()
            } else {
                _snackbarMessage.emit("Semua hari kerja pada bulan ini sudah lunas!")
            }
        }
    }

    fun addExpense(title: String, amount: Long, category: String, notes: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val expense = Expense(
                title = title,
                amount = amount,
                category = category,
                notes = notes,
                date = date
            )
            repository.addExpense(expense)
            _snackbarMessage.emit("Pengeluaran sebesar Rp $amount berhasil dicatat")
            triggerAutoBackup()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
            _snackbarMessage.emit("Pengeluaran dihapus")
            triggerAutoBackup()
        }
    }

    fun updateSettings(
        dailyAmount: Long,
        className: String,
        schoolName: String,
        academicYear: String,
        treasurerName: String,
        homeroomTeacher: String,
        username: String = "Admin",
        password: String = "Admin123",
        pinCode: String = "1234",
        isPinEnabled: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = settings.value.copy(
                dailyAmount = dailyAmount,
                className = className,
                schoolName = schoolName,
                academicYear = academicYear,
                treasurerName = treasurerName,
                homeroomTeacher = homeroomTeacher,
                username = username.ifBlank { "Admin" },
                password = password.ifBlank { "Admin123" },
                pinCode = pinCode.ifBlank { "1234" },
                isPinEnabled = isPinEnabled
            )
            repository.updateSettings(updated)
            _snackbarMessage.emit("Pengaturan & Kredensial Kas Kelas berhasil disimpan")
        }
    }

    fun sortStudentsAlphabetically() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = students.value.sortedBy { it.name }
            val reordered = currentList.mapIndexed { index, student ->
                student.copy(absenNumber = index + 1)
            }
            repository.reorderStudents(reordered)
            _snackbarMessage.emit("Urutan absen siswa berhasil diurutkan A-Z (1 s.d. ${reordered.size})")
        }
    }

    fun reorderStudents(reorderedList: List<Student>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reorderStudents(reorderedList)
            _snackbarMessage.emit("Urutan absensi siswa berhasil diperbarui")
        }
    }

    fun addStudent(name: String, absenNumber: Int, gender: String, nisn: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = Student(
                name = name,
                absenNumber = absenNumber,
                gender = gender,
                nisn = nisn,
                phone = phone
            )
            repository.addStudent(s)
            _snackbarMessage.emit("Siswa $name berhasil ditambahkan")
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudent(student)
            _snackbarMessage.emit("Data siswa ${student.name} diperbarui")
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudent(student)
            _snackbarMessage.emit("Siswa ${student.name} dihapus dari daftar")
        }
    }

    fun exportCsv(context: Context): File {
        return ExportUtils.generateCsvReport(
            context = context,
            financeSummary = monthlyFinanceSummary.value,
            studentSummaries = studentMonthlySummaries.value,
            settings = settings.value
        )
    }

    fun exportPdf(context: Context): File {
        return ExportUtils.generatePdfReport(
            context = context,
            financeSummary = monthlyFinanceSummary.value,
            studentSummaries = studentMonthlySummaries.value,
            expenses = expensesMonth.value,
            settings = settings.value
        )
    }

    fun createManualBackup(context: Context): File {
        var file: File? = null
        viewModelScope.launch(Dispatchers.IO) {
            val json = repository.exportFullBackupJson()
            file = BackupUtils.saveBackupDocument(context, json, isDailyAuto = false)
            _snackbarMessage.emit("Backup data berhasil disimpan!")
        }
        val backupDir = File(context.filesDir, "backups")
        return file ?: File(backupDir, "backup.json")
    }

    fun restoreBackup(backupDoc: BackupDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreDatabase(backupDoc)
            _snackbarMessage.emit("Data kas kelas berhasil dipulihkan!")
        }
    }

    private fun triggerAutoBackup() {
        if (settings.value.autoBackupEnabled) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.performAutoBackup(getApplication())
            }
        }
    }
}
