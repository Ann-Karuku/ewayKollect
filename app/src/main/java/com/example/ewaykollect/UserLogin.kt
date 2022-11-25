package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserLogin : AppCompatActivity() {

    private lateinit var email : EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var registerLink: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //splash screen delay for 3 seconds
        Thread.sleep(1000)
        //install the splash screen to the main activity on launch
        installSplashScreen()

        setContentView(R.layout.activity_user_login)


        email= findViewById(R.id.edtEmail)
        password=findViewById(R.id.edtPassword)
        signUpBtn=findViewById(R.id.signUpBtn)
        registerLink=findViewById(R.id.regLink)

        // Initialize Firebase Auth
        auth = Firebase.auth


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
        var email: String = email.text.toString()
        var password: String = password.text.toString()


       if (email.isEmpty()||password.isEmpty()){
           Toast.makeText(this,"Please fill in all fields!", Toast.LENGTH_SHORT).show()
       }else{
           auth.signInWithEmailAndPassword(email, password)
               .addOnCompleteListener(this) { task ->
                   if (task.isSuccessful) {
                       // Sign in success, update UI with the signed-in user's information
                       Toast.makeText(this, "Login success..", Toast.LENGTH_LONG,).show()

                   } else {
                       // If sign in fails, display a message to the user.
                       Toast.makeText(this, "Login failed..", Toast.LENGTH_LONG,).show()
                   }
               }
       }
    }
}