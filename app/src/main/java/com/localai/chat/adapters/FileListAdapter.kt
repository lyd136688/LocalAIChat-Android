package com.localai.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.R
import com.localai.chat.data.models.FileItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(
    private val onItemClick: (FileItem) -> Unit
) : ListAdapter<FileItem, FileListAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconFile: ImageView = itemView.findViewById(R.id.iconFile)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textInfo: TextView = itemView.findViewById(R.id.textInfo)
        
        fun bind(fileItem: FileItem) {
            textName.text = fileItem.name
            iconFile.setImageResource(
                if (fileItem.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
            )
            
            val info = if (fileItem.isDirectory) {
                "文件夹"
            } else {
                "${formatSize(fileItem.size)} | ${formatTime(fileItem.lastModified)}"
            }
            textInfo.text = info
            
            itemView.setOnClickListener { onItemClick(fileItem) }
        }
        
        private fun formatSize(size: Long): String {
            return when {
                size >= 1024 * 1024 * 1024 -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
                size >= 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
                size >= 1024 -> String.format("%.2f KB", size / 1024.0)
                else -> "$size B"
            }
        }
        
        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.path == newItem.path
        }
        
        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }
    }
}
