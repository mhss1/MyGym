package com.mhss.app.mygym.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.data.Gym
import com.mhss.app.mygym.adapters.GymsAdapter
import com.mhss.app.mygym.databinding.FragmentGymSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GymSearchFragment : Fragment() {

    private lateinit var binding: FragmentGymSearchBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: GymsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = Firebase.firestore
        binding = FragmentGymSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GymsAdapter {
            findNavController()
                .navigate(GymSearchFragmentDirections.actionGymSearchFragmentToGymPageFragment(it))
        }
        binding.gymsRec.adapter = adapter

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH)
                searchGym()

            true
        }
    }

    private fun searchGym() = lifecycleScope.launch(Dispatchers.IO) {
        db.collection("gyms")
            .get()
            .addOnSuccessListener { result ->
                val gyms = result
                    .toObjects<Gym>()
                    .filter {
                        it.name.contains(
                            binding.searchEditText.text.toString(),
                            ignoreCase = true
                        )
                    }

                if (gyms.isEmpty())
                    binding.noGymsMessage.visibility = View.VISIBLE
                else
                    binding.noGymsMessage.visibility = View.GONE

                adapter.submitList(gyms)
            }
    }

}