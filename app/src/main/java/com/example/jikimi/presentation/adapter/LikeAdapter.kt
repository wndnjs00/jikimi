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
    }


    class LikeViewHolder(
        private var binding : LikeItemBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(likeEntity: LikeEntity){

            binding.likeShelterNameTv.text = likeEntity.vtAcmdfcltyNm
            binding.likeShelterAddressTv.text = likeEntity.rnAdres

            // 주소 데이터 설정
            binding.likeShelterAddressTv.text = when {
                !likeEntity?.rnAdres.isNullOrEmpty() -> likeEntity?.rnAdres // rnAdres가 존재하면 사용
                !likeEntity?.dtlAdres.isNullOrEmpty() -> likeEntity?.dtlAdres // rnAdres가 null이면 dtlAdres 사용
                else -> "데이터가 없음" // 두 값이 모두 null이면 기본 텍스트
            }
        }
    }


    object LikeDiffUtil : DiffUtil.ItemCallback<LikeEntity>(){
        override fun areItemsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LikeEntity, newItem: LikeEntity): Boolean {
            return oldItem == newItem
        }
    }
}