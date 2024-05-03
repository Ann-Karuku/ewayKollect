package com.example.ewaykollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

class AccountFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root:View = inflater.inflate(R.layout.fragment_account, container, false)

        // navigate to myEwaste fragment
        val myEwasteItems = root.findViewById<CardView>(R.id.cdvw_my_ewaste)
        myEwasteItems.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment2_to_homeFragment2)
        }
        return root
    }
}
