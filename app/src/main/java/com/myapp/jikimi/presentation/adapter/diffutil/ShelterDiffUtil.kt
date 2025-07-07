package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.data.model.entity.ShelterEntity

class ShelterDiffUtil : DiffUtil.ItemCallback<ShelterEntity>() {
    override fun areItemsTheSame(oldItem: ShelterEntity, newItem: ShelterEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ShelterEntity, newItem: ShelterEntity): Boolean {
        return oldItem.vtAcmdfcltyNm == newItem.vtAcmdfcltyNm && oldItem.address == newItem.address && oldItem.shelterType == newItem.shelterType
    }
}