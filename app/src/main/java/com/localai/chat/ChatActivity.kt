package com.localai.chat

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.adapters.MessageListAdapter
import com.localai.chat.data.models.MessageItem
import com.localai.chat.utils.AgentExecutor
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    
    private lateinit var spinnerModel: Spinner
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var editInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var messageAdapter: MessageListAdapter
    private val messages = mutableListOf<MessageItem>()
    private lateinit var agentExecutor: AgentExecutor
    
    private var selectedModel: String = "默认模型"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initViews()
        setupModelSelector()
        setupRecyclerView()
        setupInput()
        
        agentExecutor = AgentExecutor(this)
    }
    
    private fun initViews() {
        spinnerModel = findViewById(R.id.spinnerModel)
        recyclerMessages = findViewById(R.id.recyclerMessages)
        editInput = findViewById(R.id.editInput)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnVoice = findViewById(R.id.btnVoice)
    }
    
    private fun setupModelSelector() {
        val models = arrayOf("默认模型", "LLaMA-2-7B", "LLaMA-2-13B", "Qwen-7B", "ChatGLM3-6B")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, models)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModel.adapter = adapter
        
        spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = models[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageListAdapter(messages)
        recyclerMessages.layoutManager = LinearLayoutManager(this)
        recyclerMessages.adapter = messageAdapter
    }
    
    private fun setupInput() {
        btnSend.setOnClickListener {
            val text = editInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                editInput.text.clear()
            }
        }
        
        btnAttach.setOnClickListener {
            Toast.makeText(this, "选择图片", Toast.LENGTH_SHORT).show()
        }
        
        btnVoice.setOnClickListener {
            Toast.makeText(this, "语音输入", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendMessage(text: String) {
        val userMessage = MessageItem(
            id = System.currentTimeMillis(),
            content = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        messages.add(userMessage)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerMessages.scrollToPosition(messages.size - 1)
        
        lifecycleScope.launch {
            val response = agentExecutor.execute(text)
            val aiMessage = MessageItem(
                id = System.currentTimeMillis(),
                content = response,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            messages.add(aiMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerMessages.scrollToPosition(messages.size - 1)
        }
    }
}

