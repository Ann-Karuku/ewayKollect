package com.example.ewaykollect

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

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

        //see if user is logged in with google and hide the current pass field
        val user = auth.currentUser
        val providers = user?.providerData?.map { it.providerId } ?: emptyList()
        if (providers.contains("google.com")) {
            currentPass.visibility = View.GONE
        } else {
            currentPass.visibility = View.VISIBLE
        }

        //password toogle
        setupPasswordToggle(currentPass)
        setupPasswordToggle(newPass)
        setupPasswordToggle(confirmPass)

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        var isPasswordVisible = false

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawablesRelative[2] // Get the drawableEnd (eye icon)
                if (drawableEnd != null) {
                    val iconBounds = drawableEnd.bounds
                    val iconWidth = iconBounds.width()
                    val editTextEnd = editText.right - editText.paddingRight
                    val touchX = event.rawX.toInt()

                    if (touchX >= (editTextEnd - iconWidth)) {
                        // Toggle visibility
                        isPasswordVisible = !isPasswordVisible
                        editText.inputType = if (isPasswordVisible) {
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        } else {
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        }
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close, 0
                        )
                        editText.setSelection(editText.text.length) // Keep cursor at the end

                        editText.performClick()

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
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

        if (user != null) {
            // Check if user signed in with Google (no password exists)
            val providers = user.providerData.map { it.providerId }
            if (providers.contains("google.com")) {
                // Google Sign-In users don't have a password, so just allow setting one
                user.updatePassword(new).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Password set successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_changePasswordFragment_to_profileFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to set password!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return
            }


        if (user.email != null) {
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