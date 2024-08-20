package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_my_ewaste, container, false)

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

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firestore listener to avoid memory leaks
        firestoreListener.remove()
    }
}
