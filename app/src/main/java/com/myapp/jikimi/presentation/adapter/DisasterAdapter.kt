package com.myapp.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.CommonsenseItem
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse

class DisasterAdapter(
    private val onItemClick: (DisasterResponse) -> Unit
) : RecyclerView.Adapter<DisasterAdapter.DisasterViewHolder>() {

    private var disasters = listOf<DisasterResponse>()

    fun updateDisasters(newDisasters: List<DisasterResponse>) {
        disasters = newDisasters
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisasterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_disaster, parent, false)
        return DisasterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisasterViewHolder, position: Int) {
        holder.bind(disasters[position])
    }

    override fun getItemCount(): Int = disasters.size

    inner class DisasterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tv_disaster_title)
        private val categoryText: TextView = itemView.findViewById(R.id.tv_disaster_category)
        private val riskLevelText: TextView = itemView.findViewById(R.id.tv_risk_level)
//        private val descriptionText: TextView = itemView.findViewById(R.id.tv_disaster_description)

        fun bind(disaster: DisasterResponse) {
            titleText.text = disaster.title
            categoryText.text = disaster.category
            riskLevelText.text = disaster.riskLevel
//            descriptionText.text = disaster.shortDescription

            // 위험도에 따른 색상 설정
            val riskColor = when (disaster.riskLevel) {
                "낮음" -> android.R.color.holo_green_light
                "보통" -> android.R.color.holo_orange_light
                "높음" -> android.R.color.holo_red_light
//                "매우높음" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            riskLevelText.setTextColor(itemView.context.getColor(riskColor))

            itemView.setOnClickListener {
                onItemClick(disaster)
            }
        }
    }
}
