package com.cookshare.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.R
import com.cookshare.data.remote.api.MealDto
import com.cookshare.databinding.ItemMealBinding
import com.squareup.picasso.Picasso

class MealAdapter(
    private val onMealClick: (MealDto) -> Unit
) : ListAdapter<MealDto, MealAdapter.MealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(private val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: MealDto) {
            binding.tvMealName.text = meal.name
            binding.tvMealCategory.text = meal.category.ifEmpty { meal.area }
            if (meal.thumbnailUrl.isNotEmpty()) {
                Picasso.get()
                    .load(meal.thumbnailUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .fit().centerCrop()
                    .into(binding.ivMealImage)
            } else {
                binding.ivMealImage.setImageResource(R.drawable.placeholder_recipe)
            }
            binding.root.setOnClickListener { onMealClick(meal) }
        }
    }

    class MealDiffCallback : DiffUtil.ItemCallback<MealDto>() {
        override fun areItemsTheSame(oldItem: MealDto, newItem: MealDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MealDto, newItem: MealDto) = oldItem == newItem
    }
}
