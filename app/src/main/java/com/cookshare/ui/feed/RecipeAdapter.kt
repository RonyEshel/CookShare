package com.cookshare.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.R
import com.cookshare.data.model.Recipe
import com.cookshare.databinding.ItemRecipeBinding
import com.squareup.picasso.Picasso

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) { holder.bind(getItem(position)) }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.tvRecipeTitle.text = recipe.title
            binding.tvRecipeAuthor.text = "by ${recipe.authorName}"
            binding.tvRecipeCategory.text = recipe.category
            binding.tvCookingTime.text = "${recipe.cookingTime} min"
            if (recipe.imageUrl.isNotEmpty()) {
                Picasso.get().load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .fit().centerCrop()
                    .into(binding.ivRecipeImage)
            } else {
                binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
            }
            binding.root.setOnClickListener { onRecipeClick(recipe) }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
}