package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.CashSettings
import com.example.data.model.Expense
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.StudentPaymentSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    /**
     * Generates a CSV/Excel compatible file for the monthly cash report.
     */
    fun generateCsvReport(
        context: Context,
        financeSummary: MonthlyFinanceSummary,
        studentSummaries: List<StudentPaymentSummary>,
        settings: CashSettings
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeMonth = financeSummary.yearMonth.replace("-", "_")
        val fileName = "Laporan_Kas_${settings.className}_$safeMonth.csv"
        val file = File(reportsDir, fileName)

        val workingDays = financeSummary.workingDaysList

        val writer = file.bufferedWriter(Charsets.UTF_8)
        // UTF-8 BOM for Microsoft Excel compatibility
        writer.write("\uFEFF")

        // Header
        writer.appendLine("LAPORAN KAS KELAS ${settings.className} - ${settings.schoolName.uppercase()}")
        writer.appendLine("PERIODE: ${DateUtils.formatToMonthYear(financeSummary.yearMonth)}")
        writer.appendLine("Wali Kelas: ${settings.homeroomTeacher} | Bendahara: ${settings.treasurerName}")
        writer.appendLine("Nominal Kas Harian: Rp ${settings.dailyAmount} (Senin s.d. Jumat | Sabtu & Minggu Libur)")
        writer.appendLine("Total Hari Kerja: ${financeSummary.workingDaysCount} Hari")
        writer.appendLine("Target Kas: Rp ${financeSummary.monthlyTargetTotal} | Terkumpul: Rp ${financeSummary.monthlyCollectedTotal} | Tunggakan: Rp ${financeSummary.monthlyRemainingTotal}")
        writer.appendLine("Pengeluaran: Rp ${financeSummary.monthlyExpenseTotal} | Saldo Bersih: Rp ${financeSummary.netBalance}")
        writer.appendLine()

        // Table Header
        val headerSb = StringBuilder("No,Absen,Nama Siswa,L/P")
        workingDays.forEach { date ->
            val dayNum = DateUtils.getDayNumber(date)
            headerSb.append(",Tgl $dayNum")
        }
        headerSb.append(",Hari Bayar,Total Bayar (Rp),Target (Rp),Tunggakan (Rp),Status")
        writer.appendLine(headerSb.toString())

        // Rows
        studentSummaries.forEachIndexed { index, s ->
            val rowSb = StringBuilder()
            rowSb.append("${index + 1},${s.student.absenNumber},\"${s.student.name}\",${s.student.gender}")
            workingDays.forEach { date ->
                val isPaid = s.dailyStatusMap[date] == true
                rowSb.append(if (isPaid) ",LUNAS" else ",-")
            }
            val status = if (s.isFullyPaid) "LUNAS" else "BELUM LUNAS"
            rowSb.append(",${s.paidDaysCount},${s.totalPaidInMonth},${s.targetInMonth},${s.unpaidInMonth},$status")
            writer.appendLine(rowSb.toString())
        }

        writer.appendLine()
        writer.appendLine(",,,TOTAL,,${financeSummary.monthlyCollectedTotal},${financeSummary.monthlyTargetTotal},${financeSummary.monthlyRemainingTotal},")
        writer.appendLine("Dicetak pada: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("in", "ID")).format(Date())}")

        writer.flush()
        writer.close()

        return file
    }

    /**
     * Generates a clean, professional PDF Document using Android's native PdfDocument.
     */
    fun generatePdfReport(
        context: Context,
        financeSummary: MonthlyFinanceSummary,
        studentSummaries: List<StudentPaymentSummary>,
        expenses: List<Expense>,
        settings: CashSettings
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeMonth = financeSummary.yearMonth.replace("-", "_")
        val fileName = "Laporan_Kas_${settings.className}_$safeMonth.pdf"
        val file = File(reportsDir, fileName)

        // A4 dimension in 72 DPI points: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pdfDocument = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Page 1: Header + Financial Metrics + Student Table Part 1
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Header Background Banner
        paint.color = Color.rgb(15, 60, 50)
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

        // Header Accent line
        paint.color = Color.rgb(0, 168, 107)
        canvas.drawRect(0f, 95f, pageWidth.toFloat(), 98f, paint)

        // Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("LAPORAN KAS KELAS ${settings.className}", 30f, 35f, textPaint)

        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${settings.schoolName} • Tahun Ajaran ${settings.academicYear}", 30f, 54f, textPaint)
        canvas.drawText("Periode: ${DateUtils.formatToMonthYear(financeSummary.yearMonth)} (${financeSummary.workingDaysCount} Hari Kerja - Sen s.d. Jum)", 30f, 72f, textPaint)

        // Date right top
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 9f
        canvas.drawText("Dicetak: ${DateUtils.formatToShortDate(DateUtils.getTodayIso())}", (pageWidth - 30).toFloat(), 35f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        // Financial Summary Cards Box
        var curY = 115f
        val cardWidth = (pageWidth - 60 - 20) / 3f
        val cardHeight = 52f

        // Card 1: Target
        drawMetricCard(canvas, 30f, curY, cardWidth, cardHeight, "TARGET KAS", FormatUtils.formatRupiah(financeSummary.monthlyTargetTotal), Color.rgb(235, 243, 240), Color.rgb(15, 60, 50))
        // Card 2: Terkumpul
        drawMetricCard(canvas, 30f + cardWidth + 10f, curY, cardWidth, cardHeight, "TERKUMPUL", FormatUtils.formatRupiah(financeSummary.monthlyCollectedTotal), Color.rgb(230, 248, 238), Color.rgb(0, 140, 70))
        // Card 3: Saldo Bersih
        drawMetricCard(canvas, 30f + (cardWidth + 10f) * 2, curY, cardWidth, cardHeight, "SALDO KAS BERSIH", FormatUtils.formatRupiah(financeSummary.netBalance), Color.rgb(235, 245, 255), Color.rgb(20, 80, 160))

        curY += cardHeight + 20f

        // Section Title: Tabel Pembukuan Siswa
        textPaint.color = Color.rgb(20, 35, 30)
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("REKAP PEMBAYARAN KAS SISWA (KAS: ${FormatUtils.formatRupiah(settings.dailyAmount)}/HARI)", 30f, curY, textPaint)

        curY += 10f

        // Draw Table Header
        val colNo = 30f
        val colAbsen = 55f
        val colNama = 85f
        val colGender = 250f
        val colHari = 285f
        val colBayar = 345f
        val colTunggak = 430f
        val colStatus = 505f
        val tableWidth = (pageWidth - 60).toFloat()

        paint.color = Color.rgb(235, 240, 238)
        paint.style = Paint.Style.FILL
        canvas.drawRect(30f, curY, 30f + tableWidth, curY + 22f, paint)

        textPaint.color = Color.rgb(30, 45, 40)
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val headerY = curY + 15f
        canvas.drawText("No", colNo + 5f, headerY, textPaint)
        canvas.drawText("Abs", colAbsen, headerY, textPaint)
        canvas.drawText("Nama Siswa", colNama, headerY, textPaint)
        canvas.drawText("L/P", colGender, headerY, textPaint)
        canvas.drawText("Hari", colHari, headerY, textPaint)
        canvas.drawText("Total Bayar", colBayar, headerY, textPaint)
        canvas.drawText("Tunggakan", colTunggak, headerY, textPaint)
        canvas.drawText("Status", colStatus, headerY, textPaint)

        curY += 22f

        // Draw Table Rows
        val rowHeight = 19f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 8.5f

        for (i in studentSummaries.indices) {
            val s = studentSummaries[i]

            // Check page overflow
            if (curY + rowHeight > pageHeight - 60) {
                // Footer of page
                drawPageFooter(canvas, pageNumber, pageWidth, pageHeight)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                // New page header
                curY = 40f
                paint.color = Color.rgb(235, 240, 238)
                canvas.drawRect(30f, curY, 30f + tableWidth, curY + 20f, paint)
                textPaint.color = Color.rgb(30, 45, 40)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val newHeaderY = curY + 14f
                canvas.drawText("No", colNo + 5f, newHeaderY, textPaint)
                canvas.drawText("Abs", colAbsen, newHeaderY, textPaint)
                canvas.drawText("Nama Siswa (Lanjutan)", colNama, newHeaderY, textPaint)
                canvas.drawText("L/P", colGender, newHeaderY, textPaint)
                canvas.drawText("Hari", colHari, newHeaderY, textPaint)
                canvas.drawText("Total Bayar", colBayar, newHeaderY, textPaint)
                canvas.drawText("Tunggakan", colTunggak, newHeaderY, textPaint)
                canvas.drawText("Status", colStatus, newHeaderY, textPaint)
                curY += 20f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            // Alternating row background
            if (i % 2 == 1) {
                paint.color = Color.rgb(248, 250, 249)
                paint.style = Paint.Style.FILL
                canvas.drawRect(30f, curY, 30f + tableWidth, curY + rowHeight, paint)
            }

            val textY = curY + 13f
            textPaint.color = Color.rgb(40, 50, 45)
            canvas.drawText("${i + 1}", colNo + 5f, textY, textPaint)
            canvas.drawText("${s.student.absenNumber}", colAbsen, textY, textPaint)
            canvas.drawText(truncate(s.student.name, 26), colNama, textY, textPaint)
            canvas.drawText(s.student.gender, colGender, textY, textPaint)
            canvas.drawText("${s.paidDaysCount}/${s.workingDaysCount}", colHari, textY, textPaint)
            canvas.drawText(FormatUtils.formatRupiah(s.totalPaidInMonth), colBayar, textY, textPaint)

            if (s.unpaidInMonth > 0) {
                textPaint.color = Color.rgb(190, 40, 40)
                canvas.drawText(FormatUtils.formatRupiah(s.unpaidInMonth), colTunggak, textY, textPaint)
            } else {
                textPaint.color = Color.rgb(0, 130, 60)
                canvas.drawText("Rp 0", colTunggak, textY, textPaint)
            }

            // Status Badge
            if (s.isFullyPaid) {
                textPaint.color = Color.rgb(0, 130, 60)
                canvas.drawText("✓ LUNAS", colStatus, textY, textPaint)
            } else {
                textPaint.color = Color.rgb(190, 40, 40)
                canvas.drawText("✕ BELUM", colStatus, textY, textPaint)
            }

            curY += rowHeight
        }

        // Draw Summary and Signatures on last page or new page if tight
        if (curY + 160f > pageHeight - 50) {
            drawPageFooter(canvas, pageNumber, pageWidth, pageHeight)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            curY = 40f
        }

        curY += 20f

        // Expenses Mini Table if any
        if (expenses.isNotEmpty()) {
            textPaint.color = Color.rgb(20, 35, 30)
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("RINCIAN PENGELUARAN KAS KELAS:", 30f, curY, textPaint)
            curY += 14f

            textPaint.textSize = 8.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            expenses.take(5).forEach { exp ->
                textPaint.color = Color.rgb(60, 70, 65)
                canvas.drawText("• ${DateUtils.formatToShortDate(exp.date)} - ${exp.title} (${exp.category})", 35f, curY, textPaint)
                textPaint.color = Color.rgb(180, 40, 40)
                canvas.drawText(FormatUtils.formatRupiah(exp.amount), (pageWidth - 140).toFloat(), curY, textPaint)
                curY += 13f
            }
            curY += 10f
        }

        // Signatures area
        curY += 15f
        textPaint.color = Color.rgb(40, 50, 45)
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val colSignLeft = 60f
        val colSignRight = (pageWidth - 210).toFloat()

        canvas.drawText("Mengetahui,", colSignLeft, curY, textPaint)
        canvas.drawText("Kota Pelajar, ${DateUtils.formatToShortDate(DateUtils.getTodayIso())}", colSignRight, curY, textPaint)
        curY += 14f

        canvas.drawText("Wali Kelas ${settings.className}", colSignLeft, curY, textPaint)
        canvas.drawText("Bendahara Kas Kelas", colSignRight, curY, textPaint)

        curY += 55f // Space for signature

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(settings.homeroomTeacher, colSignLeft, curY, textPaint)
        canvas.drawText(settings.treasurerName, colSignRight, curY, textPaint)

        drawPageFooter(canvas, pageNumber, pageWidth, pageHeight)
        pdfDocument.finishPage(page)

        // Save to file
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return file
    }

    private fun drawMetricCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        bgColor: Int,
        textColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = bgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 8f, 8f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.rgb(80, 95, 90)
        textPaint.textSize = 7.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 8f, y + 16f, textPaint)

        textPaint.color = textColor
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 8f, y + 36f, textPaint)
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, pageWidth: Int, pageHeight: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.GRAY
        textPaint.textSize = 8f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Halaman $pageNumber • Kas Kelas XI-F4 • Otomatis & Terverifikasi",
            pageWidth / 2f,
            (pageHeight - 20).toFloat(),
            textPaint
        )
    }

    private fun truncate(str: String, maxLen: Int): String {
        return if (str.length > maxLen) str.substring(0, maxLen - 2) + ".." else str
    }

    fun shareFile(context: Context, file: File, mimeType: String, subject: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Berikut terlampir $subject.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Bagikan $subject")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
