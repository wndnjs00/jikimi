package com.myapp.jikimi.presentation.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.Comment
import com.myapp.jikimi.databinding.ItemCommentBinding
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private val onOptionsClick: (Comment, Boolean, View) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val currentUserId: String
) : ListAdapter<CommentAdapter.CommentItem, CommentAdapter.CommentViewHolder>(CommentDiffUtil) {

    // 차단된 댓글 ID 목록
    private val blockedCommentIds = mutableSetOf<String>()

    // 댓글 데이터 클래스
    sealed class CommentItem {
        data class MainComment(val comment: Comment) : CommentItem()
        data class ReplyComment(val comment: Comment) : CommentItem()
    }

    fun updateComments(newComments: List<Comment>) {
        // 차단된 댓글 ID 가져오기
        fetchBlockedComments {
            val newDisplayItems = processComments(newComments)
            submitList(newDisplayItems)
        }
    }

    // 댓글 목록을 처리하여 표시 항목으로 변환
    private fun processComments(comments: List<Comment>): List<CommentItem> {
        val result = mutableListOf<CommentItem>()
        val mainComments = mutableListOf<Comment>()
        val repliesMap = mutableMapOf<String, MutableList<Comment>>()

        // 메인 댓글과 답글 분리
        comments.forEach { comment ->
            // 차단된 댓글 제외
            if (!blockedCommentIds.contains(comment.id)) {
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
        }

        // 항목 목록 구성: 메인 댓글 다음에 해당 답글이 오도록
        mainComments.forEach { mainComment ->
            result.add(CommentItem.MainComment(mainComment))

            // 해당 메인 댓글에 대한 답글 추가
            repliesMap[mainComment.id]?.forEach { reply ->
                result.add(CommentItem.ReplyComment(reply))
            }
        }

        return result
    }

    // 차단된 댓글 ID 가져오기
    private fun fetchBlockedComments(onComplete: () -> Unit) {
        blockedCommentIds.clear()

        if (currentUserId.isEmpty()) {
            onComplete()
            return
        }

        FirebaseFirestore.getInstance().collection("blocked_comments")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val commentId = document.getString("commentId") ?: ""
                    if (commentId.isNotEmpty()) {
                        blockedCommentIds.add(commentId)
                    }
                }
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentItem.MainComment -> VIEW_TYPE_MAIN_COMMENT
            is CommentItem.ReplyComment -> VIEW_TYPE_REPLY_COMMENT
        }
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = getItem(position)
        val comment = when (item) {
            is CommentItem.MainComment -> item.comment
            is CommentItem.ReplyComment -> item.comment
        }

        val isReply = item is CommentItem.ReplyComment
        holder.bind(comment, isReply)
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

                // 모든 댓글에 옵션 버튼 표시
                btnCommentOptions.isVisible = true

                // 내 댓글인지 여부 확인
                val isUserComment = comment.userId == currentUserId

                // 버튼 클릭 시 해당 버튼(view)를 함께 전달
                btnCommentOptions.setOnClickListener { view ->
                    onOptionsClick(comment, isUserComment, view)
                }

                // 답글 버튼은 대댓글에서는 숨김
                tvReply.isVisible = !isReply
                tvReply.setOnClickListener {
                    onReplyClick(comment)
                }
            }
        }
    }

    // DiffUtil 구현
    companion object {
        private const val VIEW_TYPE_MAIN_COMMENT = 0
        private const val VIEW_TYPE_REPLY_COMMENT = 1

        object CommentDiffUtil : DiffUtil.ItemCallback<CommentItem>() {
            override fun areItemsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean {
                return when {
                    oldItem is CommentItem.MainComment && newItem is CommentItem.MainComment ->
                        oldItem.comment.id == newItem.comment.id
                    oldItem is CommentItem.ReplyComment && newItem is CommentItem.ReplyComment ->
                        oldItem.comment.id == newItem.comment.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean {
                return when {
                    oldItem is CommentItem.MainComment && newItem is CommentItem.MainComment ->
                        oldItem.comment == newItem.comment
                    oldItem is CommentItem.ReplyComment && newItem is CommentItem.ReplyComment ->
                        oldItem.comment == newItem.comment
                    else -> false
                }
            }
        }
    }
}