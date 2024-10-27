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

                // 안전 카테고리와 이미지 매핑
                val imageMap = mapOf(
                    "태풍" to R.drawable.wind_img,
                    "호우" to R.drawable.rain_img,
                    "강풍" to R.drawable.storm_img,
                    "대설" to R.drawable.snow_img,
                    "한파" to R.drawable.cold_img,
                    "풍랑" to R.drawable.storm,
                    "황사" to R.drawable.sand_img,
                    "폭염" to R.drawable.heat_img,
                    "가뭄" to R.drawable.drought_img,
                    "산사태" to R.drawable.landslide_img,
                )

                // 해당 카테고리에 맞는 이미지 가져오기, 기본 이미지는 ic_launcher_foreground
                val image = imageMap[item.safetyCateNm2] ?: R.drawable.ic_launcher_foreground

                //coil사용해서 이미지 띄우기
                cardViewIv.load(item.contentsUrl){
                    placeholder(image)  //로딩중
                    error(image) // 에러발생시
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