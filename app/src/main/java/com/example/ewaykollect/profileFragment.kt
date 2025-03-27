package com.example.ewaykollect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var dpName:TextView
    private lateinit var dpMail:TextView
    private lateinit var dpPhone:TextView
    private lateinit var dpCounty:TextView
    private lateinit var dpTown:TextView
    private lateinit var dpImage: ImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var edt_prof :TextView
    private lateinit var edt_pass:TextView

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestoreRef = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var db= Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root:View=inflater.inflate(R.layout.fragment_profile, container, false)

        // Set the title
        (activity as AppCompatActivity).supportActionBar?.title = "My Profile"

        val userID= auth.currentUser!!.uid
        val ref=db.collection("user").document(userID)

        ref.get().addOnSuccessListener {
            if(it!=null){
                val name= it.data?.get("name").toString()
                val email= it.data?.get("email").toString()
                val phone=it.data?.get("phone").toString()
                val county=it.data?.get("county").toString()
                val town=it.data?.get("town").toString()

                dpName=root.findViewById(R.id.prof_name)
                dpName.setText(name)
                dpMail=root.findViewById(R.id.prof_email)
                dpMail.setText(email)
                dpPhone=root.findViewById(R.id.prof_phone)
                dpPhone.setText(phone)
                dpCounty=root.findViewById(R.id.prof_county)
                dpCounty.setText(county)
                dpTown=root.findViewById(R.id.prof_subcounty)
                dpTown.setText(town)

            }
        }
            .addOnFailureListener{
                Toast.makeText(this.context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }


        cameraIcon = root.findViewById(R.id.edt_camera)
        edt_prof=root.findViewById(R.id.edt_profile)
        edt_pass=root.findViewById(R.id.change_pass)

        // Load existing profile image
        loadProfileImage()

        // Open gallery when camera icon is clicked
        cameraIcon.setOnClickListener {
            openGallery()
        }

        edt_prof.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        edt_pass.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }

        return root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            dpImage.setImageURI(imageUri)
            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val imageRef = storageRef.child("profile_images/$userId.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveImageUrlToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        firestoreRef.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadProfileImage() {
        val userId = auth.currentUser?.uid ?: return
        firestoreRef.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this.context).load(imageUrl).into(dpImage)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile image", Toast.LENGTH_SHORT).show()
            }
    }

}