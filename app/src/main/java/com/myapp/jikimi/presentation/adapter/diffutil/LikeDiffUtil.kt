package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.data.model.entity.LikeEntity

class LikeDiffUtil : DiffUtil.ItemCallback<LikeEntity>(){
    override fun areItemsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
        return oldItem.vtAcmdfcltyNm == newItem.vtAcmdfcltyNm
    }

    override fun areContentsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
        return oldItem == newItem
    }
}