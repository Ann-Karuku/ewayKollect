package com.example.ewaykollect

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var submissionsAdapter: SubmissionAdapter
    private var submissions = mutableListOf<SubmissionItem>()
    private var ewasteCount = 0
    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_reports, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(context, "Please sign in", Toast.LENGTH_SHORT).show()
            return null
        }

        val statCard1 = view.findViewById<View>(R.id.stat_card_1)
        val statCard2 = view.findViewById<View>(R.id.stat_card_2)
        val statCard3 = view.findViewById<View>(R.id.stat_card_3)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBadge)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSubmissions)
        val downloadButton = view.findViewById<Button>(R.id.btnDownloadReport)

        recyclerView.layoutManager = LinearLayoutManager(context)
        submissionsAdapter = SubmissionAdapter(emptyList())
        recyclerView.adapter = submissionsAdapter
        recyclerView.isNestedScrollingEnabled = false

        fetchReports(statCard1, statCard2, statCard3, progressBar)

        downloadButton.setOnClickListener { generatePdfReport() }

        return view
    }

    private fun fetchReports(
        statCard1: View,
        statCard2: View,
        statCard3: View,
        progressBar: ProgressBar
    ) {
        val userId = auth.currentUser?.uid ?: return
        submissions.clear()

        // Fetch EwasteItems
        firestore.collection("EwasteItems")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                ewasteCount = snapshot.documents.size

                statCard1.findViewById<TextView>(R.id.textStatTitle).text = getString(R.string.e_waste_items)
                statCard1.findViewById<TextView>(R.id.textStatValue).text = ewasteCount.toString()
                updateSubmissions()

                val progress = (ewasteCount % 10) * 10
                progressBar.progress = progress

                // Badge handling
                val newBadgeCount = ewasteCount / 10
                val badgeTextView = view.findViewById<TextView>(R.id.textBadgeCount)
                if (badgeTextView != null) {
                    badgeTextView.text = "Badges: $newBadgeCount"
                }
                firestore.collection("users").document(userId)
                    .collection("stats").document("badges")
                    .get()
                    .addOnSuccessListener { doc ->
                        val oldBadgeCount = doc.getLong("badgeCount")?.toInt() ?: 0
                        if (newBadgeCount > oldBadgeCount) {
                            firestore.collection("users").document(userId)
                                .collection("stats").document("badges")
                                .set(mapOf("badgeCount" to newBadgeCount))
                            AlertDialog.Builder(requireContext())
                                .setTitle("New Badge!")
                                .setMessage("You earned a badge for $ewasteCount e-waste items!")
                                .setPositiveButton("OK") { _, _ -> }
                                .show()
                        }
                    }

            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching e-waste: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Fetch pickupRequests
        firestore.collection("pickupRequests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val totalRequests = snapshot.documents.size
                val completedRequests = snapshot.documents.count { it.getString("status") == "completed" }

                statCard2.findViewById<TextView>(R.id.textStatTitle).text = getString(R.string.pickup_requests)
                statCard2.findViewById<TextView>(R.id.textStatValue).text = totalRequests.toString()

                statCard3.findViewById<TextView>(R.id.textStatTitle).text = getString(R.string.completed_pickups)
                statCard3.findViewById<TextView>(R.id.textStatValue).text = completedRequests.toString()

                snapshot.documents.forEach { doc ->
                    submissions.add(
                        SubmissionItem(
                            title = "Pickup: ${doc.getString("ewasteName") ?: "Unknown"}",
                            description = "Company: ${doc.getString("companyName") ?: "N/A"}, Status: ${doc.getString("status") ?: "N/A"}",
                            timestamp = doc.getLong("requestTime") ?: 0
                        )
                    )
                }
                updateSubmissions()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching pickups: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSubmissions() {submissionsAdapter.updateData(submissions.sortedByDescending { it.timestamp })
        val noSubmissionsText = view.findViewById<TextView>(R.id.textNoSubmissions)
        if (noSubmissionsText != null) {
            noSubmissionsText.visibility = if (submissions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun generatePdfReport() {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        var pageNumber = 1
        var page = pdfDocument.startPage(
            android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        )
        var canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 14f }
        var y = 50f
        val maxY = 792f // Page height - bottom margin (842 - 50)
        val lineHeight = 20f

        // Helper function to start a new page if needed
        fun startNewPage() {
            pdfDocument.finishPage(page)
            pageNumber++
            page = pdfDocument.startPage(
                android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            )
            canvas = page.canvas
            y = 50f
        }

        // Title
        canvas.drawText("E-WayKollect Report", 50f, y, paint)
        y += lineHeight * 2

        // User Info
        val userName = auth.currentUser?.displayName ?: "Unknown User"
        canvas.drawText("User ID: $userName", 50f, y, paint)
        y += lineHeight
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Generated: $date", 50f, y, paint)
        y += lineHeight * 2

        // E-waste Items Section
        canvas.drawText("E-waste Items (Total: $ewasteCount)", 50f, y, paint)
        y += lineHeight

        val userId = auth.currentUser?.uid ?: return
        firestore.collection("EwasteItems")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val pickupTasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
                snapshot.documents.forEach { doc ->
                    if (y > maxY - lineHeight * 4) startNewPage()
                    val name = doc.getString("name") ?: "Unknown"
                    val type = doc.getString("type") ?: "N/A"
                    val quantity = doc.get("number")?.toString() ?: "0"
                    val state = doc.getString("state") ?: "N/A"
                    val timestamp = doc.getLong("timestamp")?.let {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "N/A"

                    canvas.drawText("Item: $name", 50f, y, paint)
                    y += lineHeight
                    canvas.drawText("Type: $type, Quantity: $quantity, State: $state", 60f, y, paint)
                    y += lineHeight
                    canvas.drawText("Added: $timestamp", 60f, y, paint)
                    y += lineHeight

                    // Fetch associated pickup requests
                    val ewasteId = doc.id
                    val pickupTask = firestore.collection("pickupRequests")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("ewasteId", ewasteId)
                        .get()
                        .addOnSuccessListener { pickupSnapshot ->
                            if (pickupSnapshot.documents.isNotEmpty()) {
                                if (y > maxY - lineHeight * 3) startNewPage()
                                canvas.drawText("Pickup Requests for $name:", 60f, y, paint)
                                y += lineHeight
                                pickupSnapshot.documents.forEach { pickupDoc ->
                                    if (y > maxY - lineHeight * 3) startNewPage()
                                    val pickupName = pickupDoc.getString("ewasteName") ?: "Unknown"
                                    val company = pickupDoc.getString("companyName") ?: "N/A"
                                    val status = pickupDoc.getString("status") ?: "N/A"
                                    val requestTime = pickupDoc.getLong("requestTime")?.let {
                                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it))
                                    } ?: "N/A"

                                    canvas.drawText("Pickup: $pickupName, Company: $company", 70f, y, paint)
                                    y += lineHeight
                                    canvas.drawText("Status: $status, Requested: $requestTime", 70f, y, paint)
                                    y += lineHeight
                                }
                            }
                            y += lineHeight
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error fetching pickups: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    pickupTasks.add(pickupTask)
                }

                // Wait for all pickup tasks to complete
                com.google.android.gms.tasks.Tasks.whenAll(pickupTasks)
                    .addOnSuccessListener {
                        // Continental Impact Section
                        if (y > maxY - lineHeight * 6) startNewPage()
                        canvas.drawText("Impact on the Continent", 50f, y, paint)
                        y += lineHeight
                        val impactText = "Your e-waste recycling efforts contribute to a sustainable Africa by reducing " +
                                "landfill waste, preventing toxic materials from polluting the environment, " +
                                "and conserving valuable resources like metals and plastics. " +
                                "Each item recycled supports local economies through responsible e-waste management and " +
                                "promotes a circular economy across the continent."
                        // Manual text wrapping (from previous fix)
                        val maxLineLength = 80
                        val words = impactText.split(" ")
                        val lines = mutableListOf<String>()
                        var currentLine = StringBuilder()
                        for (word in words) {
                            if (currentLine.length + word.length + 1 > maxLineLength) {
                                lines.add(currentLine.toString())
                                currentLine = StringBuilder(word)
                            } else {
                                if (currentLine.isNotEmpty()) currentLine.append(" ")
                                currentLine.append(word)
                            }
                        }
                        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                        lines.forEach { line ->
                            if (y > maxY - lineHeight) startNewPage()
                            canvas.drawText(line, 50f, y, paint)
                            y += lineHeight
                        }

                        // Finalize PDF
                        pdfDocument.finishPage(page)
                        try {
                            val fileName = "EwayKollect_Report_${System.currentTimeMillis()}.pdf"
                            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                            file.parentFile?.mkdirs()
                            pdfDocument.writeTo(FileOutputStream(file))
                            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            pdfDocument.close()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error completing pickup requests: ${e.message}", Toast.LENGTH_SHORT).show()
                        pdfDocument.finishPage(page)
                        pdfDocument.close()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching e-waste: ${e.message}", Toast.LENGTH_SHORT).show()
                pdfDocument.finishPage(page)
                pdfDocument.close()
            }
    }
}

data class SubmissionItem(
    val title: String,
    val description: String,
    val timestamp: Long
)