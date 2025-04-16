package com.example.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jikimi.data.model.entity.ShelterEntity
import com.example.jikimi.databinding.LikeItemBinding

class ShelterSearchAdapter(
    private val onItemClick: (ShelterEntity) -> Unit
) : RecyclerView.Adapter<ShelterSearchAdapter.ViewHolder>() {

    private var shelters: List<ShelterEntity> = emptyList()

    fun updateShelters(newShelters: List<ShelterEntity>) {
        shelters = newShelters
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LikeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shelters[position])
    }

    override fun getItemCount(): Int = shelters.size

    inner class ViewHolder(private val binding: LikeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(shelters[position])
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
}