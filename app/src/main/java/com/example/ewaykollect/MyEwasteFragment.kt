package com.example.ewaykollect

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyEwasteFragment : Fragment() {

    private lateinit var ewasteAdapter: EwasteAdapter
    private lateinit var recyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var firestoreListener: ListenerRegistration
    private var selectedButton: Button? = null // To keep track of the selected button

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

        // Set the title
        (activity as AppCompatActivity).supportActionBar?.title = "My Ewaste Items"

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
        val spinnerCategories = root.findViewById<Spinner>(R.id.spnnrCategories)
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
                    cornerRadius = 12f // Adjust the radius for rounding
                    setColor(ContextCompat.getColor(requireContext(), R.color.light_green)) // Default color
                    setStroke(1, ContextCompat.getColor(requireContext(), R.color.other_green)) // Border color and width
                }
                background = drawable
                setPadding(16, 5, 16, 5)
            }
            button.setOnClickListener {
                // Deselect the previously selected button
                selectedButton?.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.light_green))
                    setStroke(1, ContextCompat.getColor(requireContext(), R.color.other_green))
                }

                // Select the current button
                button.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(ContextCompat.getColor(requireContext(), R.color.other_green)) // Active button color
                    setStroke(1, ContextCompat.getColor(requireContext(), R.color.other_green))
                }
                selectedButton = button

                // Filter and update the RecyclerView based on the selected category
                filterEwasteItemsByCategory(category)

                Toast.makeText(requireContext(), category, Toast.LENGTH_SHORT).show()
            }
            linearLytCategories.addView(button)
        }


        // If there are more than maxButtonsToShow, add the rest to the Spinner


        return root
    }
    private fun fetchEwasteItems() {
        // Reference to the Firestore collection
        val collectionRef = firestore.collection("EwasteItems")

        // Listen for changes to the collection
        firestoreListener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
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
            ewasteAdapter = EwasteAdapter(ewasteList)
            recyclerView.adapter = ewasteAdapter
        }
    }

    private fun filterEwasteItemsByCategory(category: String) {
        // Reference to the Firestore collection
        val collectionRef = firestore.collection("EwasteItems")

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
        // Remove Firestore listener to avoid memory leaks
        firestoreListener.remove()
    }
}


