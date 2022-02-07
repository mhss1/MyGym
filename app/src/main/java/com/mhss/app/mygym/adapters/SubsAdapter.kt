package com.mhss.app.mygym.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mhss.app.mygym.R
import com.mhss.app.mygym.data.User
import com.mhss.app.mygym.databinding.RecItemBinding
import java.text.SimpleDateFormat
import java.util.*

class SubsAdapter(
    private val onItemClicked: (User) -> Unit
)
    : ListAdapter<User, SubsAdapter.SubsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubsViewHolder {
        return SubsViewHolder(
            RecItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SubsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class SubsViewHolder(private var binding: RecItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                title.text = user.name
                secondaryTitle.text =
                    root.context.getString(R.string.sub_ends_in, getFormattedDate(user.sub_end))
                if (!isActiveSub(user.sub_end))
                    constraint.background = ContextCompat.getDrawable(root.context,
                        R.drawable.gradient_background_red
                    )

                root.setOnClickListener { onItemClicked(user) }
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
}