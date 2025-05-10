package com.myapp.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapp.jikimi.data.model.entity.ShelterEntity
import com.myapp.jikimi.databinding.LikeItemBinding

class ShelterSearchAdapter(
    private val onItemClick: (ShelterEntity) -> Unit
) : ListAdapter<ShelterEntity, ShelterSearchAdapter.ViewHolder>(ShelterDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LikeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: LikeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(shelter: ShelterEntity) {
            binding.likeShelterNameTv.text = shelter.vtAcmdfcltyNm
            binding.likeShelterAddressTv.text = shelter.address
            binding.likeShelterTv.text = if (shelter.shelterType == "임시주거시설") "임시주거시설" else "야외대피장소"
            binding.likeDistanceTv.visibility = View.GONE // 거리 정보는 표시하지 않음
        }
    }

    object ShelterDiffUtil : DiffUtil.ItemCallback<ShelterEntity>() {
        override fun areItemsTheSame(oldItem: ShelterEntity, newItem: ShelterEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShelterEntity, newItem: ShelterEntity): Boolean {
            return oldItem.vtAcmdfcltyNm == newItem.vtAcmdfcltyNm &&
                    oldItem.address == newItem.address &&
                    oldItem.shelterType == newItem.shelterType
        }
    }
}