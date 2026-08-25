package com.example.data.model

data class StudentPaymentSummary(
    val student: Student,
    val totalPaidInMonth: Long,
    val targetInMonth: Long,
    val unpaidInMonth: Long,
    val paidDaysCount: Int,
    val workingDaysCount: Int,
    val isFullyPaid: Boolean,
    val dailyStatusMap: Map<String, Boolean>
)

data class MonthlyFinanceSummary(
    val yearMonth: String,
    val workingDaysCount: Int,
    val workingDaysList: List<String>,
    val totalStudents: Int,
    val dailyRate: Long,
    val monthlyTargetTotal: Long,
    val monthlyCollectedTotal: Long,
    val monthlyRemainingTotal: Long,
    val monthlyExpenseTotal: Long,
    val netBalance: Long,
    val collectionPercentage: Float
)

data class StudentDayPaymentState(
    val student: Student,
    val isPaidToday: Boolean,
    val amountPaidToday: Long,
    val paymentRecord: CashPayment? = null
)

data class BackupDocument(
    val app: String = "Kas XI-F4",
    val backupDate: String,
    val settings: CashSettings,
    val students: List<Student>,
    val payments: List<CashPayment>,
    val expenses: List<Expense>
)
