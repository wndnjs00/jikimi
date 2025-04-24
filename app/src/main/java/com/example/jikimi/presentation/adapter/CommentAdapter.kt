package com.example.jikimi.presentation.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Comment
import com.example.jikimi.databinding.ItemCommentBinding

class CommentAdapter(
    private val onDeleteClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val currentUserId: String
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()
    private val mainComments = mutableListOf<Comment>()
    private val repliesMap = mutableMapOf<String, MutableList<Comment>>()

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)

        // 메인 댓글과 답글 분리
        mainComments.clear()
        repliesMap.clear()

        newComments.forEach { comment ->
            if (comment.parentCommentId.isEmpty()) {
                // 일반 댓글
                mainComments.add(comment)
            } else {
                // 답글
                val parentId = comment.parentCommentId
                if (!repliesMap.containsKey(parentId)) {
                    repliesMap[parentId] = mutableListOf()
                }
                repliesMap[parentId]?.add(comment)
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        var totalCount = mainComments.size

        // 각 부모 댓글에 대한 답글 수를 더함
        repliesMap.values.forEach { replies ->
            totalCount += replies.size
        }

        return totalCount
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        var currentPos = 0

        // 적절한 댓글 또는 답글 찾기
        for (i in mainComments.indices) {
            val mainComment = mainComments[i]

            if (currentPos == position) {
                // 메인 댓글
                holder.bind(mainComment, false)
                return
            }
            currentPos++

            // 해당 메인 댓글에 대한 답글이 있는 경우
            val replies = repliesMap[mainComment.id] ?: emptyList()
            for (j in replies.indices) {
                if (currentPos == position) {
                    // 답글
                    holder.bind(replies[j], true)
                    return
                }
                currentPos++
            }
        }
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, isReply: Boolean) {
            binding.apply {
                // 답글 여부에 따라 왼쪽 여백 조정
                val params = root.layoutParams as ViewGroup.MarginLayoutParams
                if (isReply) {
                    params.marginStart = (40 * root.resources.displayMetrics.density).toInt() // 40dp
                } else {
                    params.marginStart = 0
                }
                root.layoutParams = params

                tvCommentNickname.text = comment.nickname
                tvCommentContent.text = comment.content
                tvCommentTime.text = DateUtils.getRelativeTimeSpanString(
                    comment.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )

                // 프로필 이미지 설정
                if (comment.profileImageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(comment.profileImageUrl)
                        .placeholder(R.drawable.jikimi_img)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivCommentUserProfile)
                } else {
                    ivCommentUserProfile.setImageResource(R.drawable.jikimi_img)
                }

                // 본인 댓글인 경우에만 삭제 버튼 표시
                btnCommentOptions.isVisible = comment.userId == currentUserId

                btnCommentOptions.setOnClickListener {
                    onDeleteClick(comment)
                }

                // 답글 버튼은 대댓글에서는 숨김
                tvReply.isVisible = !isReply
                tvReply.setOnClickListener {
                    onReplyClick(comment)
                }
            }
        }
    }
}