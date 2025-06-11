package com.myapp.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse
import com.myapp.jikimi.databinding.ItemDisasterBinding

class DisasterAdapter(
    private val onClick: (DisasterResponse, Int) -> Unit
) : ListAdapter<DisasterResponse, DisasterAdapter.DisasterViewHolder>(DisasterDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisasterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_disaster, parent, false)
        return DisasterViewHolder(ItemDisasterBinding.bind(view))
    }

    override fun onBindViewHolder(holder: DisasterViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onClick(item, position)
        }
    }

    class DisasterViewHolder(
        private val binding: ItemDisasterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(disaster: DisasterResponse) {
            with(binding) {
                tvDisasterTitle.text = disaster.title
                tvDisasterCategory.text = disaster.category
                tvRiskLevel.text = disaster.riskLevel

                // 위험도에 따른 색상 설정
                val riskColor = when (disaster.riskLevel) {
                    "낮음" -> android.R.color.holo_green_light
                    "보통" -> android.R.color.holo_orange_light
                    "높음" -> android.R.color.holo_red_light
                    else -> android.R.color.darker_gray
                }
                tvRiskLevel.setTextColor(itemView.context.getColor(riskColor))
            }
        }
    }

    object DisasterDiffUtil : DiffUtil.ItemCallback<DisasterResponse>() {
        override fun areItemsTheSame(oldItem: DisasterResponse, newItem: DisasterResponse): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DisasterResponse, newItem: DisasterResponse): Boolean {
            return oldItem == newItem
        }
    }
}