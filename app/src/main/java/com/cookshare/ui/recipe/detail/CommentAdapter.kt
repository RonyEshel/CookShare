package com.cookshare.ui.recipe.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookshare.data.model.Comment
import com.cookshare.data.model.User
import com.cookshare.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiff()) {

    private var liveUsers: Map<String, User> = emptyMap()

    fun updateUsers(users: Map<String, User>) {
        liveUsers = users
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            // Prefer the LIVE user data over the snapshot stored on the comment
            val live = liveUsers[comment.authorId]
            val displayName = live?.displayName?.takeIf { it.isNotBlank() } ?: comment.authorName
            val img = live?.profileImageUrl?.takeIf { it.isNotEmpty() } ?: comment.authorImage

            binding.tvCommentAuthor.text = displayName
            binding.tvCommentText.text = comment.text
            binding.tvCommentTime.text = formatTime(comment.timestamp)

            when {
                img.startsWith("http") -> {
                    binding.tvCommentAvatar.visibility = android.view.View.GONE
                    binding.ivCommentAvatar.visibility = android.view.View.VISIBLE
                    com.squareup.picasso.Picasso.get().load(img).into(binding.ivCommentAvatar)
                }
                img.isNotEmpty() -> {
                    binding.tvCommentAvatar.visibility = android.view.View.GONE
                    binding.ivCommentAvatar.visibility = android.view.View.VISIBLE
                    try {
                        val bytes = android.util.Base64.decode(img, android.util.Base64.NO_WRAP)
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) binding.ivCommentAvatar.setImageBitmap(bmp)
                    } catch (_: Exception) {
                        binding.tvCommentAvatar.visibility = android.view.View.VISIBLE
                        binding.ivCommentAvatar.visibility = android.view.View.GONE
                        binding.tvCommentAvatar.text = comment.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    }
                }
                else -> {
                    binding.tvCommentAvatar.visibility = android.view.View.VISIBLE
                    binding.ivCommentAvatar.visibility = android.view.View.GONE
                    binding.tvCommentAvatar.text = comment.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                }
            }
        }

        private fun formatTime(ts: Long): String {
            val diff = System.currentTimeMillis() - ts
            return when {
                diff < 60_000 -> "now"
                diff < 3_600_000 -> "${diff / 60_000}m"
                diff < 86_400_000 -> "${diff / 3_600_000}h"
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
            }
        }
    }

    class CommentDiff : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }
}
