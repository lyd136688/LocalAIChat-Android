package com.localai.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.localai.chat.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()

        binding.btnBack.setOnClickListener { finish() }

        binding.itemModelProvider.setOnClickListener { showModelProviderDialog() }
        binding.itemSceneConfig.setOnClickListener { showSceneConfigDialog() }
        binding.itemLocalModel.setOnClickListener {
            Toast.makeText(this, "本地模型管理功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.itemWorkspaceMemory.setOnClickListener { showWorkspaceMemoryDialog() }
        binding.itemMcpTools.setOnClickListener { showMcpToolsDialog() }
        binding.itemAlpine.setOnClickListener {
            Toast.makeText(this, "Alpine环境功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.itemAlarm.setOnClickListener {
            Toast.makeText(this, "闹钟设置功能开发中", Toast.LENGTH_SHORT).show()
        }

        binding.switchLocalService.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("local_service_enabled", isChecked).apply()
        }

        binding.switchBackgroundHide.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("background_hide", isChecked).apply()
        }

        binding.switchWorkspaceMemory.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("workspace_memory_enabled", isChecked).apply()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        binding.switchLocalService.isChecked = prefs.getBoolean("local_service_enabled", false)
        binding.switchBackgroundHide.isChecked = prefs.getBoolean("background_hide", false)
        binding.switchWorkspaceMemory.isChecked = prefs.getBoolean("workspace_memory_enabled", true)
    }

    private fun showModelProviderDialog() {
        val providers = arrayOf("OpenAI", "Claude", "Qwen", "DeepSeek", "本地模型")
        AlertDialog.Builder(this)
            .setTitle("选择模型提供商")
            .setSingleChoiceItems(providers, -1) { dialog, which ->
                getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit().putString("model_provider", providers[which]).apply()
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showSceneConfigDialog() {
        AlertDialog.Builder(this)
            .setTitle("场景模型配置")
            .setMessage("为不同场景绑定默认模型：\n\n· 聊天对话：qwen3.5-plus\n· 代码生成：qwen3.5-flash\n· 文档总结：qwen-plus\n· 图片理解：qwen3-vl-plus")
            .setPositiveButton("配置", null)
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showWorkspaceMemoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Workspace记忆配置")
            .setMessage("启用后，对话内容将被存储并支持语义检索。\n\n当前状态：已启用")
            .setPositiveButton("管理记忆") { _, _ ->
                startActivity(Intent(this, MemoryCenterActivity::class.java))
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showMcpToolsDialog() {
        AlertDialog.Builder(this)
            .setTitle("MCP工具管理")
            .setMessage("可添加、启动/停止远程MCP服务。\n\n可用工具：\n· web_search\n· calculator\n· file_manager\n· terminal")
            .setPositiveButton("添加工具", null)
            .setNegativeButton("关闭", null)
            .show()
    }
}
