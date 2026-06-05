package com.localai.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.localai.chat.adapters.MessageListAdapter
import com.localai.chat.databinding.ActivityChatBinding
import com.localai.chat.data.models.MessageItem
import com.localai.chat.native.InferenceScheduler
import com.localai.chat.utils.AgentExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageListAdapter
    private lateinit var agentExecutor: AgentExecutor
    private lateinit var inferenceScheduler: InferenceScheduler

    private var currentImageUri: Uri? = null
    private var currentModelId: String = "qwen3.5-plus"

    private val availableModels = listOf(
        "qwen-plus",
        "qwen3-vl-plus",
        "qwen3.5-flash",
        "qwen3.5-plus",
        "text-embedding",
        "OmniInfer (本地)"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                currentImageUri = uri
                Toast.makeText(this, "已选择图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        agentExecutor = AgentExecutor(application as MyApplication)

        val app = application as MyApplication
        inferenceScheduler = InferenceScheduler(
            this,
            app.llamaHelper,
            app.sessionMemoryManager,
            app.imageMemoryStorage
        )

        setupModelSelector()
        setupRecyclerView()
        setupInputArea()
        loadModel()
    }

    private fun setupModelSelector() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availableModels)
        binding.spinnerModel.setAdapter(adapter)
        binding.spinnerModel.setText("qwen3.5-plus", false)

        binding.spinnerModel.setOnItemClickListener { _, _, position, _ ->
            currentModelId = availableModels[position]
            if (position == availableModels.size - 1) {
                loadLocalModel()
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageListAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = messageAdapter
    }

    private fun setupInputArea() {
        binding.btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.btnVoice.setOnClickListener {
            Toast.makeText(this, "语音输入功能开发中", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadModel() {
        val modelPath = getSharedPreferences("app_config", MODE_PRIVATE)
            .getString("current_model", null)

        if (modelPath != null && File(modelPath).exists()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val success = (application as MyApplication).llamaHelper.loadModel(modelPath)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@ChatActivity, "本地模型加载完成", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ChatActivity, "模型加载失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadLocalModel() {
        val modelPath = getSharedPreferences("app_config", MODE_PRIVATE)
            .getString("current_model", null)

        if (modelPath == null || !File(modelPath).exists()) {
            Toast.makeText(this, "请先在模型市场下载模型", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val success = (application as MyApplication).llamaHelper.loadModel(modelPath)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@ChatActivity, "本地模型加载完成", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChatActivity, "模型加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val input = binding.etInput.text.toString().trim()
        if (input.isEmpty() && currentImageUri == null) return

        val userMessage = buildString {
            if (currentImageUri != null) append("[图片] ")
            append(input)
        }

        val userMsg = MessageItem(
            content = userMessage,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        messageAdapter.addMessage(userMsg)
        binding.etInput.setText("")
        binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)

        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "思考中..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val memoryManager = (application as MyApplication).memoryManager
                memoryManager.addShortTermMemory(userMessage, "user")

                val replyBuilder = StringBuilder()

                inferenceScheduler.chat(
                    userMessage = input,
                    onChunk = { chunk ->
                        replyBuilder.append(chunk)
                        lifecycleScope.launch(Dispatchers.Main) {
                            val aiMsg = MessageItem(
                                content = replyBuilder.toString(),
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                            messageAdapter.updateLastMessage(aiMsg)
                            binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
                        }
                    },
                    onComplete = { response, truncated ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val finalReply = if (truncated) "$response\n\n[内容已截断，超过安全阈值]" else response
                            val aiMsg = MessageItem(
                                content = finalReply,
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                            messageAdapter.updateLastMessage(aiMsg)
                            binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
                            binding.progressBar.visibility = View.GONE
                            binding.tvStatus.text = if (truncated) "内容已截断" else "思考完成"
                            currentImageUri = null
                        }
                        memoryManager.addShortTermMemory(response, "assistant")
                    }
                )

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = "执行失败: ${e.message}"
                    Toast.makeText(this@ChatActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
