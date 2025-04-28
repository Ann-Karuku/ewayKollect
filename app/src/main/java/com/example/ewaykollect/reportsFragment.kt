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

    private fun updateSubmissions() {
        submissionsAdapter.updateData(submissions.sortedByDescending { it.timestamp })
        if (submissions.isEmpty()) {
            Toast.makeText(context, "No submissions found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generatePdfReport() {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 14f }

        var y = 50f
        canvas.drawText("E-WayKollect Report", 50f, y, paint)
        y += 30f
        canvas.drawText("E-waste Items: $ewasteCount", 50f, y, paint)
        y += 20f
        canvas.drawText("Submissions:", 50f, y, paint)
        y += 20f
        submissions.sortedByDescending { it.timestamp }.take(10).forEach { submission ->
            canvas.drawText("${submission.title}", 50f, y, paint)
            y += 15f
        }

        pdfDocument.finishPage(page)

        try {
            val fileName = "EwayKollect_Report_${System.currentTimeMillis()}.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}

data class SubmissionItem(
    val title: String,
    val description: String,
    val timestamp: Long
)