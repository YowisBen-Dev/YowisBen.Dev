package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashSettings
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.StudentPaymentSummary
import com.example.ui.UserRole
import com.example.ui.components.QuickPayDialog
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.util.DateUtils
import com.example.util.FormatUtils

@Composable
fun MonthlyScreen(
    yearMonth: String,
    financeSummary: MonthlyFinanceSummary,
    studentSummaries: List<StudentPaymentSummary>,
    settings: CashSettings,
    userRole: UserRole,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPayAdvanceDays: (studentId: Int, days: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Ringkasan & Tunggakan, 1: Matriks Presensi Kas
    var selectedStudentSummary by remember { mutableStateOf<StudentPaymentSummary?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month Selector Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("btn_prev_month")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Bulan Sebelumnya")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DateUtils.formatToMonthYear(yearMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${financeSummary.workingDaysCount} Hari Kerja (Senin s.d. Jumat)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("btn_next_month")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Bulan Berikutnya")
                }
            }
        }

        // Rule Notification
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alokasi Kas: ${FormatUtils.formatRupiah(settings.dailyAmount)}/anak per hari kerja. Sabtu & Minggu libur otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Financial 4-Card Overview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Target Kas
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TARGET BULAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(FormatUtils.formatRupiah(financeSummary.monthlyTargetTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${financeSummary.totalStudents} Siswa × ${financeSummary.workingDaysCount} H", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Card 2: Terkumpul
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusSuccessContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TERKUMPUL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(FormatUtils.formatRupiah(financeSummary.monthlyCollectedTotal), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = StatusSuccess)
                    Text("${String.format("%.1f", financeSummary.collectionPercentage)}% Tercapai", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = StatusSuccess)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 3: Tunggakan
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (financeSummary.monthlyRemainingTotal > 0) StatusDangerContainer else MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("SISA TUNGGAKAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (financeSummary.monthlyRemainingTotal > 0) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(FormatUtils.formatRupiah(financeSummary.monthlyRemainingTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (financeSummary.monthlyRemainingTotal > 0) StatusDanger else MaterialTheme.colorScheme.onSurface)
                    val fullyPaidCount = studentSummaries.count { it.isFullyPaid }
                    Text("$fullyPaidCount/${studentSummaries.size} Siswa Lunas", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Card 4: Saldo Bersih Kas (Terkumpul - Pengeluaran)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("SALDO KAS BERSIH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(FormatUtils.formatRupiah(financeSummary.netBalance), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Pengeluaran: ${FormatUtils.formatRupiah(financeSummary.monthlyExpenseTotal)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sub Tabs: Tunggakan vs Matriks Presensi
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Status Tunggakan", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarViewMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Matriks Presensi Kas", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedSubTab == 0) {
            // List of students with monthly progress
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortedList = studentSummaries.sortedWith(
                    compareBy<StudentPaymentSummary> { it.isFullyPaid }.thenByDescending { it.unpaidInMonth }
                )

                items(sortedList, key = { it.student.id }) { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (s.isFullyPaid) StatusSuccessContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${s.student.absenNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (s.isFullyPaid) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.student.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${s.paidDaysCount}/${s.workingDaysCount} Hari",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (s.isFullyPaid) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Bayar: ${FormatUtils.formatRupiah(s.totalPaidInMonth)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (s.isFullyPaid) {
                                    Surface(
                                        color = StatusSuccessContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "✓ LUNAS",
                                            color = StatusSuccess,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = StatusDangerContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Tunggakan: ${FormatUtils.formatRupiah(s.unpaidInMonth)}",
                                            color = StatusDanger,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    if (userRole == UserRole.TREASURER) {
                                        OutlinedButton(
                                            onClick = { selectedStudentSummary = s },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text("Bayar", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Matrix Grid: Students vs Working Days
            val workingDays = financeSummary.workingDaysList
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Matriks Hari Kerja (Senin s.d. Jumat)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "No/Nama Siswa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(130.dp).padding(start = 6.dp)
                                )
                                workingDays.forEach { date ->
                                    val dayNum = DateUtils.getDayNumber(date)
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(26.dp)
                                    )
                                }
                                Text(
                                    text = "Total",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(48.dp)
                                )
                            }
                        }

                        // Student Rows
                        items(studentSummaries, key = { it.student.id }) { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${s.student.absenNumber}. ${s.student.name}",
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    modifier = Modifier.width(130.dp).padding(start = 6.dp)
                                )
                                workingDays.forEach { date ->
                                    val isPaid = s.dailyStatusMap[date] == true
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isPaid) StatusSuccessContainer else StatusDangerContainer.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPaid) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = StatusSuccess,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = StatusDanger,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                }

                                Text(
                                    text = "${s.paidDaysCount}H",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (s.isFullyPaid) StatusSuccess else StatusDanger,
                                    modifier = Modifier.width(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick pay modal from monthly list
    selectedStudentSummary?.let { s ->
        QuickPayDialog(
            student = s.student,
            isPaidToday = s.dailyStatusMap[DateUtils.getTodayIso()] == true,
            dailyAmount = settings.dailyAmount,
            workingDaysCountInMonth = s.workingDaysCount,
            paidDaysCountInMonth = s.paidDaysCount,
            userRole = userRole,
            onDismiss = { selectedStudentSummary = null },
            onToggleToday = {
                onPayAdvanceDays(s.student.id, 1, "Bayar Kas")
            },
            onPayAdvanceDays = { days, notes ->
                onPayAdvanceDays(s.student.id, days, notes)
            }
        )
    }
}
