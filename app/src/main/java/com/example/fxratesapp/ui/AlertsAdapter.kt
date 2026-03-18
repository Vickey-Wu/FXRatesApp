package com.example.fxratesapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fxratesapp.data.AlertEntity
import com.example.fxratesapp.databinding.ItemAlertBinding

class AlertsAdapter(
    private val onDelete: (AlertEntity) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private val items = mutableListOf<AlertEntity>()

    fun submit(list: List<AlertEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = items[position]
        holder.bind(alert)
    }

    override fun getItemCount(): Int = items.size

    inner class AlertViewHolder(private val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alert: AlertEntity) {
            val text = String.format(
                "%s %.2f -> %s %s %.4f",
                alert.base,
                alert.amount,
                alert.target,
                alert.operator,
                alert.threshold
            )
            binding.alertText.text = text
            binding.btnDelete.setOnClickListener { onDelete(alert) }
        }
    }
}
