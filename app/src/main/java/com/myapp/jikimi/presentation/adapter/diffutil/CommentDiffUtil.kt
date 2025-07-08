package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.presentation.adapter.CommentAdapter.CommentItem

class CommentDiffUtil : DiffUtil.ItemCallback<CommentItem>() {
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