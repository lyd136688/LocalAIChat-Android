package com.localai.chat

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val itemModelProvider: LinearLayout = findViewById(R.id.item_model_provider)
        itemModelProvider.setOnClickListener { showModelProviderDialog() }

        val itemSceneConfig: LinearLayout = findViewById(R.id.item_scene_config)
        itemSceneConfig.setOnClickListener { showSceneConfigDialog() }

        val itemLocalModel: LinearLayout = findViewById(R.id.item_local_model)
        itemLocalModel.setOnClickListener {
            Toast.makeText(this, "本地模型管理功能开发中", Toast.LENGTH_SHORT).show()
        }

        val itemWorkspaceMemory: LinearLayout = findViewById(R.id.item_workspace_memory)
        itemWorkspaceMemory.setOnClickListener { showWorkspaceMemoryDialog() }

        val itemMcpTools: LinearLayout = findViewById(R.id.item_mcp_tools)
        itemMcpTools.setOnClickListener { showMcpToolsDialog() }

        val itemAlpine: LinearLayout = findViewById(R.id.item_alpine)
        itemAlpine.setOnClickListener {
            Toast.makeText(this, "Alpine环境功能开发中", Toast.LENGTH_SHORT).show()
        }

        val itemAlarm: LinearLayout = findViewById(R.id.item_alarm)
        itemAlarm.setOnClickListener {
            Toast.makeText(this, "闹钟设置功能开发中", Toast.LENGTH_SHORT).show()
        }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        val switchLocal: SwitchCompat = findViewById(R.id.switch_local_service)
        val switchHide: SwitchCompat = findViewById(R.id.switch_background_hide)
        val switchMemory: SwitchCompat = findViewById(R.id.switch_workspace_memory)

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

