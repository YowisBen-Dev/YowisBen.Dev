package com.example.data.repository

import android.content.Context
import com.example.data.db.KasDao
import com.example.data.model.BackupDocument
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.Student
import com.example.data.model.StudentPaymentSummary
import com.example.util.BackupUtils
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KasRepository(private val kasDao: KasDao) {

    val allActiveStudents: Flow<List<Student>> = kasDao.getAllActiveStudents()
    val settings: Flow<CashSettings> = kasDao.getSettings().map { it ?: com.example.data.db.InitialData.defaultSettings }
    val allExpenses: Flow<List<Expense>> = kasDao.getAllExpenses()

    fun getPaymentsForDate(date: String): Flow<List<CashPayment>> = kasDao.getPaymentsForDate(date)

    fun getPaymentsForMonth(yearMonth: String): Flow<List<CashPayment>> = kasDao.getPaymentsForMonth(yearMonth)

    fun getExpensesForMonth(yearMonth: String): Flow<List<Expense>> = kasDao.getExpensesForMonth(yearMonth)

    suspend fun toggleStudentPayment(studentId: Int, date: String, dailyAmount: Long) {
        val existing = kasDao.getPaymentForStudentAndDate(studentId, date)
        if (existing != null) {
            // Delete / uncheck
            kasDao.deletePaymentById(existing.id)
        } else {
            // Insert checked
            kasDao.insertPayment(
                CashPayment(
                    studentId = studentId,
                    date = date,
                    amountPaid = dailyAmount,
                    isPaid = true,
                    notes = "Lunas",
                    recordedBy = "Bendahara"
                )
            )
        }
    }

    suspend fun setStudentPaymentStatus(studentId: Int, date: String, isPaid: Boolean, amount: Long, notes: String = "") {
        val existing = kasDao.getPaymentForStudentAndDate(studentId, date)
        if (!isPaid) {
            if (existing != null) {
                kasDao.deletePaymentById(existing.id)
            }
        } else {
            if (existing != null) {
                kasDao.insertPayment(
                    existing.copy(
                        amountPaid = amount,
                        isPaid = true,
                        notes = notes.ifEmpty { "Lunas" },
                        paidAtTimestamp = System.currentTimeMillis()
                    )
                )
            } else {
                kasDao.insertPayment(
                    CashPayment(
                        studentId = studentId,
                        date = date,
                        amountPaid = amount,
                        isPaid = true,
                        notes = notes.ifEmpty { "Lunas" },
                        recordedBy = "Bendahara"
                    )
                )
            }
        }
    }

    suspend fun markAllStudentsPaid(students: List<Student>, date: String, dailyAmount: Long) {
        val payments = students.map { student ->
            CashPayment(
                studentId = student.id,
                date = date,
                amountPaid = dailyAmount,
                isPaid = true,
                notes = "Lunas Kolektif",
                recordedBy = "Bendahara"
            )
        }
        kasDao.insertPayments(payments)
    }

    suspend fun payAdvanceDays(studentId: Int, dates: List<String>, dailyAmount: Long, notes: String) {
        val payments = dates.map { date ->
            CashPayment(
                studentId = studentId,
                date = date,
                amountPaid = dailyAmount,
                isPaid = true,
                notes = notes.ifEmpty { "Bayar di Muka" },
                recordedBy = "Bendahara"
            )
        }
        kasDao.insertPayments(payments)
    }

    suspend fun addExpense(expense: Expense): Long = kasDao.insertExpense(expense)

    suspend fun deleteExpense(expense: Expense) = kasDao.deleteExpense(expense)

    suspend fun updateSettings(settings: CashSettings) = kasDao.insertOrUpdateSettings(settings)

    suspend fun addStudent(student: Student): Long = kasDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = kasDao.updateStudent(student)

    suspend fun reorderStudents(students: List<Student>) = kasDao.insertStudents(students)

    suspend fun deleteStudent(student: Student) = kasDao.deleteStudent(student)

    fun calculateStudentMonthlySummaries(
        yearMonth: String,
        students: List<Student>,
        payments: List<CashPayment>,
        dailyRate: Long
    ): List<StudentPaymentSummary> {
        val workingDays = DateUtils.getWorkingDaysForYearMonth(yearMonth)
        val workingDaysCount = workingDays.size
        val monthlyTargetPerStudent = workingDaysCount * dailyRate

        val paymentsByStudent = payments.groupBy { it.studentId }

        return students.map { student ->
            val studentPayments = paymentsByStudent[student.id] ?: emptyList()
            val dailyMap = mutableMapOf<String, Boolean>()
            var totalPaid = 0L
            var paidDaysCount = 0

            workingDays.forEach { date ->
                val p = studentPayments.firstOrNull { it.date == date && it.isPaid }
                if (p != null) {
                    dailyMap[date] = true
                    totalPaid += p.amountPaid
                    paidDaysCount++
                } else {
                    dailyMap[date] = false
                }
            }

            val unpaid = (monthlyTargetPerStudent - totalPaid).coerceAtLeast(0L)
            val isFullyPaid = (totalPaid >= monthlyTargetPerStudent && workingDaysCount > 0) || paidDaysCount >= workingDaysCount

            StudentPaymentSummary(
                student = student,
                totalPaidInMonth = totalPaid,
                targetInMonth = monthlyTargetPerStudent,
                unpaidInMonth = unpaid,
                paidDaysCount = paidDaysCount,
                workingDaysCount = workingDaysCount,
                isFullyPaid = isFullyPaid,
                dailyStatusMap = dailyMap
            )
        }
    }

    fun calculateMonthlyFinanceSummary(
        yearMonth: String,
        students: List<Student>,
        payments: List<CashPayment>,
        expenses: List<Expense>,
        dailyRate: Long
    ): MonthlyFinanceSummary {
        val workingDays = DateUtils.getWorkingDaysForYearMonth(yearMonth)
        val workingDaysCount = workingDays.size
        val totalStudents = students.size
        val monthlyTargetTotal = workingDaysCount * dailyRate * totalStudents

        // Sum payments in this month for working days
        val monthlyCollectedTotal = payments.filter { it.isPaid }.sumOf { it.amountPaid }
        val monthlyExpenseTotal = expenses.sumOf { it.amount }
        val remainingTotal = (monthlyTargetTotal - monthlyCollectedTotal).coerceAtLeast(0L)
        val netBalance = monthlyCollectedTotal - monthlyExpenseTotal
        val percentage = if (monthlyTargetTotal > 0) {
            (monthlyCollectedTotal.toFloat() / monthlyTargetTotal.toFloat()) * 100f
        } else 0f

        return MonthlyFinanceSummary(
            yearMonth = yearMonth,
            workingDaysCount = workingDaysCount,
            workingDaysList = workingDays,
            totalStudents = totalStudents,
            dailyRate = dailyRate,
            monthlyTargetTotal = monthlyTargetTotal,
            monthlyCollectedTotal = monthlyCollectedTotal,
            monthlyRemainingTotal = remainingTotal,
            monthlyExpenseTotal = monthlyExpenseTotal,
            netBalance = netBalance,
            collectionPercentage = percentage
        )
    }

    suspend fun exportFullBackupJson(): String {
        val settings = kasDao.getSettingsSync() ?: com.example.data.db.InitialData.defaultSettings
        val students = kasDao.getAllStudentsSync()
        val payments = kasDao.getAllPaymentsSync()
        val expenses = kasDao.getAllExpensesSync()
        return BackupUtils.createBackupJson(settings, students, payments, expenses)
    }

    suspend fun restoreDatabase(backupDoc: BackupDocument) {
        kasDao.insertOrUpdateSettings(backupDoc.settings)
        if (backupDoc.students.isNotEmpty()) {
            kasDao.insertStudents(backupDoc.students)
        }
        if (backupDoc.payments.isNotEmpty()) {
            kasDao.insertPayments(backupDoc.payments)
        }
        backupDoc.expenses.forEach {
            kasDao.insertExpense(it)
        }
    }

    suspend fun performAutoBackup(context: Context) {
        val json = exportFullBackupJson()
        BackupUtils.saveBackupDocument(context, json, isDailyAuto = true)
    }
}
