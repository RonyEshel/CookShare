package com.cookshare.ui.feed

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
import com.cookshare.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeAdapter = RecipeAdapter { recipe ->
            val bundle = Bundle().apply { putString("recipeId", recipe.id) }
            findNavController().navigate(R.id.action_feedFragment_to_recipeDetailFragment, bundle)
        }

        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshRecipes() }

        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_editRecipeFragment)
        }

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
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