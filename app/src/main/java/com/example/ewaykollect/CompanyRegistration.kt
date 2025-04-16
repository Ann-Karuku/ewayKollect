package com.example.ewaykollect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt

class CompanyRegistration : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var countiesArray: Array<String>
    private lateinit var townMap: Map<String, Int>
    private lateinit var spinnerCounty: Spinner
    private lateinit var spinnerTown: Spinner
    private var latitude: Double = -1.286389 // Default: Nairobi
    private var longitude: Double = 36.817223
    private val selectedWasteCategories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_registration)
        supportActionBar?.hide()

        // Initialize Firestore and Location
        db = Firebase.firestore
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Layouts
        val step1 = findViewById<LinearLayout>(R.id.stepOneLayout)
        val step2 = findViewById<LinearLayout>(R.id.stepTwoLayout)
        val step3 = findViewById<LinearLayout>(R.id.stepThreeLayout)

        // Step 1 fields
        val edtCName = findViewById<EditText>(R.id.edtCName)
        val edtCEmail = findViewById<EditText>(R.id.edtCEmail)
        val edtCPhone = findViewById<EditText>(R.id.edtCPhone)

        // Step 2 fields
        val spinnerCompanyType = findViewById<Spinner>(R.id.spinnerCompanyType)
        val edtRegNo = findViewById<EditText>(R.id.edtRegNo)
        spinnerCounty = findViewById(R.id.spinnerCounty)
        spinnerTown = findViewById(R.id.spinnerTown)

        // Step 3 fields
        val wasteCategoriesTextView = findViewById<TextView>(R.id.spinnerWasteCategories)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtRepeatPass = findViewById<EditText>(R.id.edtRepeatPass)

        // Buttons
        val nextStep1Btn = findViewById<Button>(R.id.nextStep1Btn)
        val nextStep2Btn = findViewById<Button>(R.id.nextStep2Btn)
        val prevStep2Btn = findViewById<Button>(R.id.prevStep2Btn)
        val prevStep3Btn = findViewById<Button>(R.id.prevStep3Btn)
        val signUpBtn = findViewById<Button>(R.id.signUpBtn)

        // Setup spinners
        setupSpinners()

        // Setup company type spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.company_types,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCompanyType.adapter = adapter
        }

        // Setup waste categories multi-select
        val wasteCategories = resources.getStringArray(R.array.waste_categories).toList()
        wasteCategoriesTextView.setOnClickListener {
            val checkedItems = BooleanArray(wasteCategories.size) { index ->
                selectedWasteCategories.contains(wasteCategories[index])
            }
            AlertDialog.Builder(this)
                .setTitle("Select Waste Categories")
                .setMultiChoiceItems(
                    wasteCategories.toTypedArray(),
                    checkedItems
                ) { _, which, isChecked ->
                    val category = wasteCategories[which]
                    if (isChecked) {
                        selectedWasteCategories.add(category)
                    } else {
                        selectedWasteCategories.remove(category)
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    if (selectedWasteCategories.isEmpty()) {
                        wasteCategoriesTextView.text = "Select categories"
                    } else {
                        wasteCategoriesTextView.text = selectedWasteCategories.joinToString(", ")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Get location
        requestLocation()

        nextStep1Btn.setOnClickListener {
            if (validateStep1(edtCName, edtCEmail, edtCPhone)) {
                step1.visibility = View.GONE
                step2.visibility = View.VISIBLE
            }
        }

        nextStep2Btn.setOnClickListener {
            if (validateStep2(edtRegNo, spinnerCompanyType, spinnerCounty, spinnerTown)) {
                step2.visibility = View.GONE
                step3.visibility = View.VISIBLE
            }
        }

        prevStep2Btn.setOnClickListener {
            step1.visibility = View.VISIBLE
            step2.visibility = View.GONE
        }

        prevStep3Btn.setOnClickListener {
            step2.visibility = View.VISIBLE
            step3.visibility = View.GONE
        }

        signUpBtn.setOnClickListener {
            if (validateStep3(edtPassword, edtRepeatPass)) {
                // Hash the password
                val hashedPassword = BCrypt.hashpw(edtPassword.text.toString(), BCrypt.gensalt())

                val company = hashMapOf(
                    "companyName" to edtCName.text.toString(),
                    "email" to edtCEmail.text.toString(),
                    "contact" to mapOf(
                        "phone" to edtCPhone.text.toString()
                    ),
                    "companyType" to spinnerCompanyType.selectedItem.toString(),
                    "registrationNumber" to edtRegNo.text.toString(),
                    "location" to mapOf(
                        "county" to spinnerCounty.selectedItem.toString(),
                        "town" to spinnerTown.selectedItem.toString()
                    ),
                    "coordinates" to mapOf(
                        "latitude" to latitude,
                        "longitude" to longitude
                    ),
                    "wasteCategory" to selectedWasteCategories,
                    "password" to hashedPassword,
                    "rating" to 0.0,
                    "popularityScore" to 0L
                )

                // Check if email already exists
                db.collection("companies")
                    .whereEqualTo("email", edtCEmail.text.toString())
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Save to Firestore
                            db.collection("companies")
                                .add(company)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Registration Successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Registration Failed: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            edtCEmail.error = "Email already registered"
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error checking email: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        }
    }

    private fun setupSpinners() {
        // Get counties from resources
        countiesArray = resources.getStringArray(R.array.kenya_counties)

        // Mapping counties to town arrays
        townMap = mapOf(
            "Baringo" to R.array.towns_baringo,
            "Bomet" to R.array.towns_bomet,
            "Bungoma" to R.array.towns_bungoma,
            "Busia" to R.array.towns_busia,
            "Elgeyo Marakwet" to R.array.towns_elgeyo_marakwet,
            "Embu" to R.array.towns_embu,
            "Garissa" to R.array.towns_garissa,
            "Homa Bay" to R.array.towns_homa_bay,
            "Isiolo" to R.array.towns_isiolo,
            "Kajiado" to R.array.towns_kajiado,
            "Kakamega" to R.array.towns_kakamega,
            "Kericho" to R.array.towns_kericho,
            "Kiambu" to R.array.towns_kiambu,
            "Kilifi" to R.array.towns_kilifi,
            "Kirinyaga" to R.array.towns_kirinyaga,
            "Kisii" to R.array.towns_kisii,
            "Kisumu" to R.array.towns_kisumu,
            "Kitui" to R.array.towns_kitui,
            "Kwale" to R.array.towns_kwale,
            "Laikipia" to R.array.towns_laikipia,
            "Lamu" to R.array.towns_lamu,
            "Machakos" to R.array.towns_machakos,
            "Makueni" to R.array.towns_makueni,
            "Mandera" to R.array.towns_mandera,
            "Marsabit" to R.array.towns_marsabit,
            "Meru" to R.array.towns_meru,
            "Migori" to R.array.towns_migori,
            "Mombasa" to R.array.towns_mombasa,
            "Murang'a" to R.array.towns_muranga,
            "Nairobi" to R.array.towns_nairobi,
            "Nakuru" to R.array.towns_nakuru,
            "Nandi" to R.array.towns_nandi,
            "Narok" to R.array.towns_narok,
            "Nyamira" to R.array.towns_nyamira,
            "Nyandarua" to R.array.towns_nyandarua,
            "Nyeri" to R.array.towns_nyeri,
            "Samburu" to R.array.towns_samburu,
            "Siaya" to R.array.towns_siaya,
            "Taita Taveta" to R.array.towns_taita_taveta,
            "Tana River" to R.array.towns_tana_river,
            "Tharaka Nithi" to R.array.towns_tharaka_nithi,
            "Trans Nzoia" to R.array.towns_trans_nzoia,
            "Turkana" to R.array.towns_turkana,
            "Uasin Gishu" to R.array.towns_uasin_gishu,
            "Vihiga" to R.array.towns_vihiga,
            "Wajir" to R.array.towns_wajir,
            "West Pokot" to R.array.towns_west_pokot
        )

        // Populate Counties Spinner
        val countyAdapter = ArrayAdapter(this, R.layout.spinner_item, countiesArray)
        countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCounty.adapter = countyAdapter

        // Handle County Selection to Load Towns
        spinnerCounty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCounty = countiesArray[position]
                loadTownsForCounty(selectedCounty)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadTownsForCounty(county: String) {
        val townArrayId = townMap[county] ?: return
        val townArray = resources.getStringArray(townArrayId)

        val townAdapter = ArrayAdapter(this, R.layout.spinner_item, townArray)
        townAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTown.adapter = townAdapter
    }

    private fun validateStep1(name: EditText, email: EditText, phone: EditText): Boolean {
        return when {
            name.text.isEmpty() -> {
                name.error = "Company name is required"
                false
            }
            email.text.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches() -> {
                email.error = "Valid email is required"
                false
            }
            phone.text.isEmpty() || !android.util.Patterns.PHONE.matcher(phone.text).matches() -> {
                phone.error = "Valid phone number is required"
                false
            }
            else -> true
        }
    }

    private fun validateStep2(regNo: EditText, companyType: Spinner, county: Spinner, town: Spinner): Boolean {
        return when {
            regNo.text.isEmpty() -> {
                regNo.error = "Registration number is required"
                false
            }
            companyType.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select a company type", Toast.LENGTH_SHORT).show()
                false
            }
            county.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select a county", Toast.LENGTH_SHORT).show()
                false
            }
            town.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Please select a town", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun validateStep3(password: EditText, repeatPass: EditText): Boolean {
        return when {
            password.text.isEmpty() -> {
                password.error = "Password is required"
                false
            }
            password.text.length < 6 -> {
                password.error = "Password must be at least 6 characters"
                false
            }
            repeatPass.text.toString() != password.text.toString() -> {
                repeatPass.error = "Passwords do not match"
                false
            }
            selectedWasteCategories.isEmpty() -> {
                Toast.makeText(this, "Please select at least one waste category", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}