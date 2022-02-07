package com.mhss.app.mygym.ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.*
import com.mhss.app.mygym.adapters.ExercisesAdapter
import com.mhss.app.mygym.data.Exercise
import com.mhss.app.mygym.data.Gym
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.FragmentUserHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class UserHomeFragment : Fragment() {

    private lateinit var binding: FragmentUserHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ExercisesAdapter

    companion object {
        lateinit var user: User
        lateinit var userProgram: List<Exercise>
        lateinit var userGym: Gym
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        auth = Firebase.auth
        db = Firebase.firestore
        binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.helloTv.text = getString(R.string.welcome_message, auth.currentUser?.displayName)

        getUserData()

        adapter = ExercisesAdapter()
        binding.programRec.adapter = adapter

    }

    private fun getUserProgram() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("program")
            .get().addOnSuccessListener { result ->
                if (result.isEmpty || user.gym.isBlank())
                    binding.noProgramTv.visibility = View.VISIBLE
                else {
                    userProgram = result.toObjects<Exercise>().filter { DateUtils.isToday(it.date) }
                    adapter.submitList(userProgram)
                    binding.noProgramTv.visibility = View.GONE
                }
            }
    }

    private fun getUserData() = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            showProgressBar(true)
        }
        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                user = documentSnapshot.toObject<User>()!!
                setSubscriptionStatus()
            }.addOnFailureListener {
                toast(getString(R.string.could_not_update_data))
            }
    }

    private fun setUserGym() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("gyms").document(user.gym).get()
            .addOnSuccessListener { gymSnapshot ->
                userGym = gymSnapshot.toObject()!!
                setGymData(userGym.name)
                binding.leaveGymButton.setOnClickListener {
                    leaveGym()
                    deleteProgramData()
                }
                showProgressBar(false)
            }.addOnFailureListener {
                toast(getString(R.string.could_not_update_gym_data))
                showProgressBar(false)
            }
    }

    private fun setGymData(name: String) {
        binding.gymName.text = name
    }

    private fun setSubscriptionStatus() {

        if (user.gym.isNotBlank() && user.state == "active") {
            setUserGym()
            handleGymUI()

            if (isActiveSub(user.sub_end))
                handleActiveSub()
            else
                handleInactiveSub()

            binding.subscriptionEnds.text =
                getString(R.string.sub_ends_in, getFormattedDate(user.sub_end))
        } else
            handleNoGymUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> findNavController().navigate(R.id.action_userHomeFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()

    private fun getFormattedDate(date: Long): String {
        val sdf = SimpleDateFormat("MMM dd,yyyy", Locale.getDefault())

        val calender = Calendar.getInstance()
        calender.timeInMillis = date
        return sdf.format(calender.time)
    }

    private fun isActiveSub(date: Long): Boolean {
        val calender = Calendar.getInstance()
        calender.timeInMillis = date

        val today = Date()
        return today.before(calender.time)
    }

    private fun leaveGym() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(user.id)
            .update(mapOf("gym" to "", "state" to ""))
            .addOnSuccessListener {
                user.gym = ""
                user.state = ""
                setSubscriptionStatus()
                toast(getString(R.string.sub_canceled))
            }
    }

    private fun deleteProgramData() = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(user.id)
            .collection("program")
            .get().addOnSuccessListener {
                for (item in it.documents) {
                    deleteDocument(user.id, item.id)
                }
                adapter.submitList(emptyList())
            }
    }

    private fun deleteDocument(dId: String, itemId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("users")
                .document(dId)
                .collection("program")
                .document(itemId)
                .delete()
        }

    private fun handleNoGymUI() {
        binding.subscriptionCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray
            )
        )

        binding.cardHeader.text = getString(R.string.no_gym)
        binding.subscriptionEnds.visibility = View.GONE
        binding.activeTv.visibility = View.GONE
        binding.searchIc.visibility = View.VISIBLE
        binding.gymName.text = getString(R.string.no_gym)
        binding.leaveGymButton.visibility = View.GONE

        binding.yourGymCard.setOnClickListener {
            findNavController().navigate(R.id.action_userHomeFragment_to_gymSearchFragment)
        }
        binding.subscriptionCard.setOnClickListener {
            findNavController().navigate(R.id.action_userHomeFragment_to_gymSearchFragment)
        }
    }

    private fun handleGymUI() {
        binding.cardHeader.text = getString(R.string.your_subscription_is)
        binding.subscriptionEnds.visibility = View.VISIBLE
        binding.activeTv.visibility = View.VISIBLE
        binding.leaveGymButton.visibility = View.VISIBLE
    }

    private fun handleActiveSub() {
        getUserProgram()
        binding.activeTv.text = getString(R.string.active)
        binding.subscriptionCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.green
            )
        )
    }

    private fun handleInactiveSub() {
        binding.activeTv.text = getString(R.string.inactive)
        binding.subscriptionCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.red
            )
        )
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

}