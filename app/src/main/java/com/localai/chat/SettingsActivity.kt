package com.localai.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.item_model_provider).setOnClickListener {
            showModelProviderDialog()
        }
        findViewById<LinearLayout>(R.id.item_scene_config).setOnClickListener {
            showSceneConfigDialog()
        }
        findViewById<LinearLayout>(R.id.item_local_model).setOnClickListener {
            Toast.makeText(this, "本地模型管理功能开发中", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.item_workspace_memory).setOnClickListener {
            showWorkspaceMemoryDialog()
        }
        findViewById<LinearLayout>(R.id.item_mcp_tools).setOnClickListener {
            showMcpToolsDialog()
        }
        findViewById<LinearLayout>(R.id.item_alpine).setOnClickListener {
            Toast.makeText(this, "Alpine环境功能开发中", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.item_alarm).setOnClickListener {
            Toast.makeText(this, "闹钟设置功能开发中", Toast.LENGTH_SHORT).show()
        }

        val switchLocal = findViewById<Switch>(R.id.switch_local_service)
        val switchHide = findViewById<Switch>(R.id.switch_background_hide)
        val switchMemory = findViewById<Switch>(R.id.switch_workspace_memory)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        switchLocal.isChecked = prefs.getBoolean("local_service_enabled", false)
        switchHide.isChecked = prefs.getBoolean("background_hide", false)
        switchMemory.isChecked = prefs.getBoolean("workspace_memory_enabled", true)

        switchLocal.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("local_service_enabled", isChecked).apply()
        }
        switchHide.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("background_hide", isChecked).apply()
        }
        switchMemory.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit().putBoolean("workspace_memory_enabled", isChecked).apply()
        }
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

