package com.example.ewaykollect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //splash screen delay for 3 seconds
        Thread.sleep(1000)

        //install the splash screen to the main activity on launch
        installSplashScreen()
        setContentView(R.layout.activity_main)

    }
}