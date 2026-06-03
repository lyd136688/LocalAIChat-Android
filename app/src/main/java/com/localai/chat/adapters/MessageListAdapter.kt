package com.localai.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.R
import com.localai.chat.data.models.MessageItem

class MessageListAdapter(
    private val messages: List<MessageItem>
) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    
    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_AI
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == TYPE_USER) R.layout.item_message else R.layout.item_message
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount(): Int = messages.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textContent: TextView = itemView.findViewById(R.id.textContent)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        
        fun bind(message: MessageItem) {
            textContent.text = message.content
            textTime.text = android.text.format.DateFormat.format("HH:mm", message.timestamp)
            
            val layoutParams = itemView.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
            if (message.isUser) {
                itemView.setBackgroundResource(R.drawable.bg_button_primary)
                layoutParams.marginStart = 100
                layoutParams.marginEnd = 16
            } else {
                itemView.setBackgroundResource(R.drawable.bg_card)
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 100
            }
            itemView.layoutParams = layoutParams
        }
    }
}
