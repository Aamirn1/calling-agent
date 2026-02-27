package com.elevateedge.aicallingagent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elevateedge.aicallingagent.data.Lead
import com.elevateedge.aicallingagent.databinding.ItemLeadBinding

class LeadAdapter(private val onLeadClick: (Lead) -> Unit) : ListAdapter<Lead, LeadAdapter.LeadViewHolder>(LeadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadViewHolder {
        val binding = ItemLeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeadViewHolder(binding, onLeadClick)
    }

    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LeadViewHolder(private val binding: ItemLeadBinding, private val onLeadClick: (Lead) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lead: Lead) {
            binding.businessName.text = lead.businessName
            binding.phoneNumber.text = lead.phoneNumber
            binding.statusBadge.text = lead.status.uppercase()
            
            binding.root.setOnClickListener {
                onLeadClick(lead)
            }
        }
    }

    private class LeadDiffCallback : DiffUtil.ItemCallback<Lead>() {
        override fun areItemsTheSame(oldItem: Lead, newItem: Lead): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Lead, newItem: Lead): Boolean = oldItem == newItem
    }
}
