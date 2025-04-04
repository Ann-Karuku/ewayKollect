package com.example.ewaykollect

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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

class UserLogin : AppCompatActivity() {

    private lateinit var email : EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var registerLink: TextView
    private lateinit var googleBtn: ImageView
    private lateinit var fbBtn: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private var db=Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen
        val splashScreen = installSplashScreen()

        // Keep the splash screen visible for 3 seconds
        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        // Delay the "loading complete" state
        Handler(Looper.getMainLooper()).postDelayed({
            isLoading = false
        }, 3000)

        setContentView(R.layout.activity_user_login)


        val receiver = connectivityReceiver()
        val networkStatus: String? = receiver.status
        val filter = IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        this.registerReceiver(connectivityReceiver(), filter)
        Toast.makeText(applicationContext, networkStatus, Toast.LENGTH_SHORT).show()

        printHashKey(applicationContext)

        email = findViewById(R.id.edtEmail)
        password = findViewById(R.id.edtPassword)
        signUpBtn = findViewById(R.id.signUpBtn)
        registerLink = findViewById(R.id.regLink)
        googleBtn = findViewById(R.id.google)
        fbBtn = findViewById(R.id.btn_facebook)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        signUpBtn.setOnClickListener {
            perfomAuth()
        }

        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterChoice::class.java)
            startActivity(intent)
        }

        //google authentication
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleBtn.setOnClickListener {
            signInGoogle()
        }


        // Initialize Facebook authentication
        callbackManager = CallbackManager.Factory.create()
        fbBtn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this, listOf("email", "public_profile")
            )
        }
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
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

    //authentication via email-password
    private fun perfomAuth() {
        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()

        if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent2 = Intent(this, MainActivity::class.java)
                        startActivity(intent2)
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect email/password", Toast.LENGTH_SHORT).show()
                    }
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


    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        this.registerReceiver(connectivityReceiver(), filter)
    }




}