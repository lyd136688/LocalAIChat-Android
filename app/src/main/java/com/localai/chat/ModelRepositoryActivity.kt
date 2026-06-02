// ✅ 修复后的代码
package com.localai.chat

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.localai.chat.network.RetrofitClient
import kotlinx.coroutines.launch

class ModelRepositoryActivity : AppCompatActivity() {

    private lateinit var downloadManager: DownloadManager
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_repository)

        downloadManager = (application as MyApplication).downloadManager

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultsContainer = findViewById(R.id.resultsContainer)
        progressBar = findViewById(R.id.progressBar)

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
        progressBar.visibility = View.VISIBLE
        resultsContainer.removeAllViews()
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.apiService
                
                // 1. 先搜索模型列表
                val modelList = apiService.searchModels(query)
                
                if (modelList.isEmpty()) {
                    showEmptyResult()
                    return@launch
                }

                // 2. 获取第一个模型的详细文件列表
                val firstModel = modelList.first()
                val modelId = firstModel.id
                
                val files = apiService.getModelFiles(modelId)
                
                // 3. 筛选 GGUF 文件
                val ggufFiles = files.filter { 
                    it.path.endsWith(".gguf", ignoreCase = true) 
                }

                if (ggufFiles.isEmpty()) {
                    showNoGgufFiles(modelId)
                    return@launch
                }

                // 4. 显示结果
                for (file in ggufFiles) {
                    val itemView = createModelItemView(modelId, file.path, file.size ?: 0)
                    resultsContainer.addView(itemView)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ModelRepositoryActivity, 
                    "搜索失败: ${e.message}", 
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEmptyResult() {
        val textView = TextView(this).apply {
            text = "未找到相关模型，请尝试其他关键词"
            setPadding(32, 32, 32, 32)
        }
        resultsContainer.addView(textView)
    }

    private fun showNoGgufFiles(modelId: String) {
        val textView = TextView(this).apply {
            text = "模型 $modelId 没有找到 GGUF 格式文件"
            setPadding(32, 32, 32, 32)
        }
        resultsContainer.addView(textView)
    }

    private fun createModelItemView(modelId: String, filePath: String, fileSize: Long): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundResource(android.R.drawable.list_selector_background)
        }

        val fileNameView = TextView(this).apply {
            text = filePath.substringAfterLast("/")
            textSize = 16f
            setTextColor(0xFF333333.toInt())
        }

        val infoView = TextView(this).apply {
            text = buildString {
                append("模型: $modelId")
                if (fileSize > 0) {
                    append(" | 大小: ${formatFileSize(fileSize)}")
                }
            }
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            setPadding(0, 8, 0, 0)
        }

        val downloadButton = Button(this).apply {
            text = "下载"
            setOnClickListener {
                showDownloadDialog(modelId, filePath)
            }
        }

        layout.addView(fileNameView)
        layout.addView(infoView)
        layout.addView(downloadButton)

        return layout
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> String.format("%.2f GB", size / (1024.0 * 1024 * 1024))
        }
    }

    private fun showDownloadDialog(modelId: String, filePath: String) {
        val fileName = filePath.substringAfterLast("/")
        
        AlertDialog.Builder(this)
            .setTitle("确认下载")
            .setMessage("确定要下载 $fileName 吗？\n\n模型ID: $modelId")
            .setPositiveButton("下载") { _, _ ->
                // 构造正确的 Hugging Face 下载 URL
                val downloadUrl = "https://huggingface.co/$modelId/resolve/main/${filePath.substringAfterLast("/")}"
                downloadManager.startDownload(downloadUrl, fileName, modelId)
                Toast.makeText(this, "开始下载: $fileName", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
