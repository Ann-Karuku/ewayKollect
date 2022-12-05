package com.example.ewaykollect

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserLogin : AppCompatActivity() {

    private lateinit var email : EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var registerLink: TextView
    private lateinit var googleBtn: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //splash screen delay for 3 seconds
        Thread.sleep(1000)
        //install the splash screen to the main activity on launch
        installSplashScreen()

        setContentView(R.layout.activity_user_login)


        email = findViewById(R.id.edtEmail)
        password = findViewById(R.id.edtPassword)
        signUpBtn = findViewById(R.id.signUpBtn)
        registerLink = findViewById(R.id.regLink)
        googleBtn=findViewById(R.id.google)

        // Initialize Firebase Auth
        auth = Firebase.auth


        registerLink.setOnClickListener {
            val intent = Intent(this, UserRegistration::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            perfomAuth()

        }

        //google authentication
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)

        googleBtn.setOnClickListener {
            signInGoogle()
        }

    }

    //method to open home activity after google auth
    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    //variable launcher with result data
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            //get the account from intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val intent : Intent = Intent(this , MainActivity::class.java)
                intent.putExtra("email" , account.email)
                intent.putExtra("name" , account.displayName)
                intent.putExtra("image",account.photoUrl)
                startActivity(intent)
            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }

    //authentication via email-password

    private fun perfomAuth() {
        var email: String = email.text.toString()
        var password: String = password.text.toString()


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "Login success..", Toast.LENGTH_LONG).show()
                        val intent2 = Intent(this, MainActivity::class.java)
                        startActivity(intent2)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Incorrect email/password", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}