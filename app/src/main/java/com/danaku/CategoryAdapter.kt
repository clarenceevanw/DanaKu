package com.danaku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danaku.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    fun updateData(newCategories: List<Category>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            val context = binding.root.context
            binding.tvCategoryName.text = category.name

            if (category.type == TransactionType.INCOME) {
                binding.tvCategoryTypeBadge.text = context.getString(R.string.title_income)
                binding.tvCategoryTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.neon_green))
                binding.tvCategoryTypeBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.income_green_badge_bg))
            } else {
                binding.tvCategoryTypeBadge.text = context.getString(R.string.title_expense)
                binding.tvCategoryTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
                binding.tvCategoryTypeBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.expense_red_badge_bg))
            }

            binding.btnEdit.setOnClickListener { onEditClick(category) }
            binding.btnDelete.setOnClickListener { onDeleteClick(category) }
        }
    }
}
