package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterChoice : AppCompatActivity() {

    private lateinit var companyBtn: Button
    private lateinit var userBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_choice)
        supportActionBar?.hide()

       companyBtn=findViewById(R.id.companyBtn)
       userBtn=findViewById(R.id.userBtn)



       companyBtn.setOnClickListener {
           val intent = Intent(this, CompanyRegistration::class.java)
           startActivity(intent)
       }
        userBtn.setOnClickListener {
            val intent = Intent(this, UserRegistration::class.java)
            startActivity(intent)
        }
    }
}