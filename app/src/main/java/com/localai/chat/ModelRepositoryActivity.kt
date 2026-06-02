package com.localai.chat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ModelRepositoryActivity : AppCompatActivity() {

    private lateinit var downloadManager: DownloadManager
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_repository)

        downloadManager = (application as MyApplication).downloadManager

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultsContainer = findViewById(R.id.resultsContainer)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchModels(query)
            } else {
                Toast.makeText(this, "请输入模型名称", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchModels(query: String) {
        lifecycleScope.launch {
            try {
                val apiService = com.localai.chat.network.RetrofitClient.apiService
                val modelInfo = apiService.getModelInfo(query)

                resultsContainer.removeAllViews()
                val ggufFiles = modelInfo.files.filter { it.rfilename.endsWith(".gguf") }

                if (ggufFiles.isEmpty()) {
                    val textView = TextView(this@ModelRepositoryActivity)
                    textView.text = "未找到GGUF格式文件"
                    resultsContainer.addView(textView)
                    return@launch
                }

                for (file in ggufFiles) {
                    val modelItem = createModelItemView(file.rfilename, file.size)
                    resultsContainer.addView(modelItem)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ModelRepositoryActivity, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createModelItemView(fileName: String, fileSize: Long): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val textView = TextView(this).apply {
            text = "$fileName (${formatFileSize(fileSize)})"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val downloadButton = Button(this).apply {
            text = "下载"
            setOnClickListener {
                showDownloadDialog(fileName)
            }
        }

        layout.addView(textView)
        layout.addView(downloadButton)
        return layout
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    private fun showDownloadDialog(fileName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("确认下载")
        builder.setMessage("确定要下载 $fileName 吗？")

        builder.setPositiveButton("下载") { _, _ ->
            val modelId = fileName.substringBefore(".")
            val url = "https://huggingface.co/models/resolve/main/$fileName"
            downloadManager.startDownload(url, fileName, modelId)
            Toast.makeText(this, "开始下载: $fileName", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }
}
