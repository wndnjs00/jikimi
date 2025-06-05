package com.myapp.jikimi.presentation.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.databinding.ItemPostBinding

class PostAdapter(
    private val onPostClick: (Post) -> Unit,
    private val currentUserId: String
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                tvNickname.text = post.nickname
                tvContent.text = post.content
                tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
                    post.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                tvCommentCount.text = "댓글 ${post.commentCount}개"

                tvCategory.text = post.category
                tvCategory.visibility = if (post.category.isNotEmpty()) View.VISIBLE else View.GONE

                // 프로필 이미지 설정
                if (post.profileImageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(post.profileImageUrl)
                        .placeholder(R.drawable.jikimi_img)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivUserProfile)
                } else {
                    ivUserProfile.setImageResource(R.drawable.jikimi_img)
                }

                // 게시물 이미지가 있으면 첫 번째 이미지만 표시
                if (post.imageUrls.isNotEmpty()) {
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(post.imageUrls[0])
                        .centerCrop()
                        .into(ivPostImage)
                } else {
                    ivPostImage.visibility = View.GONE
                }

                // 아이템 클릭시
                root.setOnClickListener {
                    onPostClick(post)
                }
            }
        }
    }

    // DiffUtil 구현
    object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            // 포스트 ID를 사용하여 동일 아이템인지 확인
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            // Post 클래스의 equals 메서드를 사용하거나, 필요한 필드 비교
            return oldItem == newItem
        }
    }
}