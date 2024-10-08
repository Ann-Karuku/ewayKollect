package com.example.ewaykollect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AccountFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_account, container, false)

        // Set the title
        (activity as AppCompatActivity).supportActionBar?.title = "My Account"

        val myEwasteItems = root.findViewById<CardView>(R.id.cdvw_my_ewaste)
        myEwasteItems.setOnClickListener {
            // Check if the current destination is AccountFragment
            if (findNavController().currentDestination?.id == R.id.accountFragment) {
                // Navigate to other EWaste Fragment using the generated action
                findNavController().navigate(R.id.action_accountFragment_to_myEwaste)
            }
        }
        val myNotifications = root.findViewById<CardView>(R.id.cdvw_my_notifications)
        myNotifications.setOnClickListener {
            // Check if the current destination is AccountFragment
            if (findNavController().currentDestination?.id == R.id.accountFragment) {
                // Navigate to other EWaste Fragment using the generated action
                findNavController().navigate(R.id.action_accountFragment_to_notificationsFragment)
            }
        }
        return root
    }
}