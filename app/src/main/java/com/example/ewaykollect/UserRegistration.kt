package com.example.ewaykollect

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class UserRegistration : AppCompatActivity() {

    private lateinit var name : EditText
    private lateinit var email : EditText
    private lateinit var phone : EditText
    private lateinit var password: EditText
    private lateinit var passwordRpt: EditText
    private lateinit var signInBtn: Button
    private lateinit var loginLink: TextView
    private lateinit var googleBtn:ImageView
    private lateinit var fbBtn:ImageView
    private lateinit var callbackManager: CallbackManager

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var db=Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)
        supportActionBar?.hide()

        name=findViewById(R.id.edtName)
        email= findViewById(R.id.edtEmail)
        phone=findViewById(R.id.edtPhone)
        password=findViewById(R.id.edtPassword)
        passwordRpt=findViewById(R.id.edtRepeatPass)
        signInBtn=findViewById(R.id.signUpBtn)
        loginLink=findViewById(R.id.loginLink)
        googleBtn=findViewById(R.id.google)
        fbBtn=findViewById(R.id.facebook)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        printHashKey(applicationContext)

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        fbBtn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this, listOf("email", "public_profile")
            )
        }
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })


        loginLink.setOnClickListener {
            val intent= Intent(this,UserLogin::class.java)
            startActivity(intent)
        }

        signInBtn.setOnClickListener{
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

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userID = FirebaseAuth.getInstance().currentUser!!.uid
                val userRef = db.collection("user").document(userID)

                userRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val facebookUser = auth.currentUser
                        val userMap = hashMapOf(
                            "name" to (facebookUser?.displayName ?: "Not Updated"),
                            "email" to (facebookUser?.email ?: "Not Updated"),
                            "image" to (facebookUser?.photoUrl?.toString() ?: ""),
                            "phone" to "Not Updated",
                            "county" to "Not Updated",
                            "town" to "Not Updated"
                        )
                        db.collection("user").document(userID).set(userMap)
                    }
                }

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Facebook Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
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
                val intent= Intent(this , MainActivity::class.java)

                val userID= FirebaseAuth.getInstance().currentUser!!.uid
                val userRef = db.collection("user").document(userID)

                userRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Only save the data if the user document does not exist
                        val userMap = hashMapOf(
                            "name" to (account.displayName ?: "Not Updated"),
                            "email" to (account.email ?: "Not Updated"),
                            "image" to (account.photoUrl?.toString() ?: ""),
                            "phone" to "Not Updated",
                            "county" to "Not Updated",
                            "town" to "Not Updated"
                        )
                        db.collection("user").document(userID).set(userMap)
                    }
                }

                startActivity(intent)
            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }



    //SIGNIN WITH EMAIL AND PASSWORD
    private fun perfomAuth() {

        val person_name: String = name.text.toString().trim()
        val mail: String = email.text.toString().trim()
        val phoneNo: String = phone.text.toString().trim()
        val pass: String = password.text.toString().trim()
        val passRpt: String = passwordRpt.text.toString().trim()

        if (mail.isEmpty()||pass.isEmpty()||passRpt.isEmpty()) {

            Toast.makeText(this,"Please fill in all fields!",Toast.LENGTH_SHORT).show()

            if(TextUtils.isEmpty(mail)){
                email.setError("Email is required")
            }
            if(TextUtils.isEmpty(pass)){
                password.setError("Password is required")
            }
            if(pass.length <6){
                password.setError("Password must be more than 6 characters.")
            }

            return

        }else{
            if (pass == passRpt) {


                auth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign up success, update UI with the signed-in user's information

                           val userID= FirebaseAuth.getInstance().currentUser!!.uid

                            val userMap= hashMapOf(
                                "name" to person_name,
                                "email" to mail,
                                "phone" to phoneNo,
                                "password" to pass,
                                "image" to  "",
                                "phone" to "Not Updated",
                                "county" to "Not Updated",
                                "town" to "Not Updated"
                            )
                            db.collection("user").document(userID).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this,"Registration success",Toast.LENGTH_SHORT).show()
                                    name.text.clear()
                                    email.text.clear()
                                    phone.text.clear()
                                    password.text.clear()
                                    passwordRpt.text.clear()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(this,"Failed to register",Toast.LENGTH_SHORT).show()
                                }


                            val intent2=Intent(this,MainActivity::class.java)
                            startActivity(intent2)

                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(this, "Registration failed.", Toast.LENGTH_LONG).show()

                        }
                    }
             } else{
                // If password does not match
                Toast.makeText(this, "password mismatch", Toast.LENGTH_LONG).show()
            }
          }
        }

    //facebook login
    private fun printHashKey(context: Context) {
        try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.i("AppLog", "Key Hash: $hashKey")
            }
        } catch (e: Exception) {
            Log.e("AppLog", "Error getting hash key", e)
        }
    }



}