package com.example.ewaykollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth


class ChangePasswordFragment : Fragment() {

    private lateinit var currentPass:EditText
    private lateinit var newPass:EditText
    private lateinit var confirmPass:EditText
    private lateinit var savePassBtn:Button
    private lateinit var resendVerification:Button

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root=inflater.inflate(R.layout.fragment_change_password, container, false)

        // Initialize views
        currentPass = root.findViewById(R.id.et_current_password)
        newPass = root.findViewById(R.id.et_new_password)
        confirmPass = root.findViewById(R.id.et_confirm_password)
        savePassBtn = root.findViewById(R.id.btn_save_password)
        resendVerification=root.findViewById(R.id.btn_resend_verification)


        savePassBtn.setOnClickListener {
            val currentPassword = currentPass.text.toString().trim()
            val newPassword = newPass.text.toString().trim()
            val confirmPassword = confirmPass.text.toString().trim()

            if (validateInputs(currentPassword, newPassword, confirmPassword)) {
                checkEmailVerification(currentPassword, newPassword)
            }
        }

        checkEmailVerificationStatus()
        resendVerification.setOnClickListener {
            sendVerificationEmail()
        }

        return root
    }

    private fun checkEmailVerificationStatus() {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            resendVerification.visibility = View.VISIBLE // Show button if not verified
        } else {
            resendVerification.visibility = View.GONE // Hide button if verified
        }
    }

    private fun checkEmailVerification(currentPassword: String, newPassword: String) {
        val user = auth.currentUser

        if (user != null) {
            if (!user.isEmailVerified) {
                Toast.makeText(requireContext(), "Please verify your email before changing the password!", Toast.LENGTH_LONG).show()
                sendVerificationEmail()
                resendVerification.visibility = View.VISIBLE
                return
            }
            updatePassword(currentPassword, newPassword)
        } else {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser

        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Verification email sent! Please check your inbox.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Failed to send verification email!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(current: String, new: String) {
        val user = auth.currentUser

        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, current)

            // Re-authenticate the user
            user.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Now update password
                    user.updatePassword(new).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_changePasswordFragment_to_profileFragment)
                        } else {
                            Toast.makeText(requireContext(), "Failed to update password!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Current password is incorrect!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (new.length < 6) {
            Toast.makeText(requireContext(), "New password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (new != confirm) {
            Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}