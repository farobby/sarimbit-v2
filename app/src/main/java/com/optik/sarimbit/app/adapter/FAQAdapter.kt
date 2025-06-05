package com.optik.sarimbit.app.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.databinding.ItemQuestionBinding

class FAQAdapter(
    private val questions: List<String>,
    private val onQuestionClick: (String) -> Unit
) : RecyclerView.Adapter<FAQAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: String, onQuestionClick: (String) -> Unit) {
            binding.txtQuestion.text = question
            binding.root.setOnClickListener { onQuestionClick(question) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(questions[position], onQuestionClick)
    }

    override fun getItemCount() = questions.size
}
