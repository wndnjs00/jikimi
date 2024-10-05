package com.example.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.databinding.CommonsenseItemBinding

class CommonsenseAdapter(
//    private val onClick : (Item, Int) -> Unit
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

//        holder.itemView.setOnClickListener{
//            onClick(item, position)
//        }
    }


    class CommonsenseViewHolder(
        private var binding : CommonsenseItemBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item : Item){
            binding.cardViewTv.text = item.safetyCateNm2

            //coil사용해서 이미지 띄우기
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