package com.example.ewaykollect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class AddEwasteDialogFragment : DialogFragment() {

    // DB initialization
    private var db = Firebase.firestore
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    // Permission request code
    private val PERMISSION_REQUEST_CODE = 100
    private val IMAGE_PICK_CODE = 1000

    // Categorized list of e-waste items
    private val items = arrayOf(
        "Select EWaste type", "Mobile Phones", "Tablets", "Laptops",
        "Desktop Computers", "Monitors", "Printers",
        "Televisions", "Remote Controls", "DVD Players",
        "Washing Machines", "Refrigerators", "Microwaves",
        "Air Conditioners", "Electric Fans", "Heaters"
    )

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your custom layout for the dialog
        val root: View = inflater.inflate(R.layout.fragment_add_ewaste, container, false)

        // Initialize Firebase and Storage Reference
        storageReference = FirebaseStorage.getInstance().reference

        // Find views
        val edtEName = root.findViewById<EditText>(R.id.edtEName)
        val edtNo = root.findViewById<EditText>(R.id.edtENo)
        val edtState = root.findViewById<EditText>(R.id.edtState)
        val buttonUploadPhoto = root.findViewById<Button>(R.id.btnUploadPhoto)
        val uploadBtn = root.findViewById<com.google.android.material.button.MaterialButton>(R.id.uploadbtn)
        val spinner = root.findViewById<Spinner>(R.id.spinnerType)
        progressBar = root.findViewById(R.id.progressBar)

        // Creating an ArrayAdapter using the EwasteItems array
        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = arrayAdapter

        // Check and request permissions
        checkAndRequestPermissions()

        // Handle photo upload button click
        buttonUploadPhoto.setOnClickListener {
            openImagePicker()
        }

        // Handle form submission
        uploadBtn.setOnClickListener {
            val itemName = edtEName.text.toString().trim()
            val itemType = spinner.selectedItem.toString()
            val itemNo = edtNo.text.toString().trim()
            val itemState = edtState.text.toString().trim()

            if (itemName.isNotEmpty() && itemType != "Select EWaste type" && itemNo.isNotEmpty() && itemState.isNotEmpty()) {
                if (selectedImageUri != null) {
                    uploadImageAndSaveData(itemName, itemType, itemNo, itemState)
                } else {
                    Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionDebug", "Read External Storage permission granted")
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot access external storage.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Log.d("UploadDebug", "Image URI received: $selectedImageUri")
            val imageEView = view?.findViewById<ImageView>(R.id.imageEView)
            imageEView?.setImageURI(selectedImageUri)
        } else {
            Log.d("UploadDebug", "No image selected or operation cancelled")
        }
    }

    private fun uploadImageAndSaveData(itemName: String, itemType: String, itemNo: String, itemState: String) {
        // Show ProgressBar
        progressBar.visibility = View.VISIBLE

        val imageRef = storageReference.child("eway_images/${System.currentTimeMillis()}.jpg")

        selectedImageUri?.let { uri ->
            try {
                // Open the input stream from the URI using the content resolver
                val stream = requireContext().contentResolver.openInputStream(uri)

                if (stream != null) {
                    // Upload the stream to Firebase Storage
                    val uploadTask = imageRef.putStream(stream)
                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val imageUrl = downloadUri.toString()
                            saveDataToFirestore(itemName, itemType, itemNo, itemState, imageUrl)
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }.addOnCompleteListener {
                        // Hide ProgressBar
                        progressBar.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to open image file", Toast.LENGTH_SHORT).show()
                    // Hide ProgressBar in case of failure
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                // Hide ProgressBar in case of exception
                progressBar.visibility = View.GONE
            }
        } ?: run {
            Toast.makeText(requireContext(), "No image URI available for upload", Toast.LENGTH_SHORT).show()
            // Hide ProgressBar if no image URI
            progressBar.visibility = View.GONE
        }
    }

    private fun saveDataToFirestore(itemName: String, itemType: String, itemNo: String, itemState: String, imageUrl: String) {
        val ewasteItem = mapOf(
            "name" to itemName,
            "type" to itemType,
            "number" to itemNo,
            "state" to itemState,
            "imageUrl" to imageUrl
        )

        db.collection("EwasteItems")
            .add(ewasteItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item uploaded successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload item", Toast.LENGTH_SHORT).show()
            }
    }
}
