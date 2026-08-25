package com.example.data.db

import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.Student
import com.example.util.DateUtils
import java.util.Calendar

object InitialData {
    val defaultSettings = CashSettings(
        id = 1,
        dailyAmount = 1000L,
        className = "XI-F4",
        schoolName = "SMA Negeri 1",
        academicYear = "2026/2027",
        treasurerName = "Siti Rahmawati & Dimas Arya",
        homeroomTeacher = "Drs. Bambang Hidayat, M.Pd.",
        username = "Admin",
        password = "Admin123",
        pinCode = "1234",
        isPinEnabled = true,
        autoBackupEnabled = true
    )

    val defaultStudents = listOf(
        Student(id = 1, absenNumber = 1, name = "Aditia Pratama", nisn = "0078123401", gender = "L", phone = "081234567801"),
        Student(id = 2, absenNumber = 2, name = "Alya Nur Fadillah", nisn = "0078123402", gender = "P", phone = "081234567802"),
        Student(id = 3, absenNumber = 3, name = "Ananda Bagus Putra", nisn = "0078123403", gender = "L", phone = "081234567803"),
        Student(id = 4, absenNumber = 4, name = "Annisa Rahmawati", nisn = "0078123404", gender = "P", phone = "081234567804"),
        Student(id = 5, absenNumber = 5, name = "Arya Bima Sena", nisn = "0078123405", gender = "L", phone = "081234567805"),
        Student(id = 6, absenNumber = 6, name = "Bayu Tri Wicaksono", nisn = "0078123406", gender = "L", phone = "081234567806"),
        Student(id = 7, absenNumber = 7, name = "Cantika Putri Lestari", nisn = "0078123407", gender = "P", phone = "081234567807"),
        Student(id = 8, absenNumber = 8, name = "Citra Dewi Anggraeni", nisn = "0078123408", gender = "P", phone = "081234567808"),
        Student(id = 9, absenNumber = 9, name = "Daffa Ibnu Hafizh", nisn = "0078123409", gender = "L", phone = "081234567809"),
        Student(id = 10, absenNumber = 10, name = "Dimas Arya Pangestu", nisn = "0078123410", gender = "L", phone = "081234567810"),
        Student(id = 11, absenNumber = 11, name = "Dinda Ayu Safitri", nisn = "0078123411", gender = "P", phone = "081234567811"),
        Student(id = 12, absenNumber = 12, name = "Eka Nur Khotimah", nisn = "0078123412", gender = "P", phone = "081234567812"),
        Student(id = 13, absenNumber = 13, name = "Fadhil Muhammad", nisn = "0078123413", gender = "L", phone = "081234567813"),
        Student(id = 14, absenNumber = 14, name = "Fajar Ramadhan", nisn = "0078123414", gender = "L", phone = "081234567814"),
        Student(id = 15, absenNumber = 15, name = "Gita Savitri Maharani", nisn = "0078123415", gender = "P", phone = "081234567815"),
        Student(id = 16, absenNumber = 16, name = "Hadi Wijaya Kusuma", nisn = "0078123416", gender = "L", phone = "081234567816"),
        Student(id = 17, absenNumber = 17, name = "Hanif Fauzan", nisn = "0078123417", gender = "L", phone = "081234567817"),
        Student(id = 18, absenNumber = 18, name = "Indah Permatasari", nisn = "0078123418", gender = "P", phone = "081234567818"),
        Student(id = 19, absenNumber = 19, name = "Joko Prasetyo Nugroho", nisn = "0078123419", gender = "L", phone = "081234567819"),
        Student(id = 20, absenNumber = 20, name = "Kevin Jonathan Siregar", nisn = "0078123420", gender = "L", phone = "081234567820"),
        Student(id = 21, absenNumber = 21, name = "Larasati Putri Utami", nisn = "0078123421", gender = "P", phone = "081234567821"),
        Student(id = 22, absenNumber = 22, name = "Maulana Malik Ibrahim", nisn = "0078123422", gender = "L", phone = "081234567822"),
        Student(id = 23, absenNumber = 23, name = "Muhammad Rizky Pratama", nisn = "0078123423", gender = "L", phone = "081234567823"),
        Student(id = 24, absenNumber = 24, name = "Nabila Syahputri", nisn = "0078123424", gender = "P", phone = "081234567824"),
        Student(id = 25, absenNumber = 25, name = "Naufal Zhafran", nisn = "0078123425", gender = "L", phone = "081234567825"),
        Student(id = 26, absenNumber = 26, name = "Nurul Azizah", nisn = "0078123426", gender = "P", phone = "081234567826"),
        Student(id = 27, absenNumber = 27, name = "Putri Wulandari", nisn = "0078123427", gender = "P", phone = "081234567827"),
        Student(id = 28, absenNumber = 28, name = "Rafi Ahmad Syauqi", nisn = "0078123428", gender = "L", phone = "081234567828"),
        Student(id = 29, absenNumber = 29, name = "Rina Marlina", nisn = "0078123429", gender = "P", phone = "081234567829"),
        Student(id = 30, absenNumber = 30, name = "Satria Yudha Pratama", nisn = "0078123430", gender = "L", phone = "081234567830"),
        Student(id = 31, absenNumber = 31, name = "Siti Rahmawati", nisn = "0078123431", gender = "P", phone = "081234567831"),
        Student(id = 32, absenNumber = 32, name = "Taufik Hidayat", nisn = "0078123432", gender = "L", phone = "081234567832"),
        Student(id = 33, absenNumber = 33, name = "Tiara Maharani", nisn = "0078123433", gender = "P", phone = "081234567833"),
        Student(id = 34, absenNumber = 34, name = "Vina Panduwinata", nisn = "0078123434", gender = "P", phone = "081234567834"),
        Student(id = 35, absenNumber = 35, name = "Wahyu Setiawan", nisn = "0078123435", gender = "L", phone = "081234567835"),
        Student(id = 36, absenNumber = 36, name = "Zahra Salsabila", nisn = "0078123436", gender = "P", phone = "081234567836")
    )

    fun createInitialPayments(currentYearMonth: String): List<CashPayment> {
        val workingDays = DateUtils.getWorkingDaysForYearMonth(currentYearMonth)
        val today = DateUtils.getTodayIso()
        val payments = mutableListOf<CashPayment>()

        // Pre-fill some past working days of the current month
        val pastWorkingDays = workingDays.filter { it <= today }
        for (date in pastWorkingDays) {
            // For past dates, 80-90% of students paid
            defaultStudents.forEach { student ->
                // pseudo random based on student id and date hash
                val shouldPay = ((student.id * 17 + date.hashCode()) % 10) > 1
                if (shouldPay) {
                    payments.add(
                        CashPayment(
                            studentId = student.id,
                            date = date,
                            amountPaid = 1000L,
                            isPaid = true,
                            notes = "Lunas",
                            recordedBy = "Bendahara"
                        )
                    )
                }
            }
        }
        return payments
    }

    fun createInitialExpenses(todayIso: String): List<Expense> {
        return listOf(
            Expense(
                id = 1,
                date = todayIso,
                title = "Beli Spidol Boardmarker & Penghapus",
                amount = 25000L,
                category = "Kebutuhan Kelas",
                notes = "Isi ulang 3 spidol hitam + 1 biru"
            ),
            Expense(
                id = 2,
                date = todayIso,
                title = "Fotokopi Latihan Soal Matematika",
                amount = 18000L,
                category = "Fotokopi",
                notes = "36 rangkap untuk latihan ujian"
            ),
            Expense(
                id = 3,
                date = todayIso,
                title = "Uang Kas Sosial (Jenguk Teman Sakit)",
                amount = 50000L,
                category = "Sosial / Jenguk",
                notes = "Bingkisan buah untuk Joko Prasetyo"
            )
        )
    }
}
