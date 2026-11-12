package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.CustomerLedgerSummary
import com.example.data.model.PaymentTransaction
import com.example.data.model.RateUnit
import com.example.data.model.WorkEntry
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val currencyFormat = DecimalFormat("##,##,##0.##")

    fun formatCurrency(amount: Double): String {
        return "₹${currencyFormat.format(amount)}"
    }

    fun formatDuration(totalMinutes: Int, isHindi: Boolean): String {
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 && mins > 0 -> {
                if (isHindi) "$hours घंटा $mins मिनट" else "${hours}h ${mins}m"
            }
            hours > 0 -> {
                if (isHindi) "$hours घंटा" else "${hours}h"
            }
            else -> {
                if (isHindi) "$mins मिनट" else "${mins}m"
            }
        }
    }

    fun formatDate(millis: Long, isHindi: Boolean): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", if (isHindi) Locale("hi", "IN") else Locale.ENGLISH)
        return sdf.format(Date(millis))
    }

    fun formatDateWithDay(millis: Long, isHindi: Boolean): String {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", if (isHindi) Locale("hi", "IN") else Locale.ENGLISH)
        return sdf.format(Date(millis))
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun calculateTotalAmount(durationMinutes: Int, rate: Double, rateUnit: RateUnit): Double {
        return when (rateUnit) {
            RateUnit.PER_MINUTE -> (durationMinutes * rate)
            RateUnit.PER_HOUR -> ((durationMinutes.toDouble() / 60.0) * rate)
            RateUnit.FIXED -> rate
        }
    }

    fun buildWorkReceiptText(
        entry: WorkEntry,
        ownerName: String,
        tractorModel: String,
        isHindi: Boolean
    ): String {
        val durationStr = formatDuration(entry.durationMinutes, isHindi)
        val dateStr = formatDate(entry.workDateMillis, isHindi)
        val totalStr = formatCurrency(entry.totalAmount)
        val paidStr = formatCurrency(entry.amountPaid)
        val pendingStr = formatCurrency(entry.amountPending)

        return if (isHindi) {
            """
            🌾 *ट्रैक्टर काम की रसीद - Tractor Hisab* 🌾
            ━━━━━━━━━━━━━━━━━━
            👤 *ग्राहक:* ${entry.customerName}
            📅 *दिनांक:* $dateStr
            🚜 *काम का प्रकार:* ${entry.categoryName}
            ⏱️ *काम का समय:* $durationStr (${entry.durationMinutes} मिनट)
            💰 *दर:* ₹${entry.rate} / ${entry.getParsedUnit().labelHi}
            ──────────────────
            💵 *कुल बिल:* $totalStr
            ✅ *जमा राशि:* $paidStr
            ⚠️ *बाकी बकाया:* $pendingStr
            ━━━━━━━━━━━━━━━━━━
            ${if (entry.notes.isNotBlank()) "📝 *नोट:* ${entry.notes}\n" else ""}${if (ownerName.isNotBlank()) "🚜 *मालिक:* $ownerName" else ""}${if (tractorModel.isNotBlank()) " ($tractorModel)" else ""}
            _Tractor Hisab ऐप द्वारा सुरक्षित हिसाब_
            """.trimIndent()
        } else {
            """
            🌾 *Tractor Work Receipt - Tractor Hisab* 🌾
            ━━━━━━━━━━━━━━━━━━
            👤 *Customer:* ${entry.customerName}
            📅 *Date:* $dateStr
            🚜 *Work Type:* ${entry.categoryName}
            ⏱️ *Duration:* $durationStr (${entry.durationMinutes} mins)
            💰 *Rate:* ₹${entry.rate} / ${entry.getParsedUnit().labelEn}
            ──────────────────
            💵 *Total Bill:* $totalStr
            ✅ *Paid:* $paidStr
            ⚠️ *Pending Balance:* $pendingStr
            ━━━━━━━━━━━━━━━━━━
            ${if (entry.notes.isNotBlank()) "📝 *Notes:* ${entry.notes}\n" else ""}${if (ownerName.isNotBlank()) "🚜 *Owner:* $ownerName" else ""}${if (tractorModel.isNotBlank()) " ($tractorModel)" else ""}
            _Generated via Tractor Hisab App_
            """.trimIndent()
        }
    }

    fun buildCustomerStatementText(
        summary: CustomerLedgerSummary,
        entries: List<WorkEntry>,
        payments: List<PaymentTransaction>,
        ownerName: String,
        isHindi: Boolean
    ): String {
        val sb = StringBuilder()
        if (isHindi) {
            sb.appendLine("📋 *ग्राहक खाता विवरणी (Customer Statement)*")
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("👤 *नाम:* ${summary.customer.name}")
            if (summary.customer.phone.isNotBlank()) sb.appendLine("📞 *मोबाइल:* ${summary.customer.phone}")
            if (summary.customer.village.isNotBlank()) sb.appendLine("🏡 *गांव:* ${summary.customer.village}")
            sb.appendLine("⏱️ *कुल काम समय:* ${formatDuration(summary.totalWorkMinutes, true)}")
            sb.appendLine("💵 *कुल काम बिल:* ${formatCurrency(summary.totalBillAmount)}")
            sb.appendLine("✅ *कुल जमा:* ${formatCurrency(summary.totalPaidAmount)}")
            sb.appendLine("⚠️ *कुल बाकी बकाया:* ${formatCurrency(summary.remainingBalance)}")
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("📜 *हालिया काम:*")
            entries.take(5).forEach {
                sb.appendLine("• ${it.categoryName} | ${formatDuration(it.durationMinutes, true)} | ${formatCurrency(it.totalAmount)}")
            }
            if (ownerName.isNotBlank()) {
                sb.appendLine("──────────────────")
                sb.appendLine("🚜 *संपर्क:* $ownerName")
            }
        } else {
            sb.appendLine("📋 *Customer Account Statement*")
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("👤 *Customer:* ${summary.customer.name}")
            if (summary.customer.phone.isNotBlank()) sb.appendLine("📞 *Phone:* ${summary.customer.phone}")
            if (summary.customer.village.isNotBlank()) sb.appendLine("🏡 *Village:* ${summary.customer.village}")
            sb.appendLine("⏱️ *Total Duration:* ${formatDuration(summary.totalWorkMinutes, false)}")
            sb.appendLine("💵 *Total Bill:* ${formatCurrency(summary.totalBillAmount)}")
            sb.appendLine("✅ *Total Paid:* ${formatCurrency(summary.totalPaidAmount)}")
            sb.appendLine("⚠️ *Net Balance Due:* ${formatCurrency(summary.remainingBalance)}")
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("📜 *Recent Works:*")
            entries.take(5).forEach {
                sb.appendLine("• ${it.categoryName} | ${formatDuration(it.durationMinutes, false)} | ${formatCurrency(it.totalAmount)}")
            }
            if (ownerName.isNotBlank()) {
                sb.appendLine("──────────────────")
                sb.appendLine("🚜 *Contact:* $ownerName")
            }
        }
        return sb.toString().trim()
    }

    fun shareText(context: Context, text: String, title: String = "Share Bill") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun generateAndSharePdfInvoice(
        context: Context,
        entry: WorkEntry,
        ownerName: String,
        tractorModel: String,
        ownerPhone: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1B5E20")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Top Header Banner
        paint.color = Color.parseColor("#1B5E20")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("TRACTOR HISAB", 40f, 45f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        canvas.drawText("Tractor Work Bill & Receipt", 40f, 70f, paint)

        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(entry.workDateMillis))
        canvas.drawText("Date: $dateStr", 380f, 60f, paint)

        var y = 130f

        // Owner Info Box
        paint.color = Color.parseColor("#F1F8E9")
        canvas.drawRect(40f, y, 555f, y + 60f, paint)

        canvas.drawText("Owner: ${if (ownerName.isBlank()) "Tractor Owner" else ownerName}", 55f, y + 25f, boldPaint)
        canvas.drawText("Tractor: ${if (tractorModel.isBlank()) "Standard Agricultural Tractor" else tractorModel}", 55f, y + 45f, textPaint)
        if (ownerPhone.isNotBlank()) {
            canvas.drawText("Contact: $ownerPhone", 360f, y + 25f, textPaint)
        }

        y += 85f

        // Customer Details
        canvas.drawText("CUSTOMER DETAILS", 40f, y, headerPaint.apply { textSize = 16f })
        y += 25f
        canvas.drawText("Customer Name: ${entry.customerName}", 40f, y, boldPaint)
        y += 20f
        canvas.drawText("Invoice ID: #TRK-${entry.id}", 40f, y, textPaint)

        y += 35f

        // Table Header
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(40f, y, 555f, y + 30f, paint)
        canvas.drawText("Work Description", 50f, y + 20f, boldPaint)
        canvas.drawText("Duration", 260f, y + 20f, boldPaint)
        canvas.drawText("Rate", 370f, y + 20f, boldPaint)
        canvas.drawText("Amount", 480f, y + 20f, boldPaint)

        y += 30f

        // Table Row
        paint.color = Color.WHITE
        canvas.drawRect(40f, y, 555f, y + 40f, paint)
        canvas.drawText(entry.categoryName, 50f, y + 25f, textPaint)
        canvas.drawText("${entry.durationMinutes} mins (${formatDuration(entry.durationMinutes, false)})", 260f, y + 25f, textPaint)
        canvas.drawText("₹${entry.rate} / ${entry.getParsedUnit().shortUnitEn}", 370f, y + 25f, textPaint)
        canvas.drawText(formatCurrency(entry.totalAmount), 480f, y + 25f, boldPaint)

        // Divider
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y + 40f, 555f, y + 40f, paint)

        y += 65f

        // Financial Summary Box
        paint.color = Color.parseColor("#FAFAFA")
        canvas.drawRect(300f, y, 555f, y + 110f, paint)

        canvas.drawText("Total Bill Amount:", 315f, y + 28f, boldPaint)
        canvas.drawText(formatCurrency(entry.totalAmount), 460f, y + 28f, boldPaint)

        canvas.drawText("Amount Received:", 315f, y + 55f, textPaint)
        canvas.drawText(formatCurrency(entry.amountPaid), 460f, y + 55f, textPaint)

        val balanceColor = if (entry.amountPending > 0) Color.parseColor("#C62828") else Color.parseColor("#2E7D32")
        val balPaint = Paint().apply {
            color = balanceColor
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Balance Pending:", 315f, y + 85f, balPaint)
        canvas.drawText(formatCurrency(entry.amountPending), 460f, y + 85f, balPaint)

        y += 140f
        if (entry.notes.isNotBlank()) {
            canvas.drawText("Notes: ${entry.notes}", 40f, y, textPaint)
            y += 30f
        }

        // Signature section
        canvas.drawLine(40f, y + 70f, 180f, y + 70f, paint)
        canvas.drawText("Customer Signature", 50f, y + 90f, textPaint)

        canvas.drawLine(380f, y + 70f, 520f, y + 70f, paint)
        canvas.drawText("Tractor Owner Signature", 380f, y + 90f, textPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "invoice_tractor_${entry.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            // Open share intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Tractor Bill #${entry.id} - ${entry.customerName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Invoice"))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
