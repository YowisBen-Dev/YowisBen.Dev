package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KasViewModel
import com.example.ui.UserRole
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MonthlyScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusSuccessContainer

class MainActivity : ComponentActivity() {

    private val viewModel: KasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: KasViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val studentStates by viewModel.todayStudentStates.collectAsStateWithLifecycle()
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()

    val monthlyFinanceSummary by viewModel.monthlyFinanceSummary.collectAsStateWithLifecycle()
    val studentMonthlySummaries by viewModel.studentMonthlySummaries.collectAsStateWithLifecycle()
    val expensesMonth by viewModel.expensesMonth.collectAsStateWithLifecycle()
    val paymentsMonth by viewModel.paymentsMonth.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            settings = settings,
            onLoginTreasurer = { username, password -> viewModel.loginWithCredentials(username, password) },
            onLoginGuest = { viewModel.loginAsGuest() }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_kas_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(34.dp).clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kas ${settings.className}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = if (userRole == UserRole.TREASURER) "Bendahara • Rp ${settings.dailyAmount}/hari" else "Mode Siswa (Transparansi)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (userRole == UserRole.TREASURER) {
                                Surface(
                                    onClick = { showSettingsDialog = true },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .testTag("btn_open_settings")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Pengaturan",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            Surface(
                                onClick = { viewModel.logout() },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("btn_logout")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Keluar",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    color = Color(0xFFF3F3FA),
                    shadowElevation = 4.dp
                ) {
                    NavigationBar(
                        containerColor = Color(0xFFF3F3FA)
                    ) {
                        NavigationBarItem(
                            selected = activeTab == 0,
                            onClick = { viewModel.activeTab.value = 0 },
                            icon = { Icon(Icons.Default.Today, contentDescription = "Harian") },
                            label = { Text("Harian", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_daily")
                        )
                        NavigationBarItem(
                            selected = activeTab == 1,
                            onClick = { viewModel.activeTab.value = 1 },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Bulanan") },
                            label = { Text("Bulanan", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_monthly")
                        )
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { viewModel.activeTab.value = 2 },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Pengeluaran") },
                            label = { Text("Pengeluaran", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_expenses")
                        )
                        NavigationBarItem(
                            selected = activeTab == 3,
                            onClick = { viewModel.activeTab.value = 3 },
                            icon = { Icon(Icons.Default.FileDownload, contentDescription = "Laporan") },
                            label = { Text("Laporan", fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_export")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardScreen(
                            selectedDate = selectedDate,
                            settings = settings,
                            userRole = userRole,
                            studentStates = studentStates,
                            allStudentsCount = allStudents.size,
                            searchQuery = searchQuery,
                            filterStatus = filterStatus,
                            workingDaysCountInMonth = monthlyFinanceSummary.workingDaysCount,
                            monthlyPayments = paymentsMonth,
                            onPrevDay = { viewModel.prevDay() },
                            onNextDay = { viewModel.nextDay() },
                            onSetToday = { viewModel.setToday() },
                            onSelectDate = { viewModel.selectedDate.value = it },
                            onSearchChange = { viewModel.searchQuery.value = it },
                            onFilterChange = { viewModel.filterStatus.value = it },
                            onTogglePay = { studentId -> viewModel.toggleStudentPayment(studentId) },
                            onMarkAllPaidToday = { viewModel.markAllStudentsPaidToday() },
                            onPayAdvanceDays = { studentId, days, notes ->
                                viewModel.payAdvanceForStudent(studentId, days, notes)
                            },
                            onAddStudent = { name, absen, gender, nisn, phone ->
                                viewModel.addStudent(name, absen, gender, nisn, phone)
                            }
                        )

                        1 -> MonthlyScreen(
                            yearMonth = selectedYearMonth,
                            financeSummary = monthlyFinanceSummary,
                            studentSummaries = studentMonthlySummaries,
                            settings = settings,
                            userRole = userRole,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onPayAdvanceDays = { studentId, days, notes ->
                                viewModel.payAdvanceForStudent(studentId, days, notes)
                            }
                        )

                        2 -> ExpenseScreen(
                            yearMonth = selectedYearMonth,
                            financeSummary = monthlyFinanceSummary,
                            expenses = expensesMonth,
                            userRole = userRole,
                            onAddExpense = { title, amount, category, notes, date ->
                                viewModel.addExpense(title, amount, category, notes, date)
                            },
                            onDeleteExpense = { expense ->
                                viewModel.deleteExpense(expense)
                            }
                        )

                        3 -> ExportScreen(
                            yearMonth = selectedYearMonth,
                            financeSummary = monthlyFinanceSummary,
                            studentSummaries = studentMonthlySummaries,
                            expenses = expensesMonth,
                            settings = settings,
                            onExportPdf = { ctx -> viewModel.exportPdf(ctx) },
                            onExportCsv = { ctx -> viewModel.exportCsv(ctx) },
                            onManualBackup = { ctx -> viewModel.createManualBackup(ctx) },
                            onOpenSettings = { showSettingsDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            students = allStudents,
            onDismiss = { showSettingsDialog = false },
            onSave = { dailyAmount, className, schoolName, academicYear, treasurer, teacher, username, password, pin, isPinEnabled ->
                viewModel.updateSettings(
                    dailyAmount,
                    className,
                    schoolName,
                    academicYear,
                    treasurer,
                    teacher,
                    username,
                    password,
                    pin,
                    isPinEnabled
                )
            },
            onSortAlphabetically = { viewModel.sortStudentsAlphabetically() },
            onUpdateStudent = { student -> viewModel.updateStudent(student) },
            onAddStudent = { name, absen, gender, nisn, phone -> viewModel.addStudent(name, absen, gender, nisn, phone) },
            onDeleteStudent = { student -> viewModel.deleteStudent(student) }
        )
    }
}
