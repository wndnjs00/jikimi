package com.example.jikimi.presentation.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Post
import com.example.jikimi.databinding.ItemPostBinding

class PostAdapter(
    private val onPostClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit,
    private val currentUserId: String
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
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

                // 본인 게시물인 경우에만 삭제 버튼 표시
                btnDelete.isVisible = post.userId == currentUserId

                // 아이템 클릭시(수정하기)
                root.setOnClickListener {
                    onPostClick(post)
                }

                // 점 버튼 클릭시
                btnDelete.setOnClickListener {
                    onDeleteClick(post)
                }
            }
        }
    }
}
