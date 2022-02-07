package com.mhss.app.mygym.ui

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var auth: FirebaseAuth

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        auth = Firebase.auth

        val signOut: Preference? = findPreference("sign_out")
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        signOut?.setOnPreferenceClickListener {

            auth.signOut()

            val editor = pref.edit()
            editor.putString("type", null)
            editor.apply()

            findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
            true
        }

    }
}