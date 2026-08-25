package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.StudentPaymentSummary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.util.BackupUtils
import com.example.util.DateUtils
import com.example.util.ExportUtils
import com.example.util.FormatUtils

@Composable
fun ExportScreen(
    yearMonth: String,
    financeSummary: MonthlyFinanceSummary,
    studentSummaries: List<StudentPaymentSummary>,
    expenses: List<Expense> = emptyList(),
    settings: CashSettings,
    onExportPdf: (Context) -> Unit,
    onExportCsv: (Context) -> Unit,
    onManualBackup: (Context) -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Tabel Laporan Interaktif, 1: Cetak & Ekspor File
    var tableFilter by remember { mutableStateOf("ALL") } // ALL, PAID, UNPAID
    var lastExportMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LAPORAN & TABEL KAS KELAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kelas ${settings.className} • ${settings.schoolName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Periode: ${DateUtils.formatToMonthYear(yearMonth)} • Iuran: ${FormatUtils.formatRupiah(settings.dailyAmount)}/hari",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                if (onOpenSettings != null) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Sub Tabs: Tabel Laporan vs Cetak & Ekspor
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tabel Data Kas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak & Ekspor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeTab == 0) {
            // TAB 0: Interactive In-App Tables & Live Reports
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Financial Summary Matrix Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 Rekapitulasi Kas Bulan Ini",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Auto-Saved Database",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid of metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricColumn(
                                    label = "Target Kas",
                                    value = FormatUtils.formatRupiah(financeSummary.monthlyTargetTotal),
                                    subtext = "${financeSummary.totalStudents} Siswa × ${financeSummary.workingDaysCount} H",
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    label = "Terkumpul",
                                    value = FormatUtils.formatRupiah(financeSummary.monthlyCollectedTotal),
                                    subtext = "${String.format("%.1f", financeSummary.collectionPercentage)}% Capaian",
                                    containerColor = StatusSuccessContainer,
                                    textColor = StatusSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricColumn(
                                    label = "Sisa Tunggakan",
                                    value = FormatUtils.formatRupiah(financeSummary.monthlyRemainingTotal),
                                    subtext = "${studentSummaries.count { !it.isFullyPaid }} Siswa Belum Lunas",
                                    containerColor = if (financeSummary.monthlyRemainingTotal > 0) StatusDangerContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    textColor = if (financeSummary.monthlyRemainingTotal > 0) StatusDanger else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    label = "Saldo Kas Bersih",
                                    value = FormatUtils.formatRupiah(financeSummary.netBalance),
                                    subtext = "Biaya: ${FormatUtils.formatRupiah(financeSummary.monthlyExpenseTotal)}",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 2: Filter & Header for Student Table
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tabel Rincian Pembayaran Siswa",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = tableFilter == "ALL",
                                onClick = { tableFilter = "ALL" },
                                label = { Text("Semua (${studentSummaries.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = tableFilter == "PAID",
                                onClick = { tableFilter = "PAID" },
                                label = { Text("Lunas (${studentSummaries.count { it.isFullyPaid }})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = tableFilter == "UNPAID",
                                onClick = { tableFilter = "UNPAID" },
                                label = { Text("Nunggak (${studentSummaries.count { !it.isFullyPaid }})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Section 3: Student Payment Table View
                val filteredList = studentSummaries.filter {
                    when (tableFilter) {
                        "PAID" -> it.isFullyPaid
                        "UNPAID" -> !it.isFullyPaid
                        else -> true
                    }
                }

                // Table Header Row
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(28.dp))
                            Text("Nama Siswa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Text("Hari", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.width(42.dp))
                            Text("Bayar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.width(68.dp))
                            Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.width(62.dp))
                        }
                    }
                }

                items(filteredList, key = { it.student.id }) { s ->
                    Surface(
                        color = if (s.student.absenNumber % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${s.student.absenNumber}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.student.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                if (s.unpaidInMonth > 0) {
                                    Text(
                                        text = "Kurang: ${FormatUtils.formatRupiah(s.unpaidInMonth)}",
                                        fontSize = 10.sp,
                                        color = StatusDanger
                                    )
                                }
                            }
                            Text(
                                text = "${s.paidDaysCount}/${s.workingDaysCount}",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = FormatUtils.formatRupiah(s.totalPaidInMonth),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(68.dp)
                            )
                            Box(
                                modifier = Modifier.width(62.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    color = if (s.isFullyPaid) StatusSuccessContainer else StatusDangerContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (s.isFullyPaid) "LUNAS" else "NUNGGAK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (s.isFullyPaid) StatusSuccess else StatusDanger,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: Print & Export to PDF / Excel / Backup
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (lastExportMessage != null) {
                    Surface(
                        color = StatusSuccessContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lastExportMessage ?: "",
                                color = StatusSuccess,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Action 1: Export PDF
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFFFEBEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "1. Cetak Laporan PDF Resmi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lengkap dengan Kop Sekolah, Rekapitulasi Kas, Matriks Presensi, dan Kolom Tanda Tangan Wali Kelas & Bendahara",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val file = ExportUtils.generatePdfReport(
                                    context = context,
                                    financeSummary = financeSummary,
                                    studentSummaries = studentSummaries,
                                    expenses = expenses,
                                    settings = settings
                                )
                                ExportUtils.shareFile(
                                    context = context,
                                    file = file,
                                    mimeType = "application/pdf",
                                    subject = "Laporan Kas Kelas ${settings.className} ${DateUtils.formatToMonthYear(yearMonth)}"
                                )
                                lastExportMessage = "Dokumen PDF resmi berhasil dibuat & siap dicetak / dibagikan!"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_export_pdf"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buat & Cetak Dokumen PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Action 2: Export Excel / CSV
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "2. Ekspor ke Excel / Spreadsheet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "File CSV/XLS kompatibel Microsoft Excel & Google Sheets dengan seluruh matriks presensi dan rumus total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val file = ExportUtils.generateCsvReport(
                                    context = context,
                                    financeSummary = financeSummary,
                                    studentSummaries = studentSummaries,
                                    settings = settings
                                )
                                ExportUtils.shareFile(
                                    context = context,
                                    file = file,
                                    mimeType = "text/csv",
                                    subject = "Spreadsheet Kas Kelas ${settings.className} ${DateUtils.formatToMonthYear(yearMonth)}"
                                )
                                lastExportMessage = "File spreadsheet Excel (.csv) berhasil diekspor!"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_export_excel"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ekspor File Excel / CSV", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Action 3: Database Document Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "3. Auto-Save & Cadangan Database",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Setiap perubahan otomatis disimpan ke Room Database lokal & snapshot harian",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onManualBackup(context)
                                    lastExportMessage = "Cadangan database berhasil disimpan ke penyimpanan lokal!"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan Snapshot")
                            }

                            Button(
                                onClick = {
                                    val backupFiles = BackupUtils.listBackupFiles(context)
                                    if (backupFiles.isNotEmpty()) {
                                        ExportUtils.shareFile(
                                            context = context,
                                            file = backupFiles.first(),
                                            mimeType = "application/json",
                                            subject = "Backup Data Kas ${settings.className}"
                                        )
                                    } else {
                                        onManualBackup(context)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan JSON")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    subtext: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(subtext, fontSize = 9.5.sp, color = textColor.copy(alpha = 0.75f))
        }
    }
}
