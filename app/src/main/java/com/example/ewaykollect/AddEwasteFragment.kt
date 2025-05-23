package com.example.ewaykollect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddEwasteDialogFragment : DialogFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            view?.findViewById<ImageView>(R.id.imageEView)?.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_ewaste, container, false)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        val companyId = arguments?.getString("COMPANY_ID") ?: ""
        val companyName = arguments?.getString("COMPANY_NAME") ?: ""
        val nameEditText = view.findViewById<EditText>(R.id.edtEName)
        val typeSpinner = view.findViewById<Spinner>(R.id.spinnerType)
        val numberEditText = view.findViewById<EditText>(R.id.edtENo)
        val stateEditText = view.findViewById<EditText>(R.id.edtState)
        val uploadPhotoButton = view.findViewById<Button>(R.id.btnUploadPhoto)
        val submitButton = view.findViewById<Button>(R.id.uploadbtn)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val types = arrayOf(
            "Mobile Phones", "Tablets", "Laptops", "Desktop Computers",
            "Monitors", "Printers", "Televisions", "Remote Controls",
            "DVD Players", "Washing Machines", "Refrigerators", "Microwaves",
            "Air Conditioners", "Electric Fans", "Heaters"
        )
        typeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        uploadPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImage.launch(intent)
        }

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val type = typeSpinner.selectedItem.toString()
            val number = numberEditText.text.toString().toIntOrNull() ?: 0
            val state = stateEditText.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (userId == null) {
                if (isAdded && context != null) {
                    Toast.makeText(context, "Please sign in", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            if (name.isEmpty() || number <= 0 || state.isEmpty()) {
                if (isAdded && context != null) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            submitButton.isEnabled = false

            saveEwasteItem(name, type, number, state, userId, companyId, companyName, progressBar, submitButton)
        }

        return view
    }

    private fun saveEwasteItem(
        name: String,
        type: String,
        number: Int,
        state: String,
        userId: String,
        companyId: String,
        companyName: String,
        progressBar: ProgressBar,
        submitButton: Button
    ) {
        val uploadTask = imageUri?.let {
            val ref = storage.reference.child("ewaste_images/${UUID.randomUUID()}.jpg")
            ref.putFile(it).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }
        }

        uploadTask?.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            saveToFirestore(name, type, number, state, userId, companyId, companyName, imageUrl, progressBar, submitButton)
        }?.addOnFailureListener { e ->
            if (isAdded && context != null) {
                Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            progressBar.visibility = View.GONE
            submitButton.isEnabled = true
        } ?: saveToFirestore(name, type, number, state, userId, companyId, companyName, "", progressBar, submitButton)
    }

    private fun saveToFirestore(
        name: String,
        type: String,
        number: Int,
        state: String,
        userId: String,
        companyId: String,
        companyName: String,
        imageUrl: String,
        progressBar: ProgressBar,
        submitButton: Button
    ) {
        val ewasteItem = hashMapOf(
            "imageUrl" to imageUrl,
            "name" to name,
            "number" to number,
            "state" to state,
            "type" to type,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("EwasteItems").add(ewasteItem)
            .addOnSuccessListener { docRef ->
                if (companyId.isNotEmpty()) {
                    val pickupRequest = hashMapOf(
                        "companyId" to companyId,
                        "companyName" to companyName,
                        "ewasteName" to name,
                        "ewasteType" to type,
                        "quantity" to number,
                        "state" to state,
                        "imageUrl" to imageUrl,
                        "userId" to userId,
                        "requestTime" to System.currentTimeMillis(),
                        "status" to "pending"
                    )

                    firestore.collection("pickupRequests").add(pickupRequest)
                        .addOnSuccessListener { pickupDocRef ->
                            if (isAdded && context != null) {
                                Toast.makeText(context, "Pickup request submitted for $companyName", Toast.LENGTH_SHORT).show()
                            }

                            val notification = hashMapOf(
                                "userId" to userId,
                                "type" to "pickup_request",
                                "message" to "New pickup request for $name to $companyName",
                                "timestamp" to System.currentTimeMillis(),
                                "read" to false
                            )
                            firestore.collection("notifications").add(notification)
                                .addOnSuccessListener { notifDocRef ->
                                    if (isAdded && context != null) {
                                        Toast.makeText(context, "Notification created", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    if (isAdded && context != null) {
                                        Toast.makeText(context, "Error saving notification: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            if (isAdded) {
                                findNavController().popBackStack(R.id.recyclerDetailsFragment, false)
                            }
                        }
                        .addOnFailureListener { e ->
                            if (isAdded && context != null) {
                                Toast.makeText(context, "Error saving pickup request: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            progressBar.visibility = View.GONE
                            submitButton.isEnabled = true
                        }
                } else {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "E-waste item added", Toast.LENGTH_SHORT).show()
                    }
                    if (isAdded) {
                       findNavController().popBackStack(R.id.myEwasteFragment, false)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (isAdded && context != null) {
                    Toast.makeText(context, "Error saving e-waste item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.GONE
                submitButton.isEnabled = true
            }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}