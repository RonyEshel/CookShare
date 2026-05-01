package com.cookshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookshare.R
import com.cookshare.data.model.Recipe
import com.cookshare.databinding.FragmentSavedRecipesBinding
import com.cookshare.ui.feed.RecipeAdapter

class SavedRecipesFragment : Fragment() {

    private var _binding: FragmentSavedRecipesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SavedRecipesViewModel by viewModels()
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = SavedRecipesFragmentDirections
                    .actionSavedRecipesFragmentToRecipeDetailFragment(recipeId = recipe.id)
                findNavController().navigate(action)
            },
            onSaveClick = { recipe -> viewModel.unsave(recipe.id) }
        )

        binding.recyclerViewSaved.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SavedRecipesFragment.adapter
        }

        viewModel.savedRecipes.observe(viewLifecycleOwner) { savedList ->
            val recipes = savedList.map { s ->
                Recipe(
                    id = s.recipeId,
                    title = s.recipeTitle,
                    imageUrl = s.recipeImageUrl,
                    category = s.listName
                )
            }
            adapter.submitList(recipes)
            adapter.updateSavedIds(savedList.map { it.recipeId }.toSet())
            binding.tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewSaved.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
