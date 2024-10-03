package com.example.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jikimi.R
import com.example.jikimi.data.model.entity.LikeEntity
import com.example.jikimi.databinding.LikeItemBinding

class LikeAdapter(
    private val onClick : (LikeEntity, Int) -> Unit,
    private val onLongClick : (LikeEntity, Int) -> Unit,
) :ListAdapter<LikeEntity, LikeAdapter.LikeViewHolder>(LikeDiffUtil){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LikeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.like_item, parent, false)
        return LikeViewHolder(LikeItemBinding.bind(view))
    }


    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener{
            onClick(item, position)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item, position)
            true
        }
    }


    class LikeViewHolder(
        private var binding : LikeItemBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(likeEntity: LikeEntity){

            with(binding){
                likeShelterNameTv.text = likeEntity.vtAcmdfcltyNm
                likeShelterAddressTv.text = likeEntity.rnAdres
                likeShelterTv.text = likeEntity.shelterType
                likeDistanceTv.text = "${likeEntity.distanceData} m"

                // 주소 데이터 설정
                likeShelterAddressTv.text = when {
                    !likeEntity?.rnAdres.isNullOrEmpty() -> likeEntity?.rnAdres // rnAdres가 존재하면 사용
                    !likeEntity?.dtlAdres.isNullOrEmpty() -> likeEntity?.dtlAdres // rnAdres가 null이면 dtlAdres 사용
                    else -> "데이터가 없음" // 두 값이 모두 null이면 기본 텍스트
                }
            }
        }
    }


    object LikeDiffUtil : DiffUtil.ItemCallback<LikeEntity>(){
        override fun areItemsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
            return oldItem.vtAcmdfcltyNm == newItem.vtAcmdfcltyNm
        }

        override fun areContentsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
            return oldItem == newItem
        }
    }
}