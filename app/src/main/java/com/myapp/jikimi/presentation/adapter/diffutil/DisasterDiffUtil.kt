package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse

class DisasterDiffUtil : DiffUtil.ItemCallback<DisasterResponse>() {
    override fun areItemsTheSame(oldItem: DisasterResponse, newItem: DisasterResponse): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: DisasterResponse, newItem: DisasterResponse): Boolean {
        return oldItem == newItem
    }
}