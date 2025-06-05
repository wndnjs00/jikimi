package com.myapp.jikimi.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.jikimi.R
import com.myapp.jikimi.data.model.dto.CommonsenseItem

// 임시 - 수정해야함
class CommonsenseAdapter(private val items: List<CommonsenseItem>) : RecyclerView.Adapter<CommonsenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val urgency: TextView = view.findViewById(R.id.tvUrgency)
        val risk: TextView = view.findViewById(R.id.tvRisk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commonsense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.urgency.text = item.urgency
        holder.risk.text = item.risk
    }

    override fun getItemCount(): Int = items.size
}