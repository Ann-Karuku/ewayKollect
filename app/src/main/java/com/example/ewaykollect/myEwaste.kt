package com.example.ewaykollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyEwasteFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root: View = inflater.inflate(R.layout.fragment_my_ewaste, container, false)
        // Find the floating action button
        val fabAddItem = root.findViewById<FloatingActionButton>(R.id.fabAddItem)

        // Set an OnClickListener to navigate to the AddEwasteDialogFragment
        fabAddItem.setOnClickListener {
            // Use NavController to navigate to the desired fragment
            findNavController().navigate(R.id.action_myEwaste_to_addEwasteDialogFragment)
        }


        return root
    }



}
