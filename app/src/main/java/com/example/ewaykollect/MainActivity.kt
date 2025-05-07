package com.example.ewaykollect

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
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
        if (auth.currentUser == null) {
            startActivity(Intent(this, UserLogin::class.java))
            finish()
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

        // Set up AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.accountFragment, R.id.profileFragment, R.id.settingsFragment),
            drawer
        )

        // Set up Navigation View with NavController
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        // Fetch Firebase auth details
        loadUserDetails(navView)

        // Update ActionBar title and navigation icon based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.accountFragment -> supportActionBar?.title = "My Account"
                R.id.myEwaste -> supportActionBar?.title = "My E-Waste"
                R.id.profileFragment -> supportActionBar?.title = "Profile"
                R.id.settingsFragment -> supportActionBar?.title = "Settings"
                R.id.notificationsFragment -> supportActionBar?.title = "Notifications"
                R.id.FAQsFragment -> supportActionBar?.title = "FAQs"
                R.id.addEwasteDialogFragment -> supportActionBar?.title = "Add E-Waste"
                R.id.editProfileFragment -> supportActionBar?.title = "Edit Profile"
                R.id.changePasswordFragment -> supportActionBar?.title = "Change Password"
                R.id.recyclersFragment -> supportActionBar?.title = "Recyclers"
                R.id.reportsFragment->supportActionBar?.title="Reports"
                R.id.infoFragment->supportActionBar?.title="Info Center"
                R.id.supportFragment->supportActionBar?.title="Live Chat"
                else -> supportActionBar?.title = "EwayKollect"
            }
            // Set navigation icon based on destination
            if (appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                toolbar.setNavigationIcon(R.drawable.ic_menu) // Drawer icon
            } else {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back) // Back arrow
            }
        }

        // Custom navigation click listener
        toolbar.setNavigationOnClickListener {
            val currentDestination = navController.currentDestination?.id
            if (appBarConfiguration.topLevelDestinations.contains(currentDestination)) {
               if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    drawer.openDrawer(GravityCompat.START)
                }
            } else {
                if (!navController.popBackStack()) {
                    navController.navigate(R.id.accountFragment)
                }
            }
        }

        // Initialize Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun loadUserDetails(navView: NavigationView) {
        val userID = auth.currentUser?.uid
        userID?.let {
            val ref = db.collection("user").document(it)
            ref.get().addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "No Name"
                    val email = document.getString("email") ?: "No Email"
                    val image = document.getString("image")

                    val headerView: View = navView.getHeaderView(0)
                    val dpName = headerView.findViewById<TextView>(R.id.nav_name)
                    val dpMail = headerView.findViewById<TextView>(R.id.nav_email)
                    val dpImage = headerView.findViewById<ImageView>(R.id.nav_image)

                    dpName.text = name
                    dpMail.text = email
                    if (!image.isNullOrEmpty()) {
                        Glide.with(this).load(image).into(dpImage)
                    } else {
                        dpImage.setImageResource(R.drawable.icon)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.accountFragment -> {
                navController.navigate(R.id.accountFragment)
            }
            R.id.profileFragment -> {
                navController.navigate(R.id.profileFragment)
            }
            R.id.settingsFragment -> {
                navController.navigate(R.id.settingsFragment)
            }
            R.id.FAQsFragment -> {
                navController.navigate(R.id.FAQsFragment)
            }
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

    override fun onBackPressed() {
       if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (!navController.popBackStack()) {
                super.onBackPressed()
            }
        }
    }
}