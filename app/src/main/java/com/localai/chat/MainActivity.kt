package com.localai.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnSelectModel: Button
    private lateinit var btnStartChat: Button
    private lateinit var btnOpenBrowser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        btnSelectModel = findViewById(R.id.btn_select_model)
        btnStartChat = findViewById(R.id.btn_start_chat)
        btnOpenBrowser = findViewById(R.id.btn_open_browser)

        checkPermissions()

        btnSelectModel.setOnClickListener {
            startActivity(Intent(this, ModelManagerActivity::class.java))
        }
        btnStartChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        btnOpenBrowser.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            if (permissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                    .setTitle("需要存储权限")
                    .setMessage("请授予“所有文件访问权限”，以便保存模型和记忆数据。")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
            }
        }
    }
}
