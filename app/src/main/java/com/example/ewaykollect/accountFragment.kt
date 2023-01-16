package com.example.ewaykollect

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class AccountFragment : Fragment() {
    private lateinit var profile_image :ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var root:View=inflater.inflate(R.layout.fragment_account, container, false)

      profile_image=  root.findViewById<ImageView>(R.id.profile_image)
        profile_image.setOnClickListener{

        }
        return root

    }

}