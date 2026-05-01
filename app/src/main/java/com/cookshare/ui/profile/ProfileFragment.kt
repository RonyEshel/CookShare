package com.cookshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.databinding.FragmentProfileBinding
import com.cookshare.ui.auth.AuthViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.pendingImageUri = it
            _binding?.ivProfilePhoto?.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showViewMode()
        viewModel.loadProfile()
        viewModel.loadStats()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.etDisplayName.setText(user.displayName)
            binding.etEmail.setText(user.email)
            binding.tvHeaderName.text = user.displayName
            binding.tvHeaderEmail.text = user.email
            renderProfileImage(user.profileImageUrl)
        }

        viewModel.recipeCount.observe(viewLifecycleOwner) { binding.tvRecipeCount.text = it.toString() }
        viewModel.savedCount.observe(viewLifecycleOwner) { binding.tvSavedCount.text = it.toString() }
        viewModel.followingCount.observe(viewLifecycleOwner) { binding.tvFollowingCount.text = it.toString() }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSaveProfile.isEnabled = !loading
            binding.btnLogout.isEnabled = !loading
            binding.btnEditProfile.isEnabled = !loading
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                showViewMode()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditProfile.setOnClickListener { showEditMode() }

        binding.btnCancelEdit.setOnClickListener {
            viewModel.pendingImageUri = null
            viewModel.user.value?.let {
                binding.etDisplayName.setText(it.displayName)
                renderProfileImage(it.profileImageUrl)
            }
            showViewMode()
        }

        binding.btnSavedRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_savedRecipesFragment)
        }

        binding.btnChangePhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etDisplayName.text.toString().trim()
            if (name.isBlank()) {
                binding.tilDisplayName.error = "Display name cannot be empty"
                return@setOnClickListener
            }
            binding.tilDisplayName.error = null
            viewModel.saveProfile(name)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun showViewMode() {
        binding.viewModeContainer.visibility = View.VISIBLE
        binding.editModeCard.visibility = View.GONE
        binding.btnChangePhoto.visibility = View.GONE
        binding.tvHeaderEmail.visibility = View.VISIBLE
    }

    private fun showEditMode() {
        binding.viewModeContainer.visibility = View.GONE
        binding.editModeCard.visibility = View.VISIBLE
        binding.btnChangePhoto.visibility = View.VISIBLE
        binding.tvHeaderEmail.visibility = View.GONE
    }

    private fun renderProfileImage(url: String) {
        if (url.isEmpty()) {
            binding.ivProfilePhoto.setImageResource(R.drawable.placeholder_profile)
            return
        }
        if (url.startsWith("http")) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.placeholder_profile)
                .error(R.drawable.placeholder_profile)
                .into(binding.ivProfilePhoto)
        } else {
            try {
                val bytes = android.util.Base64.decode(url, android.util.Base64.NO_WRAP)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) binding.ivProfilePhoto.setImageBitmap(bitmap)
            } catch (_: Exception) {
                binding.ivProfilePhoto.setImageResource(R.drawable.placeholder_profile)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
