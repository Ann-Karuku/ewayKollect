package com.example.ewaykollect

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AccountFragment : Fragment() {
    private lateinit var acc_image :ImageView
    private lateinit var acc_name : TextView
    private lateinit var acc_email :TextView

    private var db= Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var root:View=inflater.inflate(R.layout.fragment_account, container, false)

        acc_image=  root.findViewById<ImageView>(R.id.acc_image)
        acc_image.setOnClickListener{
            Navigation.findNavController(root).navigate(R.id.action_accountFragment_to_profileFragment)
        }

        val userID= FirebaseAuth.getInstance().currentUser!!.uid

        val ref=db.collection("user").document(userID)

        ref.get().addOnSuccessListener {
            if(it!=null){
                val name= it.data?.get("name").toString()
                val email= it.data?.get("email").toString()
                val image=it.data?.get("image").toString()



                var dpName=root.findViewById<TextView>(R.id.acc_name)
                dpName.setText(name)
                var dpmail=root.findViewById<TextView>(R.id.acc_email)
                dpmail.setText(email)
                Glide.with(this.context).load(image).into(acc_image)
            }
        }
            .addOnFailureListener{
                Toast.makeText(this.context, "Failed to load data", Toast.LENGTH_LONG).show()
            }

        return root

    }

}