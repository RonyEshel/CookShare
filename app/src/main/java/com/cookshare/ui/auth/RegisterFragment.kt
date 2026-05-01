package com.cookshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyMasking(binding.etPassword)
        applyMasking(binding.etConfirmPassword)

        binding.btnRegister.setOnClickListener {
            val name = binding.etDisplayName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            binding.tilDisplayName.error = null
            binding.tilEmail.error = null
            binding.tilConfirmPassword.error = null
            binding.tilPassword.error = null
            if (name.isBlank()) {
                binding.tilDisplayName.error = "Display name is required"
                binding.etDisplayName.requestFocus()
                return@setOnClickListener
            }
            if (email.isBlank()) {
                binding.tilEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Passwords don't match"
                binding.etConfirmPassword.requestFocus()
                return@setOnClickListener
            }
            authViewModel.register(email, password, name)
        }

        binding.tvGoToLogin.setOnClickListener { findNavController().navigateUp() }

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Authenticated -> findNavController().navigate(R.id.action_registerFragment_to_feedFragment)
                is AuthState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
        }
    }

    private fun applyMasking(et: com.google.android.material.textfield.TextInputEditText) {
        val masker = android.text.method.PasswordTransformationMethod.getInstance()
        et.transformationMethod = masker
        et.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (et.transformationMethod !== masker) et.transformationMethod = masker
            }
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}