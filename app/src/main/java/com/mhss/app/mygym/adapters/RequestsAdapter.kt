package com.mhss.app.mygym.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.GymRequestItemBinding

class RequestsAdapter(
    private val onAcceptClicked: (User) -> Unit,
    private val onRemoveClicked: (User) -> Unit
)
    : ListAdapter<User, RequestsAdapter.RequestsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
        return RequestsViewHolder(
            GymRequestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RequestsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class RequestsViewHolder(private var binding: GymRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
               title.text = user.name
                acceptBtn.setOnClickListener {
                    root.visibility = View.GONE
                    onAcceptClicked(user)
                }
                removeBtn.setOnClickListener {
                    root.visibility = View.GONE
                    onRemoveClicked(user)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }

}