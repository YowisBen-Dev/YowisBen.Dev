package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val absenNumber: Int,
    val name: String,
    val nisn: String = "",
    val gender: String = "L", // "L" or "P"
    val phone: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "cash_settings")
data class CashSettings(
    @PrimaryKey
    val id: Int = 1,
    val dailyAmount: Long = 1000L, // Rp 1.000 per anak / hari
    val className: String = "XI-F4",
    val schoolName: String = "SMA Negeri 1",
    val academicYear: String = "2026/2027",
    val treasurerName: String = "Siti Rahmawati",
    val homeroomTeacher: String = "Drs. Bambang Hidayat, M.Pd.",
    val username: String = "Admin",
    val password: String = "Admin123",
    val pinCode: String = "1234",
    val isPinEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = true
)

@Entity(
    tableName = "cash_payments"
)
data class CashPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Int,
    val date: String, // "yyyy-MM-dd"
    val amountPaid: Long, // e.g. 2000
    val isPaid: Boolean = true,
    val paidAtTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val recordedBy: String = "Bendahara"
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val title: String,
    val amount: Long,
    val category: String = "Kebutuhan Kelas", // e.g. Alat Tulis/Spidol, Fotokopi, Sosial/Jenguk, Kebersihan, Acara Kelas
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_snapshots")
data class DailySnapshot(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val totalPaid: Long,
    val studentsCount: Int,
    val studentsPaidCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
