package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class UserLogin : AppCompatActivity() {

    private lateinit var uname : EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var registerLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)


        uname= findViewById(R.id.edtUname)
        password=findViewById(R.id.edtPassword)
        signUpBtn=findViewById(R.id.signUpBtn)
        registerLink=findViewById(R.id.regLink)


        registerLink.setOnClickListener{
            val intent=Intent(this,UserRegistration::class.java)
            startActivity(intent)
        }
    }
}