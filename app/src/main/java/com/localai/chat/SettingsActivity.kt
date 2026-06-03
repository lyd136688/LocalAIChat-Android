package com.localai.chat

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    
    // Settings sections
    private lateinit var sectionModelProvider: LinearLayout
    private lateinit var sectionSceneConfig: LinearLayout
    private lateinit var sectionLocalModel: LinearLayout
    private lateinit var sectionWorkspaceMemory: LinearLayout
    private lateinit var sectionMCPTools: LinearLayout
    private lateinit var sectionLocalService: LinearLayout
    private lateinit var sectionAlpineEnv: LinearLayout
    private lateinit var sectionBackgroundHide: LinearLayout
    private lateinit var sectionAlarm: LinearLayout
    
    private lateinit var switchLocalService: Switch
    private lateinit var switchBackgroundHide: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initViews()
        setupListeners()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        
        sectionModelProvider = findViewById(R.id.sectionModelProvider)
        sectionSceneConfig = findViewById(R.id.sectionSceneConfig)
        sectionLocalModel = findViewById(R.id.sectionLocalModel)
        sectionWorkspaceMemory = findViewById(R.id.sectionWorkspaceMemory)
        sectionMCPTools = findViewById(R.id.sectionMCPTools)
        sectionLocalService = findViewById(R.id.sectionLocalService)
        sectionAlpineEnv = findViewById(R.id.sectionAlpineEnv)
        sectionBackgroundHide = findViewById(R.id.sectionBackgroundHide)
        sectionAlarm = findViewById(R.id.sectionAlarm)
        
        switchLocalService = findViewById(R.id.switchLocalService)
        switchBackgroundHide = findViewById(R.id.switchBackgroundHide)
        
        btnBack.setOnClickListener { finish() }
    }
    
    private fun setupListeners() {
        sectionModelProvider.setOnClickListener {
            Toast.makeText(this, "模型提供商设置", Toast.LENGTH_SHORT).show()
        }
        
        sectionSceneConfig.setOnClickListener {
            Toast.makeText(this, "场景配置", Toast.LENGTH_SHORT).show()
        }
        
        sectionLocalModel.setOnClickListener {
            Toast.makeText(this, "本地模型管理", Toast.LENGTH_SHORT).show()
        }
        
        sectionWorkspaceMemory.setOnClickListener {
            Toast.makeText(this, "工作区记忆", Toast.LENGTH_SHORT).show()
        }
        
        sectionMCPTools.setOnClickListener {
            Toast.makeText(this, "MCP工具设置", Toast.LENGTH_SHORT).show()
        }
        
        sectionLocalService.setOnClickListener {
            Toast.makeText(this, "本地服务设置", Toast.LENGTH_SHORT).show()
        }
        
        sectionAlpineEnv.setOnClickListener {
            Toast.makeText(this, "Alpine环境配置", Toast.LENGTH_SHORT).show()
        }
        
        sectionBackgroundHide.setOnClickListener {
            switchBackgroundHide.isChecked = !switchBackgroundHide.isChecked
        }
        
        sectionAlarm.setOnClickListener {
            Toast.makeText(this, "闹钟设置", Toast.LENGTH_SHORT).show()
        }
        
        switchLocalService.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "本地服务: ${if (isChecked) "开启" else "关闭"}", Toast.LENGTH_SHORT).show()
        }
    }
}
