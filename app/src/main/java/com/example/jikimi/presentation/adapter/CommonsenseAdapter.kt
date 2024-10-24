package com.example.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.databinding.CommonsenseItemBinding

class CommonsenseAdapter(
    private val onClick : (Item, Int) -> Unit
) : ListAdapter<Item, CommonsenseAdapter.CommonsenseViewHolder>(CommonsenseDiffUtil){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommonsenseAdapter.CommonsenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.commonsense_item, parent, false)
        return CommonsenseViewHolder(CommonsenseItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CommonsenseAdapter.CommonsenseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener{
            onClick(item, position)
        }
    }


    class CommonsenseViewHolder(
        private var binding : CommonsenseItemBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item : Item){
            with(binding){
                cardViewTv.text = item.safetyCateNm2
                
                val Image = when{
                    item.safetyCateNm2 == "태풍" -> R.drawable.marker_red
                    item.safetyCateNm2 == "호우" -> R.drawable.marker_blue
                    item.safetyCateNm2 == "강풍" -> R.drawable.search_img
                    item.safetyCateNm2 == "대설" -> R.drawable.shelter_phone_img
                    item.safetyCateNm2 == "한파" -> R.drawable.shelter_people_img
                    item.safetyCateNm2 == "풍랑" -> R.drawable.shelter_classification_img
                    else -> R.drawable.ic_launcher_foreground // 기본 이미지
                }

                //coil사용해서 이미지 띄우기
                cardViewIv.load(item.contentsUrl){
                    placeholder(Image)  //로딩중
                    error(Image) // 에러발생시
                }
            }
        }
    }

    object CommonsenseDiffUtil : DiffUtil.ItemCallback<Item>(){
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.safetyCateNm3 == newItem.safetyCateNm3
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.safetyCateNm3 == newItem.safetyCateNm3
        }
    }

}