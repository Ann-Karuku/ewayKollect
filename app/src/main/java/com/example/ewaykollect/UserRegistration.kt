package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserRegistration : AppCompatActivity() {

    private lateinit var email : EditText
    private lateinit var phone : EditText
    private lateinit var password: EditText
    private lateinit var passwordRpt: EditText
    private lateinit var signInBtn: Button
    private lateinit var loginLink: TextView

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        email= findViewById(R.id.edtEmail)
        phone=findViewById(R.id.edtPhone)
        password=findViewById(R.id.edtPassword)
        passwordRpt=findViewById(R.id.edtRepeatPass)
        signInBtn=findViewById(R.id.signUpBtn)
        loginLink=findViewById(R.id.loginLink)

        // Initialize Firebase Auth
        auth = Firebase.auth

        loginLink.setOnClickListener {
            val intent= Intent(this,UserLogin::class.java)
            startActivity(intent)
        }

        signInBtn.setOnClickListener{
            perfomAuth()
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }



    }

    private fun perfomAuth() {
        var email: String = email.text.toString()
//      var phone :Int= phone.
        var password: String = password.text.toString()
        var passwordRpt: String = passwordRpt.text.toString()

        if (password===passwordRpt) {

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this,"Registration success..",Toast.LENGTH_LONG,)

                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(this,"Authentication failed.",Toast.LENGTH_LONG,)

                    }
                }
        }
    }

}