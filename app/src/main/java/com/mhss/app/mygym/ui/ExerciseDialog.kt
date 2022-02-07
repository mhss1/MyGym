package com.mhss.app.mygym.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mhss.app.mygym.R
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.CustomExerciseDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap


class ExerciseDialog(private val user: User) : DialogFragment() {

    private var binding: CustomExerciseDialogBinding? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CustomExerciseDialogBinding.inflate(LayoutInflater.from(context))
        db = Firebase.firestore

        binding?.apply {

            cancelBtn.setOnClickListener {
                dismiss()
            }

            saveBtn.setOnClickListener {
                val title = exName.text.toString()
                val sets = sets.text.toString()

                if (title.isBlank() || sets.isBlank()) {
                    toast(getString(R.string.complete_fields))
                    return@setOnClickListener
                }
                addExercise(
                    hashMapOf(
                        "name" to title,
                        "sets" to sets,
                        "date" to getDateFromDatePicker(datePicker)
                    )
                )
                dismiss()
            }

        }
        return AlertDialog.Builder(requireActivity())
            .setView(binding!!.root)
            .create()
    }

    private fun getDateFromDatePicker(dp: DatePicker): Long {
        val day = dp.dayOfMonth
        val month = dp.month
        val year = dp.year

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }

    private fun addExercise(data: HashMap<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        db.collection("users")
            .document(user.id)
            .collection("program")
            .document()
            .set(data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun toast(text: String) =
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
}