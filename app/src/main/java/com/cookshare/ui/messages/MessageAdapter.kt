package com.cookshare.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.data.model.Message
import com.cookshare.databinding.ItemMessageReceivedBinding
import com.cookshare.databinding.ItemMessageSentBinding

private const val VIEW_SENT = 1
private const val VIEW_RECEIVED = 2

class MessageAdapter(private val myId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == myId) VIEW_SENT else VIEW_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (holder is SentViewHolder) holder.bind(msg)
        else if (holder is ReceivedViewHolder) holder.bind(msg)
    }

    inner class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            binding.tvMessage.text = msg.text
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            binding.tvMessage.text = msg.text
            binding.tvSender.text = msg.senderName
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
        override fun areContentsTheSame(a: Message, b: Message) = a == b
    }
}
