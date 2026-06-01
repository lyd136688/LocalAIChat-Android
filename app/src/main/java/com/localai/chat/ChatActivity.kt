package com.localai.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var btnAttach: ImageButton
    private lateinit var tvChat: TextView
    private var llama: LlamaHelper? = null
    private var currentImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        setContentView(R.layout.activity_chat)

        etInput = findViewById(R.id.et_input)
        btnSend = findViewById(R.id.btn_send)
        btnAttach = findViewById(R.id.btn_attach)
        tvChat = findViewById(R.id.tv_chat)

        val modelPath = getSharedPreferences("app_config", MODE_PRIVATE)
            .getString("current_model", null)
        if (modelPath == null || !File(modelPath).exists()) {
            Toast.makeText(this, "请先在“选择模型”中下载并选择一个模型", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            llama = LlamaHelper(modelPath)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "模型加载完成，可以开始对话", Toast.LENGTH_SHORT).show()
            }
        }

        btnAttach.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    pickImageLauncher.launch(intent)
                }
        }

        btnSend.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isEmpty() && currentImageUri == null) return@setOnClickListener

            val userMessage = buildString {
                if (currentImageUri != null) append("[图片] ")
                append(if (input.isNotEmpty()) input else "描述这张图片")
            }
            tvChat.append("你: $userMessage\n")
            etInput.setText("")

            lifecycleScope.launch(Dispatchers.IO) {
                val reply = if (currentImageUri != null) {
                    llama?.generateWithImage(currentImageUri!!.path!!, input)
                        ?: "当前模型不支持图片识别，请更换多模态模型（如LLaVA）"
                } else {
                    llama?.generate(input) ?: "模型未响应"
                }
                withContext(Dispatchers.Main) {
                    tvChat.append("AI: $reply\n\n")
                    currentImageUri = null
                }
            }
        }
    }
}
