package com.localai.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class ModelManagerActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var modelsDir: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_manager)

        listView = findViewById(R.id.list_models)

        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        setupModelsDir()
        refreshModelList()
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AlertDialog.Builder(this)
                .setTitle("需要存储权限")
                .setMessage("请授予“所有文件访问权限”以读取模型文件。")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("取消") { _, _ -> finish() }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupModelsDir()
                refreshModelList()
            } else {
                Toast.makeText(this, "需要存储权限才能读取模型", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupModelsDir() {
        modelsDir = File(getExternalFilesDir(null), "models")
        if (!modelsDir!!.exists()) {
            modelsDir!!.mkdirs()
        }
    }

    private fun refreshModelList() {
        val models = try {
            modelsDir?.listFiles()?.filter { it.extension == "gguf" }?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, models)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = models[position]
            getSharedPreferences("app_config", MODE_PRIVATE).edit()
                .putString("current_model", File(modelsDir, selected).absolutePath).apply()
            Toast.makeText(this, "已选择模型: $selected", Toast.LENGTH_SHORT).show()
            finish()
        }
        if (models.isEmpty()) {
            Toast.makeText(this, "没有找到模型文件，请将 .gguf 模型放入\n${modelsDir?.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
