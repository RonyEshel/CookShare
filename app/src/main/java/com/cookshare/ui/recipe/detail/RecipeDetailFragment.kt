package com.cookshare.ui.recipe.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.databinding.FragmentRecipeDetailBinding
import com.squareup.picasso.Picasso

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = arguments?.getString("recipeId") ?: run {
            findNavController().navigateUp()
            return
        }

        viewModel.loadRecipe(recipeId)

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

            if (recipe.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .fit().centerCrop()
                    .into(binding.ivRecipeImage)
            }

            if (recipe.authorId == viewModel.currentUserId) {
                binding.layoutOwnerActions.visibility = View.VISIBLE
                binding.btnEdit.setOnClickListener {
                    val bundle = Bundle().apply { putString("recipeId", recipe.id) }
                    findNavController().navigate(R.id.action_recipeDetailFragment_to_editRecipeFragment, bundle)
                }
                binding.btnDelete.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete \"${recipe.title}\"?")
                        .setPositiveButton("Delete") { _, _ -> viewModel.deleteRecipe(recipe.id) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
