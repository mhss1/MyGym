package com.mhss.app.mygym.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.R
import com.mhss.app.mygym.adapters.RequestsAdapter
import com.mhss.app.mygym.adapters.SubsAdapter
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.FragmentOwnerHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class OwnerHomeFragment : Fragment() {

    private lateinit var binding: FragmentOwnerHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var subsAdapter: SubsAdapter
    private lateinit var requestsAdapter: RequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        auth = Firebase.auth
        db = Firebase.firestore
        binding = FragmentOwnerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subsAdapter = SubsAdapter {
            findNavController()
                .navigate(
                    OwnerHomeFragmentDirections.actionOwnerHomeFragmentToSubscriberPageFragment(
                        it
                    )
                )
        }

        requestsAdapter = RequestsAdapter(
            { user -> // onAcceptClicked
                acceptUser(user.id)
            }, { user -> // onRemoveClicked
                removeUser(user.id)
            })
        binding.requestsRec.adapter = requestsAdapter
        binding.subsRec.adapter = subsAdapter

        setGymData()
        getSubscribers()
        getRequests()
    }

    private fun getRequests() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .whereEqualTo("gym", auth.currentUser!!.uid)
            .whereEqualTo("state", "pending")
            .get()
            .addOnSuccessListener {
                val requests = it.toObjects<User>()
                if (requests.isEmpty())
                    binding.noRequests.visibility = View.VISIBLE
                else
                    binding.noRequests.visibility = View.GONE

                requestsAdapter.submitList(requests)
            }
    }


    private fun getSubscribers() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .whereEqualTo("gym", auth.currentUser!!.uid)
            .whereEqualTo("state", "active")
            .get()
            .addOnSuccessListener {
                val subs = it.toObjects<User>().sortedBy { sub -> sub.sub_end }
                if (subs.isEmpty())
                    binding.noSubscribers.visibility = View.VISIBLE
                else
                    binding.noSubscribers.visibility = View.GONE

                subsAdapter.submitList(subs)
            }
    }


    private fun setGymData() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("gyms")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                binding.gymName.text = it["name"].toString()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> findNavController().navigate(R.id.action_ownerHomeFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun acceptUser(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 29)
        val time = calendar.timeInMillis
        db.collection("users")
            .document(uid)
            .update(mapOf("gym" to auth.currentUser!!.uid, "state" to "active", "sub_end" to time))
        getSubscribers()
    }

    private fun removeUser(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(uid)
            .update(mapOf("gym" to "", "state" to "", "sub_end" to 0))
    }
}