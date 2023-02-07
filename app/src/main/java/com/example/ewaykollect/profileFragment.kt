package com.example.ewaykollect

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var prof_Logout :TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var root:View=inflater.inflate(R.layout.fragment_profile, container, false)
        prof_Logout=root.findViewById(R.id.prof_logout)
        prof_Logout.setOnClickListener {
            Firebase.auth.signOut()
//            startActivity(Intent(this, UserLogin::class.java))
        }
        return root
    }

}