package com.localai.chat

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.adapters.MessageListAdapter
import com.localai.chat.data.models.MessageItem
import com.localai.chat.utils.InferenceScheduler
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    
    private lateinit var spinnerModel: Spinner
    private lateinit var textBackend: TextView
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var editInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var messageAdapter: MessageListAdapter
    private val messages = mutableListOf<MessageItem>()
    private lateinit var scheduler: InferenceScheduler
    
    private var selectedModel: String = "自动选择"
    private val modelOptions = listOf("自动选择", "本地模型", "HuggingFace云端", "ModelScope云端")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initViews()
        setupModelSelector()
        setupRecyclerView()
        setupInput()
        
        scheduler = InferenceScheduler(this)
        
        // 尝试加载已下载的模型
        loadDownloadedModel()
    }
    
    private fun initViews() {
        spinnerModel = findViewById(R.id.spinnerModel)
        textBackend = findViewById(R.id.textBackend)
        recyclerMessages = findViewById(R.id.recyclerMessages)
        editInput = findViewById(R.id.editInput)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnVoice = findViewById(R.id.btnVoice)
    }
    
    private fun setupModelSelector() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModel.adapter = adapter
        
        spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = modelOptions[position]
                updateBackendStatus()
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
        // 添加用户消息
        val userMessage = MessageItem(
            id = System.currentTimeMillis(),
            content = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        messages.add(userMessage)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerMessages.scrollToPosition(messages.size - 1)
        
        // 选择后端
        val backend = when (selectedModel) {
            "本地模型" -> InferenceScheduler.Backend.LOCAL
            "HuggingFace云端" -> InferenceScheduler.Backend.CLOUD_HF
            "ModelScope云端" -> InferenceScheduler.Backend.CLOUD_MS
            else -> null // 自动选择
        }
        
        // 执行推理
        lifecycleScope.launch {
            val result = scheduler.infer(text, backend)
            
            val aiMessage = MessageItem(
                id = System.currentTimeMillis(),
                content = result.text,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            messages.add(aiMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerMessages.scrollToPosition(messages.size - 1)
            
            // 显示使用的后端
            textBackend.text = "后端: ${result.backend.name} | 延迟: ${result.latencyMs}ms"
        }
    }
    
    private fun loadDownloadedModel() {
        val modelsDir = getExternalFilesDir("models")
        val ggufFiles = modelsDir?.listFiles { _, name -> name.endsWith(".gguf") }
        
        if (!ggufFiles.isNullOrEmpty()) {
            val modelPath = ggufFiles[0].absolutePath
            val loaded = scheduler.loadLocalModel(modelPath)
            if (loaded) {
                Toast.makeText(this, "已加载模型: ${ggufFiles[0].name}", Toast.LENGTH_SHORT).show()
            }
        }
        
        updateBackendStatus()
    }
    
    private fun updateBackendStatus() {
        val status = when {
            selectedModel != "自动选择" -> selectedModel
            scheduler.isLocalModelLoaded() -> "本地模型就绪"
            else -> "自动选择 (云端)"
        }
        textBackend.text = "后端: $status"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scheduler.unloadLocalModel()
    }
}

