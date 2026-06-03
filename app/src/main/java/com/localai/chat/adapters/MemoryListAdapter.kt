package com.localai.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.R
import com.localai.chat.data.models.MemoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryListAdapter(
    private val onDelete: (MemoryItem) -> Unit,
    private val onArchive: (MemoryItem) -> Unit
) : ListAdapter<MemoryItem, MemoryListAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textContent: TextView = itemView.findViewById(R.id.textContent)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnArchive: ImageButton = itemView.findViewById(R.id.btnArchive)
        
        fun bind(memory: MemoryItem) {
            textContent.text = memory.content
            textTime.text = formatTime(memory.timestamp)
            
            btnDelete.setOnClickListener { onDelete(memory) }
            btnArchive.setOnClickListener { onArchive(memory) }
        }
        
        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<MemoryItem>() {
        override fun areItemsTheSame(oldItem: MemoryItem, newItem: MemoryItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MemoryItem, newItem: MemoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

