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

                //coil사용해서 이미지 띄우기
                cardViewIv.load(item.contentsUrl){
                    placeholder(R.drawable.ic_launcher_foreground)  //로딩중
                    error(R.drawable.ic_launcher_foreground)        // 에러발생시
                }
            }
        }
    }

    object CommonsenseDiffUtil : DiffUtil.ItemCallback<Item>(){
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

}