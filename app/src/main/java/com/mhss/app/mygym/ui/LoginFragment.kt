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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.R
import com.mhss.app.mygym.databinding.FragmentLoginBinding
import kotlinx.coroutines.*


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        checkIfUserLoggedIn()

        binding.loginBtn.setOnClickListener {
            login(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
        }

        binding.signUpTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.emailEditText.doOnTextChanged { _, _, _, _ ->
            setEmailError(null)
        }
        binding.passwordEditText.doOnTextChanged { _, _, _, _ ->
            setPasswordError(null)
        }
    }

    private fun login(email: String, password: String) {
        if (email.isBlank()) {
            setEmailError(getString(R.string.empty_email))
            return
        }
        if (password.isBlank()) {
            setPasswordError(getString(R.string.empty_password))
            return
        }

        showProgressBar(true)
        showErrorMessage(false)
        setEmailError(null)
        setPasswordError(null)

        lifecycleScope.launch(Dispatchers.IO) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true)
                            handleUserType()
                        else {
                            auth.currentUser?.sendEmailVerification()
                            showErrorMessage(true)
                            setErrorMessage(getString(R.string.please_verify_email))
                        }
                    } else {
                        showErrorMessage(true)
                        setErrorMessage(getString(R.string.error_login_or_signup))
                    }
                    showProgressBar(false)
                }
        }
    }

    private fun checkIfUserLoggedIn() {
        if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {

            val type = preferenceManager.getString("type", null)
            if (type != null) {
                if (type == USER_TYPE_SUB)
                    findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
                else
                    findNavController().navigate(R.id.action_loginFragment_to_ownerHomeFragment)
            }
        }
    }

    private fun toast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()

    private fun saveUserType(type: String?) {
        val editor = preferenceManager.edit()
        editor.putString("type", type)
        editor.apply()
    }

    private fun handleUserType() {
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                val type = it["type"].toString()
                saveUserType(if (type == "null") null else type)
                if (type == USER_TYPE_SUB)
                    findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
                else
                    findNavController().navigate(R.id.action_loginFragment_to_ownerHomeFragment)
                toast(getString(R.string.login_successful))
            }
    }

    private fun showErrorMessage(show: Boolean) {
        binding.errorTv.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setErrorMessage(message: String) {
        binding.errorTv.text = message
    }

    private fun setEmailError(message: String?) {
        binding.emailTextInputLayout.error = message
    }

    private fun setPasswordError(message: String?) {
        binding.passwordTextInputLayout.error = message
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

}