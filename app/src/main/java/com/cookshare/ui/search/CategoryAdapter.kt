package com.cookshare.ui.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onClick: (RecipeCategory) -> Unit
) : ListAdapter<RecipeCategory, CategoryAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecipeCategory>() {
            override fun areItemsTheSame(a: RecipeCategory, b: RecipeCategory) = a.name == b.name
            override fun areContentsTheSame(a: RecipeCategory, b: RecipeCategory) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: RecipeCategory) {
            binding.tvCategoryName.text = category.name
            binding.tvCategoryEmoji.text = category.emoji
            binding.categoryBg.setBackgroundColor(Color.parseColor(category.colorHex))
            binding.root.setOnClickListener { onClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
