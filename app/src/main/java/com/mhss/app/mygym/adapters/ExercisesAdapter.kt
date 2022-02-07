package com.mhss.app.mygym.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhss.app.mygym.data.Exercise
import com.mhss.app.mygym.databinding.RecItemBinding

class ExercisesAdapter
    : ListAdapter<Exercise, ExercisesAdapter.ExerciseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        return ExerciseViewHolder(
            RecItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class ExerciseViewHolder(private var binding: RecItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ex: Exercise) {
                binding.apply {
                    title.text = ex.name
                    secondaryTitle.text = ex.sets
                }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Exercise>() {
            override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
                return (oldItem.name + oldItem.date) == ( newItem.name + newItem.date)
            }

            override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
                return oldItem == newItem
            }
        }
    }
}