package com.optik.sarimbit.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.VariantModel

class FrameAdapter(private val categories: List<VariantModel>,
     val onclick: (variantModel: VariantModel) -> Unit
    ) :
    RecyclerView.Adapter<FrameAdapter.ColorsViewHolder>() {

    private var selectedPosition = 0
    fun getSelectedFrame() = categories[selectedPosition]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_category, parent, false)
        return ColorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorsViewHolder, position: Int) {
        holder.bind(categories[holder.layoutPosition].name, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onclick(categories[position])
        }
    }

    override fun getItemCount(): Int = categories.size

    class ColorsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: String, isSelected: Boolean) {
            categoryName.text = category
            categoryName.isSelected = isSelected
        }
    }
}
