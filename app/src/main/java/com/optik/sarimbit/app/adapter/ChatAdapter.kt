package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.ChatModel
import com.optik.sarimbit.app.util.ChatDiffCallback
import com.optik.sarimbit.databinding.ItemChatReceiverBinding

class ChatAdapter(
    val onQuestionClick: (String) -> Unit
) : ListAdapter<ChatModel, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSender) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENDER) {
            val view = inflater.inflate(R.layout.item_chat_sender, parent, false)
            SenderViewHolder(view)
        } else {
            val binding =
                ItemChatReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceiverViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        val showDateTime =
            position == 0 || message.isSender != message.isSender

        if (holder is SenderViewHolder) {
            holder.bind(message, showDateTime)
        } else if (holder is ReceiverViewHolder) {
            holder.bind(message)
        }
    }


    inner class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)

        fun bind(message: ChatModel, showDateTime: Boolean) {
            tvMessage.text = message.message
            tvDateTime.text = message.time
            tvDateTime.visibility = if (showDateTime) View.VISIBLE else View.GONE
        }
    }

    inner class ReceiverViewHolder(private val binding: ItemChatReceiverBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatModel) {
            binding.tvMessage.text = message.message
            binding.tvDateTime.text = message.time

            if (message.questionList!!.isNotEmpty()) {
                binding.llQuestions.visibility = View.VISIBLE
                binding.llMessage.visibility = View.GONE
                binding.recyclerQuestions.layoutManager = LinearLayoutManager(binding.root.context)
                binding.recyclerQuestions.adapter =
                    FAQAdapter(message.questionList!!, onQuestionClick)
            } else {
                binding.llMessage.visibility = View.VISIBLE
                binding.llQuestions.visibility = View.GONE
            }
        }
    }
}
