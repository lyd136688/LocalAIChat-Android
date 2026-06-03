package com.localai.chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.localai.chat.ChatActivity
import com.localai.chat.R

class ChatFragment : Fragment() {
    
    private lateinit var textWelcome: TextView
    private lateinit var btnStartChat: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        textWelcome = view.findViewById(R.id.textWelcome)
        btnStartChat = view.findViewById(R.id.btnStartChat)
        
        textWelcome.text = "欢迎使用 LocalAIChat\n开始与本地AI对话"
        
        btnStartChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }
    }
}
