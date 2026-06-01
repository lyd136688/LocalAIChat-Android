package com.localai.chat

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var tvChat: TextView
    private var llama: LlamaHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        etInput = findViewById(R.id.et_input)
        btnSend = findViewById(R.id.btn_send)
        tvChat = findViewById(R.id.tv_chat)

        val modelPath = getSharedPreferences("app_config", MODE_PRIVATE)
            .getString("current_model", null)
        if (modelPath == null || !File(modelPath).exists()) {
            Toast.makeText(this, "请先在模型管理中选择一个模型", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            llama = LlamaHelper(modelPath)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "模型加载完成", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isEmpty()) return@setOnClickListener
            etInput.setText("")
            tvChat.append("你: $input\n")
            lifecycleScope.launch(Dispatchers.IO) {
                val reply = llama?.generate(input) ?: "模型未就绪"
                withContext(Dispatchers.Main) {
                    tvChat.append("AI: $reply\n\n")
                }
            }
        }
    }
}
