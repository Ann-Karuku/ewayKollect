package com.example.ewaykollect

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var drawer: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // ✅ Check if a user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, UserLogin::class.java))
            finish() // Close MainActivity
            return
        }

        // Initialize toolbar
        val toolbar: Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)

        // Initialize drawer layout
        drawer = findViewById(R.id.drawerLyt)

        // Initialize Navigation Component
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up ActionBar with Navigation Controller
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_account, R.id.nav_profile, R.id.nav_settings
        ), drawer)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up Navigation View with NavController
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)

        // Drawer toggle setup
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        // Initialize Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Fetch Firebase auth details
        loadUserDetails(navView)

    }

    private fun loadUserDetails(navView: NavigationView) {
        val userID = auth.currentUser?.uid
        userID?.let {
            val ref = db.collection("user").document(it)
            ref.get().addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val image = document.getString("image")

                    val headerView: View = navView.getHeaderView(0)
                    val dpName = headerView.findViewById<TextView>(R.id.nav_name)
                    val dpMail = headerView.findViewById<TextView>(R.id.nav_email)
                    val dpImage = headerView.findViewById<ImageView>(R.id.nav_image)

                    dpName.text = name
                    dpMail.text = email
                    Glide.with(this).load(image).into(dpImage)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_account -> {
                navController.navigate(R.id.accountFragment)
            }
            R.id.nav_profile -> {
                navController.navigate(R.id.profileFragment)
            }
            R.id.nav_FAQs -> {
                navController.navigate(R.id.FAQsFragment)
            }
            // Other layouts here
            R.id.nav_logOut -> {
                signOut()
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        // Sign out from Firebase
        auth.signOut()

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this) {
            if (it.isSuccessful) {
                // Redirect to login screen
                val intent = Intent(this, UserLogin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to log out from Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}
