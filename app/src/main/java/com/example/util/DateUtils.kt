package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatRupiah(amount: Long): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    fun formatNumber(amount: Long): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getNumberInstance(localeID)
        return formatter.format(amount)
    }
}

object DateUtils {
    private val localeID = Locale("in", "ID")
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeID)
    private val shortDateFormat = SimpleDateFormat("dd MMM yyyy", localeID)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", localeID)
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val dayNumberFormat = SimpleDateFormat("dd", localeID)
    private val dayNameFormat = SimpleDateFormat("EEE", localeID)

    fun getTodayIso(): String {
        return isoDateFormat.format(Date())
    }

    fun getCurrentYearMonth(): String {
        return yearMonthFormat.format(Date())
    }

    fun formatToDisplayDate(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            displayDateFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatToShortDate(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            shortDateFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatToMonthYear(yearMonth: String): String {
        return try {
            val ym = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth) ?: Date()
            monthYearFormat.format(ym)
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun isWeekend(isoDate: String): Boolean {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        } catch (e: Exception) {
            false
        }
    }

    fun getDayOfWeekName(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            SimpleDateFormat("EEEE", localeID).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun getShortDayOfWeekName(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            SimpleDateFormat("EEE", localeID).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatToFullRealTimeDate(date: Date = Date()): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", localeID).format(date)
    }

    fun getAllDaysInMonth(yearMonth: String): List<String> {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val allDays = mutableListOf<String>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (day in 1..daysInMonth) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                allDays.add(isoDateFormat.format(cal.time))
            }
            allDays
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getDayNumber(isoDate: String): Int {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Mengembalikan daftar semua tanggal dalam bulan tertentu yang merupakan Hari Kerja (Senin - Jumat).
     * Sabtu dan Minggu diabaikan (bebas kas).
     */
    fun getWorkingDaysInMonth(year: Int, month1Based: Int): List<String> {
        val workingDays = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month1Based - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                workingDays.add(isoDateFormat.format(cal.time))
            }
        }
        return workingDays
    }

    /**
     * Mendapatkan working days untuk format "yyyy-MM"
     */
    fun getWorkingDaysForYearMonth(yearMonth: String): List<String> {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            getWorkingDaysInMonth(year, month)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addDays(isoDate: String, days: Int): String {
        return try {
            val date = isoDateFormat.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_MONTH, days)
            isoDateFormat.format(cal.time)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun addMonths(yearMonth: String, months: Int): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, months)
            yearMonthFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }
}
