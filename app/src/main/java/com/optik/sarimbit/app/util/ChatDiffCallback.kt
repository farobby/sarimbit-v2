package com.optik.sarimbit.app.util
import androidx.recyclerview.widget.DiffUtil
import com.optik.sarimbit.app.model.ChatModel

class ChatDiffCallback : DiffUtil.ItemCallback<ChatModel>() {
    override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        return oldItem.message == newItem.message && oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        return oldItem == newItem
    }
}
