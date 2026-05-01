package com.cookshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.databinding.FragmentLandingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LandingFragment : Fragment() {

    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.lottieAnimation.setAnimation(R.raw.woman_cooking)
            binding.lottieAnimation.playAnimation()
        } catch (e: Exception) {
            binding.lottieAnimation.visibility = View.GONE
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Show something immediately, then upgrade to real name from Firestore
            val initial = currentUser.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser.email?.substringBefore("@")
                ?: "Chef"
            renderLoggedInState(initial)

            if (currentUser.displayName.isNullOrBlank()) {
                lifecycleScope.launch {
                    val name = FirebaseManager().getUserProfile(currentUser.uid)
                        .getOrNull()?.displayName?.takeIf { it.isNotBlank() }
                    if (name != null && _binding != null) {
                        binding.tvSubtitle.text = "Hey, $name 👋"
                    }
                }
            }
        } else {
            renderLoggedOutState()
        }

        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_loginFragment)
        }

        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_registerFragment)
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_feedFragment)
        }

        binding.btnSignOut.setOnClickListener {
            authViewModel.logout()
            renderLoggedOutState()
        }
    }

    private fun renderLoggedInState(name: String) {
        binding.tvSubtitle.text = "Hey, $name 👋"
        binding.featurePills.visibility = View.GONE
        binding.btnCreateAccount.visibility = View.GONE
        binding.btnSignIn.visibility = View.GONE
        binding.btnContinue.visibility = View.VISIBLE
        binding.btnSignOut.visibility = View.VISIBLE
    }

    private fun renderLoggedOutState() {
        binding.tvSubtitle.text = "Your recipes. Your community."
        binding.featurePills.visibility = View.VISIBLE
        binding.btnCreateAccount.visibility = View.VISIBLE
        binding.btnSignIn.visibility = View.VISIBLE
        binding.btnContinue.visibility = View.GONE
        binding.btnSignOut.visibility = View.GONE
    }

    override fun onDestroyView() {
        _binding?.lottieAnimation?.cancelAnimation()
        super.onDestroyView()
        _binding = null
    }
}
