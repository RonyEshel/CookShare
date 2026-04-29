package com.cookshare.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.R
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.User
import com.cookshare.databinding.ItemRecipeBinding
import com.squareup.picasso.Picasso

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,
    private val onSaveClick: ((Recipe) -> Unit)? = null
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    private var savedIds: Set<String> = emptySet()
    private var liveUsers: Map<String, User> = emptyMap()

    fun updateSavedIds(ids: Set<String>) {
        savedIds = ids
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateLiveUsers(users: Map<String, User>) {
        liveUsers = users
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) { holder.bind(getItem(position)) }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            val live = liveUsers[recipe.authorId]
            val authorName = live?.displayName?.takeIf { it.isNotBlank() } ?: recipe.authorName
            binding.tvRecipeTitle.text = recipe.title
            binding.tvRecipeAuthor.text = "by $authorName"
            binding.tvRecipeCategory.text = recipe.category
            binding.tvCookingTime.text = "${recipe.cookingTime} min"

            binding.ivRecipeImage.alpha = 0f
            val url = recipe.imageUrl
            when {
                url.isEmpty() -> {
                    binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
                    binding.ivRecipeImage.alpha = 1f
                }
                url.startsWith("http") -> {
                    Picasso.get().load(url)
                        .placeholder(R.drawable.placeholder_recipe)
                        .error(R.drawable.placeholder_recipe)
                        .fit().centerCrop()
                        .into(binding.ivRecipeImage, object : com.squareup.picasso.Callback {
                            override fun onSuccess() { binding.ivRecipeImage.animate().alpha(1f).setDuration(300).start() }
                            override fun onError(e: Exception?) { binding.ivRecipeImage.alpha = 1f }
                        })
                }
                else -> {
                    try {
                        val bytes = android.util.Base64.decode(url, android.util.Base64.NO_WRAP)
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            binding.ivRecipeImage.setImageBitmap(bmp)
                        } else {
                            binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
                        }
                    } catch (_: Exception) {
                        binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
                    }
                    binding.ivRecipeImage.alpha = 1f
                }
            }

            val isSaved = savedIds.contains(recipe.id)
            binding.btnBookmark.setImageResource(
                if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            )

            binding.btnBookmark.setOnClickListener { onSaveClick?.invoke(recipe) }
            binding.root.setOnClickListener { onRecipeClick(recipe) }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
}
