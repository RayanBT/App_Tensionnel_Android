package com.example.apptensionnel.data

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.apptensionnel.data.models.Measurement
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportManager(private val context: Context) {

    fun exportToCSV(measurements: List<Measurement>) {
        val fileName = "Rapport_Tension_${getCurrentDate()}.csv"
        val csvHeader = "ID,Date,Heure,Systolique (mmHg),Diastolique (mmHg),Pouls (bpm),Notes\n"
        val csvContent = StringBuilder(csvHeader)

        measurements.forEach { m ->
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(m.date))
            val time = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(m.date))
            csvContent.append("${m.id},$date,$time,${m.systolic},${m.diastolic},${m.pulse},\"${m.notes}\"\n")
        }

        saveAndShareFile(fileName, csvContent.toString().toByteArray(), "text/csv")
    }

    fun exportToPDF(measurements: List<Measurement>) {
        val pdfDocument = PdfDocument()
        
        // Configuration des paints
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0D47A1")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val tableHeaderPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val bgPaint = Paint()

        val preferenceManager = PreferenceManager(context)
        val profileName = preferenceManager.getCurrentProfile()?.name ?: "Utilisateur"
        val generationDate = SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRENCH).format(Date())

        // Calcul des moyennes
        val hasData = measurements.isNotEmpty()
        val avgSys = if (hasData) measurements.map { it.systolic }.average().toInt() else 0
        val avgDia = if (hasData) measurements.map { it.diastolic }.average().toInt() else 0
        val avgPulse = if (hasData) measurements.map { it.pulse }.average().toInt() else 0

        // Pagination
        val itemsPerPage = 25
        val chunks = measurements.chunked(itemsPerPage)
        val totalPages = if (chunks.isEmpty()) 1 else chunks.size

        if (chunks.isEmpty()) {
            drawPage(pdfDocument, 1, totalPages, emptyList(), profileName, generationDate, avgSys, avgDia, avgPulse, titlePaint, headerPaint, tableHeaderPaint, textPaint, bgPaint)
        } else {
            chunks.forEachIndexed { index, pageItems ->
                drawPage(pdfDocument, index + 1, totalPages, pageItems, profileName, generationDate, avgSys, avgDia, avgPulse, titlePaint, headerPaint, tableHeaderPaint, textPaint, bgPaint)
            }
        }

        val fileName = "Rapport_Tension_${getCurrentDate()}.pdf"
        val file = File(getReportsDir(), fileName)
        
        try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
        }

        shareFile(file, "application/pdf")
    }

    private fun drawPage(
        pdfDocument: PdfDocument,
        pageNumber: Int,
        totalPages: Int,
        items: List<Measurement>,
        profileName: String,
        generationDate: String,
        avgSys: Int,
        avgDia: Int,
        avgPulse: Int,
        titlePaint: Paint,
        headerPaint: Paint,
        tableHeaderPaint: Paint,
        textPaint: Paint,
        bgPaint: Paint
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        var y = 60f

        // Header
        canvas.drawText("TensioCare - Rapport Médical", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Patient: $profileName", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("Généré le: $generationDate", 40f, y, headerPaint)
        y += 40f

        // Stats (uniquement sur la première page)
        if (pageNumber == 1 && items.isNotEmpty()) {
            bgPaint.color = Color.parseColor("#F5F7FA")
            canvas.drawRect(40f, y, 555f, y + 60f, bgPaint)
            
            bgPaint.color = Color.BLACK
            bgPaint.textSize = 12f
            bgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("MOYENNES SUR LA PÉRIODE", 55f, y + 25f, bgPaint)
            
            bgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            bgPaint.textSize = 11f
            canvas.drawText("Tension: $avgSys / $avgDia mmHg", 55f, y + 45f, bgPaint)
            canvas.drawText("Pouls: $avgPulse bpm", 350f, y + 45f, bgPaint)
            y += 90f
        }

        // Tableau
        bgPaint.color = Color.parseColor("#0D47A1")
        canvas.drawRect(40f, y, 555f, y + 25f, bgPaint)
        canvas.drawText("DATE & HEURE", 50f, y + 17f, tableHeaderPaint)
        canvas.drawText("SYS/DIA", 200f, y + 17f, tableHeaderPaint)
        canvas.drawText("POULS", 300f, y + 17f, tableHeaderPaint)
        canvas.drawText("NOTES", 380f, y + 17f, tableHeaderPaint)
        y += 25f

        items.forEachIndexed { index, m ->
            if (index % 2 == 1) {
                bgPaint.color = Color.parseColor("#F9F9F9")
                canvas.drawRect(40f, y, 555f, y + 22f, bgPaint)
            }
            
            val dateTime = SimpleDateFormat("dd/MM/yy HH:mm", Locale.FRENCH).format(Date(m.date))
            canvas.drawText(dateTime, 50f, y + 15f, textPaint)
            canvas.drawText("${m.systolic} / ${m.diastolic}", 200f, y + 15f, textPaint)
            canvas.drawText("${m.pulse} bpm", 300f, y + 15f, textPaint)
            
            val note = if (m.notes.length > 30) m.notes.take(27) + "..." else m.notes
            canvas.drawText(note, 380f, y + 15f, textPaint)
            y += 22f
        }

        // Pied de page
        bgPaint.color = Color.LTGRAY
        canvas.drawLine(40f, 800f, 555f, 800f, bgPaint)
        canvas.drawText("Page $pageNumber / $totalPages", 500f, 815f, textPaint)
        canvas.drawText("Ce document est un outil de suivi personnel et ne remplace pas une consultation médicale.", 40f, 815f, textPaint)

        pdfDocument.finishPage(page)
    }

    private fun getReportsDir(): File {
        val dir = File(context.externalCacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun saveAndShareFile(fileName: String, content: ByteArray, mimeType: String) {
        val file = File(getReportsDir(), fileName)
        FileOutputStream(file).use { it.write(content) }
        shareFile(file, mimeType)
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.example.apptensionnel.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Correction cruciale : ajouter FLAG_ACTIVITY_NEW_TASK car le contexte peut être une application
        val chooser = Intent.createChooser(intent, "Partager le rapport")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
    }
}
