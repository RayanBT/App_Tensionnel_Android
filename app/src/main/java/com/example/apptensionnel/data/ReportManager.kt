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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0D47A1") // AppBlue
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

        var y = 60f

        // --- EN-TÊTE DU RAPPORT ---
        canvas.drawText("TensioCare - Rapport Médical", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Patient: Jean-Pierre Martin", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("Généré le: ${SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRENCH).format(Date())}", 40f, y, headerPaint)
        y += 40f

        // --- STATISTIQUES RÉCAPITULATIVES ---
        if (measurements.isNotEmpty()) {
            val avgSys = measurements.map { it.systolic }.average().toInt()
            val avgDia = measurements.map { it.diastolic }.average().toInt()
            val avgPulse = measurements.map { it.pulse }.average().toInt()

            paint.color = Color.parseColor("#F5F7FA")
            canvas.drawRect(40f, y, 555f, y + 60f, paint)
            
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("MOYENNES SUR LA PÉRIODE", 55f, y + 25f, paint)
            
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 11f
            canvas.drawText("Tension: $avgSys / $avgDia mmHg", 55f, y + 45f, paint)
            canvas.drawText("Pouls: $avgPulse bpm", 350f, y + 45f, paint)
            y += 90f
        }

        // --- TABLEAU DES MESURES ---
        // Header du tableau
        paint.color = Color.parseColor("#0D47A1")
        canvas.drawRect(40f, y, 555f, y + 25f, paint)
        canvas.drawText("DATE & HEURE", 50f, y + 17f, tableHeaderPaint)
        canvas.drawText("SYS/DIA", 200f, y + 17f, tableHeaderPaint)
        canvas.drawText("POULS", 300f, y + 17f, tableHeaderPaint)
        canvas.drawText("NOTES", 380f, y + 17f, tableHeaderPaint)
        y += 25f

        // Lignes du tableau
        measurements.take(25).forEachIndexed { index, m ->
            if (index % 2 == 1) {
                paint.color = Color.parseColor("#F9F9F9")
                canvas.drawRect(40f, y, 555f, y + 22f, paint)
            }
            
            val dateTime = SimpleDateFormat("dd/MM/yy HH:mm", Locale.FRENCH).format(Date(m.date))
            canvas.drawText(dateTime, 50f, y + 15f, textPaint)
            canvas.drawText("${m.systolic} / ${m.diastolic}", 200f, y + 15f, textPaint)
            canvas.drawText("${m.pulse} bpm", 300f, y + 15f, textPaint)
            
            val note = if (m.notes.length > 30) m.notes.take(27) + "..." else m.notes
            canvas.drawText(note, 380f, y + 15f, textPaint)
            
            y += 22f
            
            // Gestion simplifiée de la pagination (limitée à 25 mesures pour cet exemple)
            if (y > 780f) return@forEachIndexed 
        }

        // Pied de page
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 800f, 555f, 800f, paint)
        canvas.drawText("Ce document est un outil de suivi personnel et ne remplace pas une consultation médicale.", 40f, 815f, textPaint)

        pdfDocument.finishPage(page)

        val fileName = "Rapport_Tension_${getCurrentDate()}.pdf"
        val file = File(getReportsDir(), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        shareFile(file, "application/pdf")
    }

    private fun getReportsDir(): File {
        val dir = File(context.cacheDir, "reports")
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
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Partager le rapport"))
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
    }
}
