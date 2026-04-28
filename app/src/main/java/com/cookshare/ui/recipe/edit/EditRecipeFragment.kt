package com.cookshare.ui.recipe.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.databinding.FragmentEditRecipeBinding
import com.squareup.picasso.Picasso

class EditRecipeFragment : Fragment() {

    private var _binding: FragmentEditRecipeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditRecipeViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.imageUri = it
            _binding?.ivRecipeImage?.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Drink")
        binding.spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)

        val recipeId = arguments?.getString("recipeId")
        viewModel.loadRecipe(recipeId)

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                binding.etTitle.setText(it.title)
                binding.etDescription.setText(it.description)
                binding.etIngredients.setText(it.ingredients)
                binding.etInstructions.setText(it.instructions)
                binding.etCookingTime.setText(it.cookingTime.toString())
                val catIndex = categories.indexOf(it.category)
                if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)
                if (it.imageUrl.isNotEmpty()) {
                    Picasso.get().load(it.imageUrl).placeholder(R.drawable.placeholder_recipe).into(binding.ivRecipeImage)
                }
                binding.tvScreenTitle.text = "Edit Recipe"
            }
        }

        binding.btnSelectImage.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSave.setOnClickListener {
            viewModel.saveRecipe(
                title = binding.etTitle.text.toString().trim(),
                description = binding.etDescription.text.toString().trim(),
                ingredients = binding.etIngredients.text.toString().trim(),
                instructions = binding.etInstructions.text.toString().trim(),
                category = binding.spinnerCategory.selectedItem.toString(),
                cookingTime = binding.etCookingTime.text.toString().toIntOrNull() ?: 0
            )
        }

        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !loading
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess {
                    Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}