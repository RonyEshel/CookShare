package com.cookshare.ui.recipe.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookshare.R
import com.cookshare.databinding.FragmentRecipeDetailBinding
import com.squareup.picasso.Picasso

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeDetailViewModel by viewModels()
    private val args: RecipeDetailFragmentArgs by navArgs()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = args.recipeId

        commentAdapter = CommentAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
            isNestedScrollingEnabled = false
        }

        viewModel.loadRecipe(recipeId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnPostComment.setOnClickListener {
            val text = binding.etComment.text.toString()
            if (text.isBlank()) return@setOnClickListener
            viewModel.postComment(recipeId, text)
            binding.etComment.setText("")
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe ?: return@observe
            binding.contentLayout.visibility = View.VISIBLE

            binding.tvTitle.text = recipe.title
            binding.tvAuthor.text = "by ${recipe.authorName}"
            binding.tvCategory.text = recipe.category
            binding.tvCookingTime.text = "${recipe.cookingTime} min"
            binding.tvDescription.text = recipe.description
            binding.tvIngredients.text = recipe.ingredients
            binding.tvInstructions.text = recipe.instructions

            renderRecipeImage(recipe.imageUrl)

            val isOwner = recipe.authorId == viewModel.currentUserId
            if (isOwner) {
                binding.layoutOwnerActions.visibility = View.VISIBLE
                binding.btnEdit.setOnClickListener {
                    val action = RecipeDetailFragmentDirections
                        .actionRecipeDetailFragmentToEditRecipeFragment(recipeId = recipe.id)
                    findNavController().navigate(action)
                }
                binding.btnDelete.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete \"${recipe.title}\"?")
                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteRecipe(recipe.id) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else if (recipe.authorId.isNotEmpty() && viewModel.currentUserId.isNotEmpty()) {
                binding.btnMessageAuthor.visibility = View.VISIBLE
                binding.btnMessageAuthor.setOnClickListener {
                    viewModel.startConversationWithAuthor(recipe.authorId, recipe.authorName)
                }
                binding.btnFollowAuthor.visibility = View.VISIBLE
                viewModel.checkFollowStatus(recipe.authorId)
                binding.btnFollowAuthor.setOnClickListener {
                    viewModel.toggleFollow(recipe.authorId)
                }
            }
        }

        viewModel.isFollowing.observe(viewLifecycleOwner) { following ->
            following ?: return@observe
            binding.btnFollowAuthor.text = if (following) "Following" else "Follow"
            binding.btnFollowAuthor.backgroundTintList = ColorStateList.valueOf(
                if (following) ContextCompat.getColor(requireContext(), R.color.text_secondary)
                else ContextCompat.getColor(requireContext(), R.color.primary)
            )
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentAdapter.submitList(comments)
            binding.tvNoComments.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            commentAdapter.updateUsers(users)
            // Also refresh the recipe's "by Chef X" with live displayName
            viewModel.recipe.value?.let { recipe ->
                val live = users[recipe.authorId]
                val name = live?.displayName?.takeIf { it.isNotBlank() } ?: recipe.authorName
                binding.tvAuthor.text = "by $name"
            }
        }

        viewModel.conversationReady.observe(viewLifecycleOwner) { pair ->
            pair ?: return@observe
            val action = RecipeDetailFragmentDirections
                .actionRecipeDetailFragmentToChatFragment(
                    conversationId = pair.first,
                    otherUserName = pair.second
                )
            findNavController().navigate(action)
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderRecipeImage(url: String) {
        if (url.isEmpty()) {
            binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
            return
        }
        if (url.startsWith("http")) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.placeholder_recipe)
                .error(R.drawable.placeholder_recipe)
                .fit().centerCrop()
                .into(binding.ivRecipeImage)
        } else {
            try {
                val bytes = android.util.Base64.decode(url, android.util.Base64.NO_WRAP)
                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) binding.ivRecipeImage.setImageBitmap(bmp)
            } catch (_: Exception) {
                binding.ivRecipeImage.setImageResource(R.drawable.placeholder_recipe)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipe(args.recipeId)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
