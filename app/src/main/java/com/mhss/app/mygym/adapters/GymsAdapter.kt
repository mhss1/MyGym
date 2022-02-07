package com.mhss.app.mygym.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhss.app.mygym.data.Gym
import com.mhss.app.mygym.databinding.RecItemBinding

class GymsAdapter(
    private val onItemClicked: (Gym) -> Unit
) : ListAdapter<Gym, GymsAdapter.GymViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GymViewHolder {
        return GymViewHolder(
            RecItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: GymViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class GymViewHolder(private var binding: RecItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gym: Gym) {
            binding.apply {
                title.text = gym.name
                root.setOnClickListener { onItemClicked(gym) }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Gym>() {
            override fun areItemsTheSame(oldItem: Gym, newItem: Gym): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Gym, newItem: Gym): Boolean {
                return oldItem == newItem
            }
        }
    }
}