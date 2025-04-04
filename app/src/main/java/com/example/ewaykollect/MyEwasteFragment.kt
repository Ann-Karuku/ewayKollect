package com.example.ewaykollect

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // Categorized list of e-waste items
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
        val root: View = inflater.inflate(R.layout.fragment_my_ewaste, container, false)

        if (!::auth.isInitialized) {
            auth = FirebaseAuth.getInstance()
        }

        // Initialize RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewEwasteItems)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter with an empty list initially
        ewasteAdapter = EwasteAdapter(emptyList())
        recyclerView.adapter = ewasteAdapter

        // Fetch data from Firestore
        fetchEwasteItems()

        // Find the floating action button
        val fabAddItem = root.findViewById<FloatingActionButton>(R.id.fabAddItem)

        // Set an OnClickListener to navigate to the AddEwasteDialogFragment
        fabAddItem.setOnClickListener {
            findNavController().navigate(R.id.action_myEwaste_to_addEwasteDialogFragment)
        }

        // Adding categories buttons dynamically
        val linearLytCategories = root.findViewById<LinearLayout>(R.id.linearLytCategories)
        val maxButtonsToShow = 15

        // Add buttons to LinearLayout for the first categories
        items.take(maxButtonsToShow).forEach { category ->
            val button = Button(requireContext()).apply {
                text = category
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                // Create a ShapeDrawable programmatically for rounded corners
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.main_grey)) // Default color
                   }
                background = drawable
                setPadding(16, 1, 16, 1)
            }
            button.setOnClickListener {
                // Deselect the previously selected button
                selectedButton?.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.main_grey))
                  }

                // Select the current button
                button.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.other_green)) // Active button color
                  }
                selectedButton = button

                // Filter and update the RecyclerView based on the selected category
                filterEwasteItemsByCategory(category)
            }
            linearLytCategories.addView(button)
        }


        // If there are more than maxButtonsToShow, add the rest to the Spinner


        return root
    }
    private fun fetchEwasteItems() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to view your items", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the Firestore collection with user filter
        val collectionRef = firestore.collection("EwasteItems")
            .whereEqualTo("userId", currentUser.uid)

        // Listen for changes to the collection
        firestoreListener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Error fetching items: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            // Map Firestore documents to EwasteItem objects
            val ewasteList = snapshot?.documents?.mapNotNull { doc ->
                val imageUrl = doc.getString("imageUrl")
                val name = doc.getString("name")
                val number = doc.getString("number") ?: ""
                val state = doc.getString("state")
                val type = doc.getString("type")

                if (imageUrl != null && name != null && state != null && type != null) {
                    EwasteItem(imageUrl, name, number, state, type)
                } else {
                    null
                }
            } ?: emptyList()

            // Update the adapter with the fetched data
            ewasteAdapter.updateData(ewasteList)
        }
    }

    private fun filterEwasteItemsByCategory(category: String) {
        val currentUser = auth.currentUser
        if (currentUser==null) {
            Toast.makeText(context, "Please log in to view your items", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the Firestore collection with user and category filter
        val collectionRef = firestore.collection("EwasteItems")
            .whereEqualTo("userId", currentUser.uid)

        // Query to filter items by category
        val query = if (category == "All") {
            collectionRef
        } else {
            collectionRef.whereEqualTo("type", category)
        }

        query.get().addOnSuccessListener { snapshot ->
            val filteredEwasteList = snapshot.documents.mapNotNull { doc ->
                val imageUrl = doc.getString("imageUrl")
                val name = doc.getString("name")
                val number = doc.getString("number") ?: ""
                val state = doc.getString("state")
                val type = doc.getString("type")

                if (imageUrl != null && name != null && state != null && type != null) {
                    EwasteItem(imageUrl, name, number, state, type)
                } else {
                    null
                }
            }

            // Update the adapter with the filtered data
            ewasteAdapter = EwasteAdapter(filteredEwasteList)
            recyclerView.adapter = ewasteAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::firestoreListener.isInitialized) {
            firestoreListener.remove()
        }
    }

}


