package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectOrUploadEwasteFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var ewasteAdapter: EwasteAdapter
    private var companyId: String = ""
    private var companyName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_or_upload_ewaste, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        companyId = arguments?.getString("COMPANY_ID") ?: run {
            Toast.makeText(context, "Error: No company data", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return null
        }
        companyName = arguments?.getString("COMPANY_NAME") ?: ""

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.ewasteRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ewasteAdapter = EwasteAdapter(emptyList()) { ewasteItem ->
            savePickupRequest(ewasteItem)
        }
        recyclerView.adapter = ewasteAdapter

        // Fetch user's e-waste items
        fetchEwasteItems()

        // Upload new e-waste button
        view.findViewById<Button>(R.id.uploadNewEwasteButton).setOnClickListener {
            val bundle = bundleOf("COMPANY_ID" to companyId, "COMPANY_NAME" to companyName)
            findNavController().navigate(R.id.action_selectOrUploadEwasteFragment_to_addEwasteDialogFragment, bundle)
        }

        return view
    }

    private fun fetchEwasteItems() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Please log in to view your items", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("EwasteItems")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching items: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val ewasteList = snapshot?.documents?.mapNotNull { doc ->
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val number = doc.get("number")?.toString() ?: "0"
                    val state = doc.getString("state") ?: return@mapNotNull null
                    val type = doc.getString("type") ?: return@mapNotNull null
                    EwasteItem(imageUrl, name, number, state, type)
                } ?: emptyList()

                ewasteAdapter.updateData(ewasteList)
            }
    }

    private fun savePickupRequest(ewasteItem: EwasteItem) {
        val userId = auth.currentUser?.uid ?: return
        val quantity = ewasteItem.number.toIntOrNull() ?: run {
            Toast.makeText(context, "Invalid quantity format", Toast.LENGTH_SHORT).show()
            return
        }

        val pickupRequest = hashMapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "ewasteName" to ewasteItem.name,
            "ewasteType" to ewasteItem.type,
            "quantity" to quantity,
            "state" to ewasteItem.state,
            "imageUrl" to ewasteItem.imageUrl,
            "userId" to userId,
            "requestTime" to System.currentTimeMillis(),
            "status" to "pending"
        )

        firestore.collection("pickupRequests")
            .add(pickupRequest)
            .addOnSuccessListener {
                Toast.makeText(context, "Pickup request submitted for $companyName", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.recyclerDetailsFragment, false)

                // Add notification
                firestore.collection("notifications").add(
                    hashMapOf(
                        "userId" to userId,
                        "type" to "pickup_request",
                        "message" to "New pickup request for ${ewasteItem.name}",
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}