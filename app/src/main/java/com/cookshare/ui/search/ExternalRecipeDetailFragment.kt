package com.cookshare.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cookshare.R
import com.cookshare.databinding.FragmentExternalRecipeDetailBinding
import com.squareup.picasso.Picasso

class ExternalRecipeDetailFragment : Fragment() {

    private var _binding: FragmentExternalRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExternalRecipeDetailViewModel by viewModels()
    private val args: ExternalRecipeDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExternalRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mealId = args.mealId.ifBlank { null } ?: run {
            findNavController().navigateUp()
            return
        }

        viewModel.loadMeal(mealId)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.meal.observe(viewLifecycleOwner) { meal ->
            meal ?: return@observe
            binding.contentLayout.visibility = View.VISIBLE

            binding.tvMealName.text = meal.name
            binding.tvMealCategory.text = meal.category
            binding.tvMealArea.text = meal.area

            if (meal.thumbnailUrl.isNotEmpty()) {
                Picasso.get()
                    .load(meal.thumbnailUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .fit().centerCrop()
                    .into(binding.ivMealImage)
            }

            val ingredients = meal.getIngredientsList()
            val measures = meal.getMeasuresList()
            val ingredientsText = ingredients.mapIndexed { i, ingredient ->
                val measure = measures.getOrNull(i) ?: ""
                if (measure.isNotBlank()) "• $measure $ingredient" else "• $ingredient"
            }.joinToString("\n")
            binding.tvIngredients.text = ingredientsText.ifEmpty { "No ingredients listed" }

            binding.tvInstructions.text = meal.instructions.ifEmpty { "No instructions listed" }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
