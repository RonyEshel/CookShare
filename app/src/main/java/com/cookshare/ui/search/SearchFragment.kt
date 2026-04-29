package com.cookshare.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookshare.R
import com.cookshare.databinding.FragmentSearchBinding
import com.cookshare.ui.feed.RecipeAdapter
import com.google.android.material.tabs.TabLayout

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var communityAdapter: RecipeAdapter
    private lateinit var mealAdapter: MealAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var isCommunityTab = true
    private var hasActiveFilter = false  // true when category selected or search text entered

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communityAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = SearchFragmentDirections
                    .actionSearchFragmentToRecipeDetailFragment(recipeId = recipe.id)
                findNavController().navigate(action)
            },
            onSaveClick = { recipe -> viewModel.toggleSave(recipe) }
        )

        mealAdapter = MealAdapter { meal ->
            val action = SearchFragmentDirections
                .actionSearchFragmentToExternalRecipeDetailFragment(mealId = meal.id)
            findNavController().navigate(action)
        }

        categoryAdapter = CategoryAdapter { category ->
            hasActiveFilter = true
            showResultsView()
            if (isCommunityTab) {
                viewModel.selectCommunityCategory(category.name)
            } else {
                viewModel.selectGlobalCategory(category.name)
            }
        }

        binding.recyclerViewSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = communityAdapter
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }
        categoryAdapter.submitList(CommunityCategories.list)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isCommunityTab = tab?.position == 0
                binding.recyclerViewSearch.adapter = if (isCommunityTab) communityAdapter else mealAdapter
                categoryAdapter.submitList(
                    if (isCommunityTab) CommunityCategories.list else GlobalCategories.list
                )
                // Reset filter when switching tabs
                hasActiveFilter = false
                viewModel.selectCommunityCategory(null)
                viewModel.selectGlobalCategory(null)
                binding.etSearch.setText("")
                showCategoriesView()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                hasActiveFilter = query.isNotBlank()
                if (hasActiveFilter) {
                    showResultsView()
                    if (isCommunityTab) viewModel.searchCommunity(query)
                    else viewModel.searchMealDb(query)
                } else {
                    viewModel.selectCommunityCategory(null)
                    viewModel.selectGlobalCategory(null)
                    showCategoriesView()
                }
            }
        })

        viewModel.communityResults.observe(viewLifecycleOwner) { recipes ->
            communityAdapter.submitList(recipes)
            if (isCommunityTab && hasActiveFilter) {
                binding.tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.savedRecipeIds().observe(viewLifecycleOwner) { ids ->
            communityAdapter.updateSavedIds(ids.toSet())
        }

        viewModel.mealDbResults.observe(viewLifecycleOwner) { meals ->
            mealAdapter.submitList(meals)
            if (!isCommunityTab && hasActiveFilter) {
                binding.tvEmptyState.visibility = if (meals.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun showCategoriesView() {
        binding.rvCategories.visibility = View.VISIBLE
        binding.recyclerViewSearch.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
    }

    private fun showResultsView() {
        binding.rvCategories.visibility = View.GONE
        binding.recyclerViewSearch.visibility = View.VISIBLE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
