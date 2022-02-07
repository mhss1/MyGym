package com.mhss.app.mygym.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.adapters.MembersAdapter
import com.mhss.app.mygym.R
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.FragmentGymPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GymPageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentGymPageBinding
    private val args: GymPageFragmentArgs by navArgs()
    private lateinit var adapter: MembersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        db = Firebase.firestore
        binding = FragmentGymPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gym = args.gym

        adapter = MembersAdapter()
        binding.membersRec.adapter = adapter

        binding.gymOwner.text = getString(R.string.owner, gym.owner)
        binding.gymTitleTv.text = gym.name

        setGymMembers(gym.id)
        setButtonText(gym.id)

        binding.gymJoinLeaveButton.setOnClickListener {
            if (UserHomeFragment.user.gym == gym.id)
                leaveGym()
            else
                requestGymJoin(gym.id)
        }

    }

    private fun setGymMembers(id: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .whereEqualTo("gym", id)
            .whereEqualTo("state", "active")
            .get()
            .addOnSuccessListener {
                val members = it.toObjects<User>()
                adapter.submitList(members)
                binding.gymMembersCount.text =
                    resources.getQuantityString(R.plurals.n_members, members.size, members.size)
            }
    }


    private fun setButtonText(id: String) {
        if (UserHomeFragment.user.gym == id) {
            binding.gymJoinLeaveButton.text = getString(R.string.pending)
        } else {
            binding.gymJoinLeaveButton.text = getString(R.string.join)
        }
    }

    private fun toast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()

    private fun leaveGym() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users").document(auth.currentUser!!.uid)
            .update(mapOf("gym" to "", "state" to ""))
            .addOnSuccessListener {
                toast(getString(R.string.sub_canceled))
                binding.gymJoinLeaveButton.text = getString(R.string.join)
                UserHomeFragment.user.gym = ""
            }
    }

    private fun requestGymJoin(gymId: String) = CoroutineScope(Dispatchers.IO).launch {
        val data = mapOf("gym" to gymId, "state" to "pending")
        db.collection("users").document(UserHomeFragment.user.id).update(data)
            .addOnSuccessListener {
                toast(getString(R.string.request_sent))
                binding.gymJoinLeaveButton.text = getString(R.string.pending)
                UserHomeFragment.user.gym = gymId
                UserHomeFragment.user.state = "pending"
            }
    }

}