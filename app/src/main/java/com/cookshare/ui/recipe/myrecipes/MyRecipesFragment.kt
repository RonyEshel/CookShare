package com.cookshare.ui.recipe.myrecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookshare.R
import com.cookshare.databinding.FragmentMyRecipesBinding
import com.cookshare.ui.feed.RecipeAdapter

class MyRecipesFragment : Fragment() {

    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = MyRecipesFragmentDirections
                    .actionMyRecipesFragmentToRecipeDetailFragment(recipeId = recipe.id)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewMyRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshMyRecipes() }

        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_myRecipesFragment_to_editRecipeFragment)
        }

        viewModel.init()

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewMyRecipes.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE
            binding.tvRecipeCountSubtitle.text = when (recipes.size) {
                0 -> "Your culinary collection"
                1 -> "1 recipe shared"
                else -> "${recipes.size} recipes shared"
            }
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefreshLayout.isRefreshing = refreshing
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
