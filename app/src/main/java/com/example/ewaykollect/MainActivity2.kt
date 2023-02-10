package com.example.ewaykollect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity2 : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var drawer : DrawerLayout

    //for firebase authentication
    private lateinit var auth : FirebaseAuth

    private var db=Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        //find the toolbar
        var toolbar: Toolbar = findViewById(R.id.toolBar2)
        setSupportActionBar(toolbar)

        //find the drawer layout
        drawer = findViewById(R.id.drawerLyt2)

        //toogle the navDrawer using a hamburger icon.
        var toogle: ActionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        //add a toogle listener
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        //navigation view
        var nav_view: NavigationView = findViewById(R.id.nav_view)
        nav_view.setNavigationItemSelectedListener(this)


        //google firebase auth details fetch
        auth = FirebaseAuth.getInstance()

        //get name and email from previous intent as supplied by google auth.
        val displayName = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val displayImage=intent.getStringExtra("image")

        //  display the name on the nav_header.
        var navView:NavigationView=findViewById(R.id.nav_view)
        var headerview: View =navView.getHeaderView(0)
        var dispName=headerview.findViewById<TextView>(R.id.nav_name)
        dispName.setText(displayName)
        var mail=headerview.findViewById<TextView>(R.id.nav_email)
        mail.setText(email)
        var dispImage=headerview.findViewById<ImageView>(R.id.nav_image)
        Glide.with(this).load(displayImage).into(dispImage)



    }

    override fun onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.nav_account->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,AccountFragment()).commit()
            R.id.nav_notifications->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,NotificationsFragment()).commit()
            R.id.nav_profile->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,ProfileFragment()).commit()
            R.id.nav_settings->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,SettingsFragment()).commit()
            R.id.nav_FAQs->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,FAQsFragment()).commit()
            R.id.nav_logOut->{
                Firebase.auth.signOut()
                startActivity(Intent(this, UserLogin::class.java))
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}