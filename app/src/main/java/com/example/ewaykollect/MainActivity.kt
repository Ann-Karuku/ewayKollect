package com.example.ewaykollect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var drawer : DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.CustomTheme)

        //find the toolbar
        var toolbar: Toolbar =findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)

        //find the drawer layout
        drawer=findViewById(R.id.drawerLyt)

        //toogle the navDrawer using a hamburger icon.
        var toogle: ActionBarDrawerToggle =ActionBarDrawerToggle(this,drawer,toolbar,
            R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        //add a toogle listener
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        //navigation view
        var nav_view: NavigationView= findViewById(R.id.nav_view)
        nav_view.setNavigationItemSelectedListener (this)

    }

    override fun onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

//        when (item.itemId){
//            R.id.nav_chat->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
//                ,chatFragment()).commit()
//            R.id.nav_profile->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
//                ,profileFragment()).commit()
//            R.id.nav_settings->getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
//                ,profileFragment()).commit()
//        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}