package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class UserLogin : AppCompatActivity() {

    private lateinit var uname : EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var registerLink: TextView

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //splash screen delay for 3 seconds
        Thread.sleep(1000)
        //install the splash screen to the main activity on launch
        installSplashScreen()

        setContentView(R.layout.activity_user_login)


        uname= findViewById(R.id.edtUname)
        password=findViewById(R.id.edtPassword)
        signUpBtn=findViewById(R.id.signUpBtn)
        registerLink=findViewById(R.id.regLink)


        registerLink.setOnClickListener{
            val intent=Intent(this,UserRegistration::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener{
            perfomAuth()
            val intent=Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }

    private fun perfomAuth() {

    }
}