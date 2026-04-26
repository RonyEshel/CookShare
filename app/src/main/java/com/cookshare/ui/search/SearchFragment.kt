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

    private var isCommunityTab = true
    private var communityObserverAttached = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communityAdapter = RecipeAdapter { recipe ->
            val bundle = Bundle().apply { putString("recipeId", recipe.id) }
            findNavController().navigate(R.id.action_searchFragment_to_recipeDetailFragment, bundle)
        }

        mealAdapter = MealAdapter { meal ->
            val bundle = Bundle().apply { putString("mealId", meal.id) }
            findNavController().navigate(R.id.action_searchFragment_to_externalRecipeDetailFragment, bundle)
        }

        binding.recyclerViewSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = communityAdapter
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isCommunityTab = tab?.position == 0
                binding.recyclerViewSearch.adapter = if (isCommunityTab) communityAdapter else mealAdapter
                triggerSearch(binding.etSearch.text.toString().trim())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                triggerSearch(s.toString().trim())
            }
        })

        viewModel.mealDbResults.observe(viewLifecycleOwner) { meals ->
            if (!isCommunityTab) {
                mealAdapter.submitList(meals)
                binding.tvEmptyState.visibility =
                    if (meals.isEmpty() && binding.etSearch.text.toString().isNotBlank()) View.VISIBLE else View.GONE
                if (meals.isEmpty() && binding.etSearch.text.toString().isBlank()) {
                    binding.tvEmptyState.text = "Type to search recipes"
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else if (meals.isEmpty()) {
                    binding.tvEmptyState.text = "No results found"
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        binding.tvEmptyState.text = "Type to search recipes"
        binding.tvEmptyState.visibility = View.VISIBLE
    }

    private fun triggerSearch(query: String) {
        if (isCommunityTab) {
            viewModel.searchCommunity(query) { liveData ->
                if (!communityObserverAttached) {
                    communityObserverAttached = true
                }
                liveData.observe(viewLifecycleOwner) { recipes ->
                    if (isCommunityTab) {
                        communityAdapter.submitList(recipes)
                        if (query.isBlank()) {
                            binding.tvEmptyState.text = "Type to search recipes"
                            binding.tvEmptyState.visibility = View.VISIBLE
                        } else if (recipes.isEmpty()) {
                            binding.tvEmptyState.text = "No results found"
                            binding.tvEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.tvEmptyState.visibility = View.GONE
                        }
                    }
                }
            }
        } else {
            viewModel.searchMealDb(query)
            if (query.isBlank()) {
                binding.tvEmptyState.text = "Type to search recipes"
                binding.tvEmptyState.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
