package com.localai.chat

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.localai.chat.utils.MemoryManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var memoryManager: MemoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val app = application
        if (app is MyApplication) memoryManager = app.memoryManager

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val itemModelProvider = findViewById<LinearLayout>(R.id.item_model_provider)
        val itemSceneConfig = findViewById<LinearLayout>(R.id.item_scene_config)
        val itemLocalModel = findViewById<LinearLayout>(R.id.item_local_model)
        val itemWorkspaceMemory = findViewById<LinearLayout>(R.id.item_workspace_memory)
        val itemMcpTools = findViewById<LinearLayout>(R.id.item_mcp_tools)
        val itemAlpine = findViewById<LinearLayout>(R.id.item_alpine)
        val itemAlarm = findViewById<LinearLayout>(R.id.item_alarm)
        val itemLocalService = findViewById<LinearLayout>(R.id.item_local_service)
        val itemBackgroundHide = findViewById<LinearLayout>(R.id.item_background_hide)

        val switchLocal = findViewById<SwitchCompat>(R.id.switch_local_service)
        val switchHide = findViewById<SwitchCompat>(R.id.switch_background_hide)
        val switchMemory = findViewById<SwitchCompat>(R.id.switch_workspace_memory)

        btnBack.setOnClickListener { finish() }

        itemModelProvider.setOnClickListener { showModelProviderDialog() }
        itemSceneConfig.setOnClickListener { showSceneConfigDialog() }
        itemLocalModel.setOnClickListener { Toast.makeText(this, "本地模型管理功能开发中", Toast.LENGTH_SHORT).show() }
        itemWorkspaceMemory.setOnClickListener { showWorkspaceMemoryDialog() }
        itemMcpTools.setOnClickListener { showMcpToolsDialog() }
        itemAlpine.setOnClickListener { Toast.makeText(this, "Alpine 环境功能开发中", Toast.LENGTH_SHORT).show() }
        itemAlarm.setOnClickListener { Toast.makeText(this, "闹钟设置功能开发中", Toast.LENGTH_SHORT).show() }
        itemLocalService.setOnClickListener { switchLocal.isChecked = !switchLocal.isChecked }
        itemBackgroundHide.setOnClickListener { switchHide.isChecked = !switchHide.isChecked }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        switchLocal.isChecked = prefs.getBoolean("local_service_enabled", false)
        switchHide.isChecked = prefs.getBoolean("background_hide", false)
        switchMemory.isChecked = prefs.getBoolean("workspace_memory_enabled", true)

        switchLocal.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("local_service_enabled", isChecked).apply()
        }
        switchHide.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("background_hide", isChecked).apply()
        }
        switchMemory.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("workspace_memory_enabled", isChecked).apply()
        }
    }

    private fun showModelProviderDialog() {
        val providers = arrayOf("OpenAI", "Claude", "Qwen", "DeepSeek", "本地模型")
        AlertDialog.Builder(this)
            .setTitle("选择模型提供商")
            .setSingleChoiceItems(providers, -1) { dialog, which ->
                getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit().putString("model_provider", providers[which]).apply()
                Toast.makeText(this, "已选择：${providers[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showSceneConfigDialog() {
        AlertDialog.Builder(this)
            .setTitle("场景模型配置")
            .setMessage("为不同场景绑定默认模型：\n\n· 聊天对话：qwen3.5-plus\n· 代码生成：qwen3.5-flash\n· 文档总结：qwen-plus\n· 图片理解：qwen3-vl-plus\n\n记忆：使用向量化检索（字符 n-gram），内容不压缩归档。")
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun showWorkspaceMemoryDialog() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val enabled = prefs.getBoolean("workspace_memory_enabled", true)
        val stateText = if (enabled) "已启用（向量化检索）" else "已禁用"
        AlertDialog.Builder(this)
            .setTitle("Workspace 记忆配置")
            .setMessage("启用后，对话内容将被存储并支持语义检索。\n\n当前状态：$stateText\n\n· 采用字符 n-gram 向量化\n· 余弦相似度排序\n· 原始内容完整保留，不做压缩归档")
            .setPositiveButton("管理记忆") { _, _ ->
                val intent = Intent()
                intent.component = ComponentName(this@SettingsActivity, "com.localai.chat.MemoryCenterActivity")
                startActivity(intent)
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showMcpToolsDialog() {
        AlertDialog.Builder(this)
            .setTitle("MCP 工具管理")
            .setMessage("可添加、启动/停止远程 MCP 服务。\n\n可用工具：\n· web_search\n· calculator\n· file_manager\n· terminal")
            .setPositiveButton("添加工具", null)
            .setNegativeButton("关闭", null)
            .show()
    }
}

