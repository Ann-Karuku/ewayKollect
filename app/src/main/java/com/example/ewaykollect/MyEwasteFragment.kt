package com.example.ewaykollect

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyEwasteFragment : Fragment() {

    private lateinit var ewasteAdapter: EwasteAdapter
    private lateinit var recyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var firestoreListener: ListenerRegistration
    private var selectedButton: Button? = null
    private lateinit var auth: FirebaseAuth

    private val items = arrayOf(
        "All", "Mobile Phones", "Tablets", "Laptops", "Desktop Computers",
        "Monitors", "Printers", "Televisions", "Remote Controls",
        "DVD Players", "Washing Machines", "Refrigerators", "Microwaves",
        "Air Conditioners", "Electric Fans", "Heaters"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_my_ewaste, container, false)

        auth = FirebaseAuth.getInstance()

        recyclerView = root.findViewById(R.id.recyclerViewEwasteItems)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ewasteAdapter = EwasteAdapter(emptyList())
        recyclerView.adapter = ewasteAdapter

        fetchEwasteItems()

        root.findViewById<FloatingActionButton>(R.id.fabAddItem).setOnClickListener {
            findNavController().navigate(R.id.action_myEwaste_to_addEwasteDialogFragment)
        }

        val linearLytCategories = root.findViewById<LinearLayout>(R.id.linearLytCategories)
        items.take(15).forEach { category ->
            val button = Button(requireContext()).apply {
                text = category
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 8, 8, 8) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.main_grey))
                }
                setPadding(16, 1, 16, 1)
            }
            button.setOnClickListener {
                selectedButton?.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.main_grey))
                }
                button.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.other_green))
                }
                selectedButton = button
                filterEwasteItemsByCategory(category)
            }
            linearLytCategories.addView(button)
        }

        return root
    }

    private fun fetchEwasteItems() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Please log in to view your items", Toast.LENGTH_SHORT).show()
            return
        }

        firestoreListener = firestore.collection("EwasteItems")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching items: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val ewasteList = snapshot?.documents?.mapNotNull { doc ->
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val number = doc.getString("number") ?: ""
                    val state = doc.getString("state") ?: return@mapNotNull null
                    val type = doc.getString("type") ?: return@mapNotNull null
                    EwasteItem(imageUrl, name, number, state, type)
                } ?: emptyList()

                ewasteAdapter.updateData(ewasteList)
            }
    }

    private fun filterEwasteItemsByCategory(category: String) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Please log in to view your items", Toast.LENGTH_SHORT).show()
            return
        }

        val query = if (category == "All") {
            firestore.collection("EwasteItems").whereEqualTo("userId", userId)
        } else {
            firestore.collection("EwasteItems").whereEqualTo("userId", userId).whereEqualTo("type", category)
        }

        query.get().addOnSuccessListener { snapshot ->
            val filteredEwasteList = snapshot.documents.mapNotNull { doc ->
                val imageUrl = doc.getString("imageUrl") ?: ""
                val name = doc.getString("name") ?: return@mapNotNull null
                val number = doc.getString("number") ?: ""
                val state = doc.getString("state") ?: return@mapNotNull null
                val type = doc.getString("type") ?: return@mapNotNull null
                EwasteItem(imageUrl, name, number, state, type)
            }
            ewasteAdapter.updateData(filteredEwasteList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener.remove()
    }
}