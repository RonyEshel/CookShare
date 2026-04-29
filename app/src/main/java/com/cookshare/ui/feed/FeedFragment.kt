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
    private var shimmerShown = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = FeedFragmentDirections
                    .actionFeedFragmentToRecipeDetailFragment(recipeId = recipe.id)
                findNavController().navigate(action)
            },
            onSaveClick = { recipe -> viewModel.toggleSave(recipe) }
        )

        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }

        binding.shimmerLayout.startShimmer()

        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshRecipes() }

        binding.chipGroupFeedMode.setOnCheckedStateChangeListener { _, checkedIds ->
            val mode = if (R.id.chipFollowing in checkedIds) "following" else "everyone"
            viewModel.setFeedMode(mode)
        }

        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_editRecipeFragment)
        }

        viewModel.displayRecipes.observe(viewLifecycleOwner) { recipes ->
            if (shimmerShown) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                shimmerShown = false
            }
            recipeAdapter.submitList(recipes)
            binding.tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            viewModel.refreshLiveUsers()
        }

        viewModel.savedRecipeIds().observe(viewLifecycleOwner) { ids ->
            recipeAdapter.updateSavedIds(ids.toSet())
        }

        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            recipeAdapter.updateLiveUsers(users)
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefreshLayout.isRefreshing = refreshing
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFollowingIds()
        viewModel.refreshLiveUsers()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
