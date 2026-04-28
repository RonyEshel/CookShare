package com.cookshare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cookshare.R
import com.cookshare.databinding.FragmentLandingBinding

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

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is AuthState.Authenticated) {
                findNavController().navigate(R.id.action_landingFragment_to_feedFragment)
            }
        }

        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_loginFragment)
        }

        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        _binding?.lottieAnimation?.cancelAnimation()
        super.onDestroyView()
        _binding = null
    }
}
