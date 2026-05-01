package com.cookshare.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.data.model.Conversation
import com.cookshare.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    private val myId: String,
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conv: Conversation) {
            val otherName = conv.getOtherUserName(myId)
            binding.tvName.text = otherName
            binding.tvLastMessage.text = conv.lastMessage.ifEmpty { "Start a conversation" }
            binding.tvAvatar.text = otherName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            if (conv.lastMessageTime > 0) {
                binding.tvTime.text = formatTime(conv.lastMessageTime)
            } else {
                binding.tvTime.text = ""
            }
            binding.root.setOnClickListener { onClick(conv) }
        }
    }

    private fun formatTime(ts: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - ts
        return when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(a: Conversation, b: Conversation) = a.id == b.id
        override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
    }
}
