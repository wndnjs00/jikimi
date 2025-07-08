package com.myapp.jikimi.presentation.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.databinding.ItemPostBinding
import com.myapp.jikimi.presentation.adapter.diffutil.PostDiffUtil

class PostAdapter(
    private val onPostClick: (Post) -> Unit,
    private val currentUserId: String
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffUtil()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(ItemPostBinding.bind(view))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)

        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            with(binding) {
                nicknameTv.text = post.nickname
                contentTv.text = post.content
                timestampTv.text = DateUtils.getRelativeTimeSpanString(
                    post.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                commentCountTv.text = "댓글 ${post.commentCount}개"

                categoryTv.text = post.category
                categoryTv.visibility = if (post.category.isNotEmpty()) View.VISIBLE else View.GONE

                // 프로필 이미지 설정
                if (post.profileImageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(post.profileImageUrl)
                        .placeholder(R.drawable.jikimi_img)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(userProfileIv)
                } else {
                    userProfileIv.setImageResource(R.drawable.jikimi_img)
                }

                // 게시물 이미지가 있으면 첫 번째 이미지만 표시
                if (post.imageUrls.isNotEmpty()) {
                    postImageIv.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(post.imageUrls[0])
                        .centerCrop()
                        .into(postImageIv)
                } else {
                    postImageIv.visibility = View.GONE
                }
            }
        }
    }
}