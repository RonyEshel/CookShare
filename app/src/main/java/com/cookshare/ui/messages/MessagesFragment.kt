package com.cookshare.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookshare.R
import com.cookshare.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MessagesViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ConversationAdapter(viewModel.currentUserId) { conversation ->
            val otherName = conversation.getOtherUserName(viewModel.currentUserId)
            val action = MessagesFragmentDirections
                .actionMessagesFragmentToChatFragment(
                    conversationId = conversation.id,
                    otherUserName = otherName
                )
            findNavController().navigate(action)
        }

        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessagesFragment.adapter
        }

        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            adapter.submitList(conversations)
            binding.tvEmptyState.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.startConvResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess { (convId, otherName) ->
                if (!isAdded || _binding == null) return@onSuccess
                val action = MessagesFragmentDirections
                    .actionMessagesFragmentToChatFragment(
                        conversationId = convId,
                        otherUserName = otherName.ifBlank { "Chef" }
                    )
                findNavController().navigate(action)
            }.onFailure { e ->
                android.widget.Toast.makeText(
                    requireContext(),
                    "Could not start chat: ${e.message ?: "unknown error"}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            viewModel.clearStartConvResult()
        }

        binding.fabNewMessage.setOnClickListener { showNewMessageDialog() }
    }

    private fun showNewMessageDialog() {
        val ctx = requireContext()
        val input = android.widget.EditText(ctx).apply {
            hint = "user@example.com"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or android.text.InputType.TYPE_CLASS_TEXT
            setPadding(48, 32, 48, 32)
        }
        val container = android.widget.FrameLayout(ctx).apply {
            setPadding(40, 16, 40, 0)
            addView(input)
        }
        AlertDialog.Builder(ctx)
            .setTitle("New Message")
            .setMessage("Enter the email of a registered CookShare user")
            .setView(container)
            .setPositiveButton("Start chat") { _, _ ->
                val email = input.text.toString().trim().lowercase()
                if (email.isBlank()) return@setPositiveButton
                if (email == viewModel.currentUserEmail.lowercase()) {
                    android.widget.Toast.makeText(ctx, "You can't message yourself", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val match = viewModel.users.value.orEmpty().firstOrNull { it.email.equals(email, ignoreCase = true) }
                if (match == null) {
                    android.widget.Toast.makeText(ctx, "No user found with that email", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.startConversation(match.uid, match.displayName.ifBlank { match.email })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
