package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.Student
import com.example.data.model.StudentDayPaymentState
import com.example.ui.FilterStatus
import com.example.ui.UserRole
import com.example.ui.components.AddEditStudentDialog
import com.example.ui.components.QuickPayDialog
import com.example.ui.components.SmartCalendarDialog
import com.example.ui.components.StudentPaymentItem
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWeekend
import com.example.ui.theme.StatusWeekendContainer
import com.example.util.DateUtils
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    selectedDate: String,
    settings: CashSettings,
    userRole: UserRole,
    studentStates: List<StudentDayPaymentState>,
    allStudentsCount: Int,
    searchQuery: String,
    filterStatus: FilterStatus,
    workingDaysCountInMonth: Int,
    monthlyPayments: List<CashPayment> = emptyList(),
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onSetToday: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (FilterStatus) -> Unit,
    onTogglePay: (studentId: Int) -> Unit,
    onMarkAllPaidToday: () -> Unit,
    onPayAdvanceDays: (studentId: Int, days: Int, notes: String) -> Unit,
    onAddStudent: (name: String, absenNumber: Int, gender: String, nisn: String, phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isWeekend = DateUtils.isWeekend(selectedDate)
    val isToday = selectedDate == DateUtils.getTodayIso()
    val dayOfWeekName = DateUtils.getDayOfWeekName(selectedDate)

    val paidCount = studentStates.count { it.isPaidToday }
    val unpaidCount = allStudentsCount - paidCount
    val totalCount = studentStates.size
    val dailyTargetTotal = allStudentsCount * settings.dailyAmount
    val todayCollectedTotal = paidCount * settings.dailyAmount
    val progress = if (allStudentsCount > 0) paidCount.toFloat() / allStudentsCount.toFloat() else 0f

    var selectedStudentForDialog by remember { mutableStateOf<StudentDayPaymentState?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Real-Time Day, Date & Smart Calendar Navigator Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevDay,
                    modifier = Modifier.testTag("btn_prev_day")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Hari Sebelumnya", tint = MaterialTheme.colorScheme.primary)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showCalendarDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateUtils.formatToDisplayDate(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = if (isWeekend) StatusWeekendContainer else StatusSuccessContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isWeekend) "🏖️ Hari Libur (Bebas Kas)" else "🏫 Hari Sekolah (Efektif Kas)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWeekend) StatusWeekend else StatusSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            onClick = { showCalendarDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "📅 Pantau Kalender",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!isToday) {
                            Surface(
                                onClick = onSetToday,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Hari Ini",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNextDay,
                    modifier = Modifier.testTag("btn_next_day")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Hari Berikutnya", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Unpaid Students Automatic Detection Alert Banner
        if (!isWeekend && unpaidCount > 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .clickable { onFilterChange(FilterStatus.UNPAID) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "⚠️ DETEKSI: $unpaidCount Siswa Belum Bayar Kas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Pada $dayOfWeekName • Terkumpul ${FormatUtils.formatRupiah(todayCollectedTotal)} dari target ${FormatUtils.formatRupiah(dailyTargetTotal)}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Lihat Nama",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Weekend Notice Banner if Saturday or Sunday
        if (isWeekend) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp),
                shape = RoundedCornerShape(14.dp),
                color = StatusWeekendContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = StatusWeekend,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hari $dayOfWeekName (Akhir Pekan / Libur Kas). Sistem otomatis hanya memantau kas pada hari kerja Senin s.d. Jumat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusWeekend,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Geometric Balance 2-Card Metric Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: IURAN / HARI
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E2EC)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "IURAN / HARI",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF44474E)
                    )
                    Column {
                        Text(
                            text = FormatUtils.formatRupiah(settings.dailyAmount),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "$allStudentsCount Siswa Terdaftar",
                            fontSize = 10.sp,
                            color = Color(0xFF44474E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Card 2: KAS TERKUMPUL
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6E3FF)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "KAS TERKUMPUL",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF004786)
                    )
                    Column {
                        Text(
                            text = FormatUtils.formatRupiah(todayCollectedTotal),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF001B3D)
                        )
                        Text(
                            text = "$paidCount / $allStudentsCount Siswa Lunas",
                            fontSize = 10.sp,
                            color = Color(0xFF004786),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Search & Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari nama siswa atau no. absen...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_students_input")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterStatus == FilterStatus.ALL,
                        onClick = { onFilterChange(FilterStatus.ALL) },
                        label = { Text("Semua ($allStudentsCount)", fontSize = 11.5.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FilterStatus.UNPAID,
                        onClick = { onFilterChange(FilterStatus.UNPAID) },
                        label = { Text("⚠️ Belum Bayar ($unpaidCount)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FilterStatus.PAID,
                        onClick = { onFilterChange(FilterStatus.PAID) },
                        label = { Text("Lunas ($paidCount)", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusSuccessContainer,
                            selectedLabelColor = StatusSuccess
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FilterStatus.MALE,
                        onClick = { onFilterChange(FilterStatus.MALE) },
                        label = { Text("Laki-laki", fontSize = 11.5.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FilterStatus.FEMALE,
                        onClick = { onFilterChange(FilterStatus.FEMALE) },
                        label = { Text("Perempuan", fontSize = 11.5.sp) }
                    )
                }
            }
        }

        // Quick Action Bar for Treasurer
        if (userRole == UserRole.TREASURER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMarkAllPaidToday,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_mark_all_paid"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimary
                    )
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Semua Lunas Hari Ini", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showAddStudentDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("btn_add_student")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = GeoPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                }
            }
        }

        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Siswa (${studentStates.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Surface(
                color = Color(0xFFE0E2EC),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isWeekend) "LIBUR KAS" else "HARI EFEKTIF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWeekend) StatusWeekend else Color(0xFF004786),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Student List
        if (studentStates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Tidak ada siswa yang cocok dengan pencarian." else "Daftar siswa kosong.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(studentStates, key = { it.student.id }) { state ->
                    StudentPaymentItem(
                        state = state,
                        dailyAmount = settings.dailyAmount,
                        userRole = userRole,
                        onTogglePay = { onTogglePay(state.student.id) },
                        onCardClick = { selectedStudentForDialog = state }
                    )
                }
            }
        }
    }

    // Smart Calendar Dialog
    if (showCalendarDialog) {
        SmartCalendarDialog(
            currentSelectedDate = selectedDate,
            totalStudentsCount = allStudentsCount,
            monthlyPayments = monthlyPayments,
            onSelectDate = { date ->
                onSelectDate(date)
                showCalendarDialog = false
            },
            onDismiss = { showCalendarDialog = false }
        )
    }

    // Quick Pay Sheet / Dialog
    selectedStudentForDialog?.let { state ->
        QuickPayDialog(
            student = state.student,
            isPaidToday = state.isPaidToday,
            dailyAmount = settings.dailyAmount,
            workingDaysCountInMonth = workingDaysCountInMonth,
            paidDaysCountInMonth = 0,
            userRole = userRole,
            onDismiss = { selectedStudentForDialog = null },
            onToggleToday = { onTogglePay(state.student.id) },
            onPayAdvanceDays = { days, notes ->
                onPayAdvanceDays(state.student.id, days, notes)
            }
        )
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AddEditStudentDialog(
            nextAbsenNumber = allStudentsCount + 1,
            onDismiss = { showAddStudentDialog = false },
            onSave = { name, absen, gender, nisn, phone ->
                onAddStudent(name, absen, gender, nisn, phone)
                showAddStudentDialog = false
            }
        )
    }
}

