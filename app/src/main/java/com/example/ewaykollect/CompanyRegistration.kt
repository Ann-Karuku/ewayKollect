package com.example.ewaykollect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class CompanyRegistration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_registration)

        val step1 = findViewById<LinearLayout>(R.id.stepOneLayout)
        val step2 = findViewById<LinearLayout>(R.id.stepTwoLayout)
        val step3 = findViewById<LinearLayout>(R.id.stepThreeLayout)

        val nextStep1Btn = findViewById<Button>(R.id.nextStep1Btn)
        val nextStep2Btn = findViewById<Button>(R.id.nextStep2Btn)
        val signUpBtn = findViewById<Button>(R.id.signUpBtn)

        nextStep1Btn.setOnClickListener {
            // Hide step 1 and show step 2
            step1.visibility = View.GONE
            step2.visibility = View.VISIBLE
        }

        nextStep2Btn.setOnClickListener {
            // Hide step 2 and show step 3
            step2.visibility = View.GONE
            step3.visibility = View.VISIBLE
        }

        signUpBtn.setOnClickListener {
            // Submit the registration
            Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()
        }

    }
}