package com.optik.sarimbit.app.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.optik.sarimbit.R

class CategoryAdminAdapter(
    private val categories: List<String>,
    private val onClick: (selectedCategories: List<String>) -> Unit
) : RecyclerView.Adapter<CategoryAdminAdapter.CategoryViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()

    fun getSelectedCategories(): ArrayList<String> {
        return selectedPositions.map { categories[it] } as ArrayList<String>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], selectedPositions.contains(position))

        holder.itemView.setOnClickListener {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position)
            } else {
                selectedPositions.add(position)
            }
            notifyItemChanged(position)
            onClick(getSelectedCategories())
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: String, isSelected: Boolean) {
            categoryName.text = category
            categoryName.isSelected = isSelected

            // Optional: Change background or style to reflect selection
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_category_selected else R.drawable.bg_category_unselected
            )
        }
    }
}
