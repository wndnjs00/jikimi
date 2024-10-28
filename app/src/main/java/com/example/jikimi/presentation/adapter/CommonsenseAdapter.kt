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

                val disasterImage = when (item.safetyCateNm2) {
                    "태풍" -> R.drawable.wind_img
                    "홍수" -> R.drawable.flood_img
                    "호우" -> R.drawable.rain_img
                    "강풍" -> R.drawable.storm_img
                    "대설" -> R.drawable.snow_img
                    "한파" -> R.drawable.cold_img
                    "풍랑" -> R.drawable.tsunami_earthquake_img
                    "황사" -> R.drawable.sand_img
                    "폭염" -> R.drawable.heat_img
                    "가뭄" -> R.drawable.drought_img
                    "지진" -> R.drawable.earthquake_img
                    "지진해일" -> R.drawable.storm
                    "해일" -> R.drawable.tsunami_img
                    "산사태" -> R.drawable.landslide_img
                    "화산폭발" -> R.drawable.volcano_img
                    else -> R.drawable.ic_launcher_foreground
                }

                //coil사용해서 이미지 띄우기
                cardViewIv.load(disasterImage){
                    placeholder(R.drawable.ic_launcher_foreground)  //로딩중
                    error(R.drawable.ic_launcher_foreground) // 에러발생시
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