package com.mhss.app.mygym.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.R
import com.mhss.app.mygym.databinding.FragmentSignUpBinding
import kotlinx.coroutines.*

const val USER_TYPE_SUB = "sub"
const val USER_TYPE_OWNER = "owner"

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var preferenceManager: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        db = Firebase.firestore
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding.signUpBtn.setOnClickListener {
            signUpNewUser(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString(),
                binding.firstNameEditText.text.toString(),
                binding.lastNameEditText.text.toString(),
                if (binding.joinRadioBtn.isChecked) USER_TYPE_SUB else USER_TYPE_OWNER
            )

        }

        binding.emailEditText.doOnTextChanged { _, _, _, _ ->
            binding.emailTextInputLayout.error = null
        }
        binding.passwordEditText.doOnTextChanged { _, _, _, _ ->
            binding.passwordTextInputLayout.error = null
        }
        binding.firstNameEditText.doOnTextChanged { _, _, _, _ ->
            binding.emailTextInputLayout.error = null
        }
        binding.typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.join_radio_btn -> binding.gymNameEditText.visibility = View.GONE
                R.id.create_radio_btn -> binding.gymNameEditText.visibility = View.VISIBLE
            }
        }

    }

    private fun signUpNewUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        type: String
    ) {
        if (email.isBlank()) {
            setEmailError(getString(R.string.empty_email))
            return
        }
        if (!password.isValidPassword()) {
            setPasswordError(getString(R.string.invalid_password_message))
            return
        }
        if (firstName.isBlank()) {
            setNameError(getString(R.string.empty_name))
            return
        }
        if (binding.createRadioBtn.isChecked && binding.gymNameEditText.text.isBlank()) {
            toast(getString(R.string.gym_name_empty))
            return
        }

        showProgressBar(true)
        showErrorMessage(false)

        lifecycleScope.launch(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = "$firstName $lastName"
                        }
                        updateUserProfile(profileUpdates, type, firstName, lastName)
                        saveUserType(type)
                    } else
                        showErrorMessage(true)

                    showProgressBar(false)
                }
        }

    }

    private fun String.isValidPassword(): Boolean {
        return length >= 8 && (any { it in 'a'..'z' } || any { it in 'A'..'Z' }) && any { it in '0'..'9' }
    }

    private fun toast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()

    private fun setEmailError(message: String) {
        binding.emailTextInputLayout.error = message
    }

    private fun setPasswordError(message: String) {
        binding.passwordTextInputLayout.error = message
    }

    private fun setNameError(message: String) {
        binding.firstNameInputLayout.error = message
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showErrorMessage(show: Boolean) {
        binding.errorTv.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createGym(data: HashMap<String, Any>) = lifecycleScope.launch(Dispatchers.IO) {
        db.collection("gyms")
            .document(auth.currentUser!!.uid)
            .set(data)
    }

    private fun setUserInfo(type: String, firstName: String, lastName: String) = lifecycleScope.launch(Dispatchers.IO) {
            val info = if (type == USER_TYPE_SUB)
                hashMapOf(
                    "type" to type,
                    "id" to auth.currentUser?.uid,
                    "name" to "$firstName $lastName",
                    "sub_end" to 0,
                    "gym" to "",
                    "state" to ""
                )
            else
                hashMapOf(
                    "type" to type,
                    "id" to auth.currentUser?.uid,
                    "name" to "$firstName $lastName",
                )
            db.collection("users").document(auth.currentUser!!.uid).set(info)
        }

    private fun updateUserProfile(
        profileUpdates: UserProfileChangeRequest,
        type: String,
        firstName: String,
        lastName: String
    ) = lifecycleScope.launch(Dispatchers.IO) {
        auth.currentUser!!.updateProfile(profileUpdates)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    if (type == USER_TYPE_OWNER)
                        createGym(
                            hashMapOf(
                                "name" to binding.gymNameEditText.text.toString(),
                                "id" to auth.currentUser!!.uid,
                                "owner" to "$firstName $lastName"
                            )
                        )

                    setUserInfo(type, firstName, lastName)

                    auth.currentUser?.sendEmailVerification()
                    toast(getString(R.string.email_verification_sent))

                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                } else
                    showErrorMessage(true)
            }
    }

    private fun saveUserType(type: String) {
        val editor = preferenceManager.edit()
        editor.putString("type", type)
        editor.apply()
    }

}