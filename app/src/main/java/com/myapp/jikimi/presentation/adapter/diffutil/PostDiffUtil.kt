package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.data.model.dto.Post

class PostDiffUtil : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        // 포스트 ID를 사용하여 동일 아이템인지 확인
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        // Post 클래스의 equals 메서드를 사용하거나, 필요한 필드 비교
        return oldItem == newItem
    }
}