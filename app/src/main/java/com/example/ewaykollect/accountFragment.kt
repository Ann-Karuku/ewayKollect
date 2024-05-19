package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_account, container, false)

        val myEwasteItems = root.findViewById<CardView>(R.id.cdvw_my_ewaste)
        myEwasteItems.setOnClickListener {
            // Check if the current destination is AccountFragment
            if (findNavController().currentDestination?.id == R.id.accountFragment) {
                // Navigate to HomeFragment using the generated action
                findNavController().navigate(R.id.action_accountFragment_to_myEwaste)
            }
        }
        return root
    }
}