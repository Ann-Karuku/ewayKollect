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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var prof_Logout :TextView

    private var db= Firebase.firestore

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

        val userID= FirebaseAuth.getInstance().currentUser!!.uid

        val ref=db.collection("user").document(userID)

        ref.get().addOnSuccessListener {
            if(it!=null){
                val name= it.data?.get("name").toString()
                val email= it.data?.get("email").toString()
                val image=it.data?.get("image").toString()
                val phone=it.data?.get("phone").toString()


                var dpName=root.findViewById<TextView>(R.id.prof_name)
                dpName.setText(name)
                var dpimage=root.findViewById<ImageView>(R.id.prof_image)
                Glide.with(this.context).load(image).into(dpimage)
                var dpmail=root.findViewById<TextView>(R.id.prof_email)
                dpmail.setText(email)
                var dpphone=root.findViewById<TextView>(R.id.prof_phone)
                dpphone.setText(phone)
            }
        }
            .addOnFailureListener{
                Toast.makeText(this.context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }


        return root
    }

}