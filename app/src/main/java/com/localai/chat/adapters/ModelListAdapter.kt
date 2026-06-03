package com.localai.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.R
import com.localai.chat.data.models.ModelInfo

class ModelListAdapter(
    private val onDownload: (ModelInfo) -> Unit
) : ListAdapter<ModelInfo, ModelListAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_model, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textInfo: TextView = itemView.findViewById(R.id.textInfo)
        private val textSize: TextView = itemView.findViewById(R.id.textSize)
        private val textAuthor: TextView = itemView.findViewById(R.id.textAuthor)
        private val btnDownload: Button = itemView.findViewById(R.id.btnDownload)
        
        fun bind(model: ModelInfo) {
            textName.text = model.displayName
            textInfo.text = "${model.parameters} 参数"
            textSize.text = model.size
            textAuthor.text = model.author
            
            btnDownload.setOnClickListener {
                onDownload(model)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<ModelInfo>() {
        override fun areItemsTheSame(oldItem: ModelInfo, newItem: ModelInfo): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ModelInfo, newItem: ModelInfo): Boolean {
            return oldItem == newItem
        }
    }
}
