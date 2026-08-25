package com.example.util

import android.content.Context
import com.example.data.model.BackupDocument
import com.example.data.model.CashPayment
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.Student
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtils {

    fun createBackupJson(
        settings: CashSettings,
        students: List<Student>,
        payments: List<CashPayment>,
        expenses: List<Expense>
    ): String {
        val root = JSONObject()
        root.put("app", "Kas XI-F4")
        root.put("version", 1)
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        // Settings
        val settingsObj = JSONObject().apply {
            put("id", settings.id)
            put("dailyAmount", settings.dailyAmount)
            put("className", settings.className)
            put("schoolName", settings.schoolName)
            put("academicYear", settings.academicYear)
            put("treasurerName", settings.treasurerName)
            put("homeroomTeacher", settings.homeroomTeacher)
            put("pinCode", settings.pinCode)
            put("isPinEnabled", settings.isPinEnabled)
            put("autoBackupEnabled", settings.autoBackupEnabled)
        }
        root.put("settings", settingsObj)

        // Students
        val studentsArray = JSONArray()
        students.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("absenNumber", s.absenNumber)
                put("name", s.name)
                put("nisn", s.nisn)
                put("gender", s.gender)
                put("phone", s.phone)
                put("isActive", s.isActive)
            }
            studentsArray.put(sObj)
        }
        root.put("students", studentsArray)

        // Payments
        val paymentsArray = JSONArray()
        payments.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("studentId", p.studentId)
                put("date", p.date)
                put("amountPaid", p.amountPaid)
                put("isPaid", p.isPaid)
                put("paidAtTimestamp", p.paidAtTimestamp)
                put("notes", p.notes)
                put("recordedBy", p.recordedBy)
            }
            paymentsArray.put(pObj)
        }
        root.put("payments", paymentsArray)

        // Expenses
        val expensesArray = JSONArray()
        expenses.forEach { e ->
            val eObj = JSONObject().apply {
                put("id", e.id)
                put("date", e.date)
                put("title", e.title)
                put("amount", e.amount)
                put("category", e.category)
                put("notes", e.notes)
                put("timestamp", e.timestamp)
            }
            expensesArray.put(eObj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    fun saveBackupDocument(context: Context, jsonContent: String, isDailyAuto: Boolean = false): File {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val prefix = if (isDailyAuto) "AutoBackup_Kas_XI-F4" else "Backup_Kas_XI-F4"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${prefix}_$timeStamp.json"
        val file = File(backupDir, fileName)

        FileOutputStream(file).use {
            it.write(jsonContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun listBackupFiles(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun parseBackupJson(jsonString: String): BackupDocument? {
        return try {
            val root = JSONObject(jsonString)
            val backupDate = root.optString("backupDate", "")

            val sObj = root.getJSONObject("settings")
            val settings = CashSettings(
                id = sObj.optInt("id", 1),
                dailyAmount = sObj.optLong("dailyAmount", 2000L),
                className = sObj.optString("className", "XI-F4"),
                schoolName = sObj.optString("schoolName", "SMA Negeri 1"),
                academicYear = sObj.optString("academicYear", "2026/2027"),
                treasurerName = sObj.optString("treasurerName", "Siti Rahmawati"),
                homeroomTeacher = sObj.optString("homeroomTeacher", "Drs. Bambang Hidayat, M.Pd."),
                pinCode = sObj.optString("pinCode", "1234"),
                isPinEnabled = sObj.optBoolean("isPinEnabled", true),
                autoBackupEnabled = sObj.optBoolean("autoBackupEnabled", true)
            )

            val students = mutableListOf<Student>()
            val sArray = root.optJSONArray("students") ?: JSONArray()
            for (i in 0 until sArray.length()) {
                val item = sArray.getJSONObject(i)
                students.add(
                    Student(
                        id = item.optInt("id", 0),
                        absenNumber = item.optInt("absenNumber", i + 1),
                        name = item.optString("name", "Siswa $i"),
                        nisn = item.optString("nisn", ""),
                        gender = item.optString("gender", "L"),
                        phone = item.optString("phone", ""),
                        isActive = item.optBoolean("isActive", true)
                    )
                )
            }

            val payments = mutableListOf<CashPayment>()
            val pArray = root.optJSONArray("payments") ?: JSONArray()
            for (i in 0 until pArray.length()) {
                val item = pArray.getJSONObject(i)
                payments.add(
                    CashPayment(
                        id = item.optLong("id", 0),
                        studentId = item.getInt("studentId"),
                        date = item.getString("date"),
                        amountPaid = item.optLong("amountPaid", 2000L),
                        isPaid = item.optBoolean("isPaid", true),
                        paidAtTimestamp = item.optLong("paidAtTimestamp", System.currentTimeMillis()),
                        notes = item.optString("notes", ""),
                        recordedBy = item.optString("recordedBy", "Bendahara")
                    )
                )
            }

            val expenses = mutableListOf<Expense>()
            val eArray = root.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until eArray.length()) {
                val item = eArray.getJSONObject(i)
                expenses.add(
                    Expense(
                        id = item.optLong("id", 0),
                        date = item.getString("date"),
                        title = item.getString("title"),
                        amount = item.getLong("amount"),
                        category = item.optString("category", "Kebutuhan Kelas"),
                        notes = item.optString("notes", ""),
                        timestamp = item.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            BackupDocument(
                backupDate = backupDate,
                settings = settings,
                students = students,
                payments = payments,
                expenses = expenses
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
