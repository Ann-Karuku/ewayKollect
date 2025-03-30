package com.example.ewaykollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfileFragment : Fragment() {

    private lateinit var edtName:TextView
    private lateinit var edtEmail:TextView
    private lateinit var edtPhone:TextView
    private lateinit var spinnerCounty: Spinner
    private lateinit var spinnerTown: Spinner
    private lateinit var updateProfBtn: Button

    private lateinit var countiesArray: Array<String>
    private lateinit var townMap: Map<String, Int>

    private var db= Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root=inflater.inflate(R.layout.fragment_edit_profile, container, false)

        edtName=root.findViewById(R.id.edtUName)
        edtEmail=root.findViewById(R.id.edtUEmail)
        edtPhone=root.findViewById(R.id.edtUPhone)
        updateProfBtn=root.findViewById(R.id.updateProfbtn)
        spinnerCounty=root.findViewById(R.id.spinnerCounty)
        spinnerTown=root.findViewById(R.id.spinnerTown)

        // Load User Data
        setupSpinners()
        loadUserData()

        // Handle Update Button Click
        updateProfBtn.setOnClickListener {
            updateUserProfile()
        }

        return root
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
            "Uasin Gishu" to R.array.towns_eldoret,
            "Vihiga" to R.array.towns_vihiga,
            "Wajir" to R.array.towns_wajir,
            "West Pokot" to R.array.towns_west_pokot
        )

        // Populate Counties Spinner
        val countyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countiesArray)
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
    private fun loadTownsForCounty(county: String){
        val townArrayId = townMap[county] ?: return
        val townArray = resources.getStringArray(townArrayId)

        val townAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, townArray)
        townAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTown.adapter = townAdapter
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val selectedCounty = spinnerCounty.selectedItem.toString()
            val selectedTown = spinnerTown.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedCounty.isEmpty() || selectedTown.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return
            }

            val userUpdates = mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "county" to selectedCounty,
                "town" to selectedTown
            )

            db.collection("user").document(userId)
                .update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        edtName.setText(document.getString("name"))
                        edtEmail.setText(document.getString("email"))
                        edtPhone.setText(document.getString("phone"))
                        // Set county spinner selection
                        val userCounty = document.getString("county") ?: ""
                        val countyIndex = countiesArray.indexOf(userCounty)
                        if (countyIndex != -1) {
                            spinnerCounty.setSelection(countyIndex)
                        }

                        // Set town spinner selection
                        val userTown = document.getString("town") ?: ""
                        loadTownsForCounty(userCounty)
                        spinnerTown.post {
                            val townIndex = (spinnerTown.adapter as ArrayAdapter<String>).getPosition(userTown)
                            if (townIndex != -1) {
                                spinnerTown.setSelection(townIndex)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}