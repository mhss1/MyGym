package com.mhss.app.mygym.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.data.Exercise
import com.mhss.app.mygym.adapters.ExercisesAdapter
import com.mhss.app.mygym.R
import com.mhss.app.mygym.databinding.FragmentSubscriberPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class SubscriberPageFragment : Fragment() {

    private lateinit var binding: FragmentSubscriberPageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ExercisesAdapter
    private val args: SubscriberPageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriberPageBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         val user = args.user

        if (isActiveSub(user.sub_end))
            showReactivateButton(false)
        else
            showReactivateButton(true)

        getProgram(user.id)

        adapter = ExercisesAdapter()
        binding.programRec.adapter = adapter

        binding.userNameTv.text = user.name
        binding.subEnd.text = getString(R.string.sub_ends_in, getFormattedDate(user.sub_end))

        binding.removeBtn.setOnClickListener {
            removeUser(user.id)
        }
        binding.addExerciseBtn.setOnClickListener {
            displayAddExerciseDialog()
        }
        binding.reactivateBtn.setOnClickListener {
            reactivateUserSubscription(user.id)
            showReactivateButton(false)
        }
    }

    private fun getProgram(id: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(id)
            .collection("program")
            .get()
            .addOnSuccessListener {
                val exercises = it.toObjects<Exercise>()
                adapter.submitList(exercises)
            }
    }

    private fun getFormattedDate(date: Long): String {
        val sdf = SimpleDateFormat("MMM dd,yyyy", Locale.getDefault())

        val calender = Calendar.getInstance()
        calender.timeInMillis = date
        return sdf.format(calender.time)
    }

    private fun displayAddExerciseDialog(){
        val fm = requireActivity().supportFragmentManager
        val dialog = ExerciseDialog(args.user)
        dialog.show(fm, "dialog")
    }

    private fun isActiveSub(date: Long): Boolean {
        val calender = Calendar.getInstance()
        calender.timeInMillis = date

        val today = Date()
        return today.before(calender.time)
    }

    private fun showReactivateButton(show: Boolean){
        binding.reactivateBtn.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun removeUser(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(uid)
            .update(mapOf("gym" to "", "state" to "", "sub_end" to 0))
            .addOnSuccessListener {
                deleteProgramData(uid)
            }
        withContext(Dispatchers.Main){
            findNavController()
                .navigate(SubscriberPageFragmentDirections.actionSubscriberPageFragmentToOwnerHomeFragment())
        }
    }

    private fun deleteProgramData(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(uid)
            .collection("program")
            .get().addOnSuccessListener {
                for (item in it.documents){
                    deleteDocument(uid, item.id)
                }
            }
    }

    private fun deleteDocument(dId: String, itemId: String) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(dId)
            .collection("program")
            .document(itemId)
            .delete()
    }

    private fun reactivateUserSubscription(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 29)
        val time = calendar.timeInMillis
        db.collection("users")
            .document(uid)
            .update(mapOf("sub_end" to time))

        binding.subEnd.text = getString(R.string.sub_ends_in, getFormattedDate(time))
    }
}