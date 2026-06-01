package com.localai.chat

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ModelManagerActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val modelsDir = File(getExternalFilesDir(null), "models")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_manager)
        listView = findViewById(R.id.list_models)

        if (!modelsDir.exists()) modelsDir.mkdirs()
        refreshModelList()
    }

    private fun refreshModelList() {
        val models = modelsDir.listFiles()?.filter { it.extension == "gguf" }?.map { it.name } ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, models)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = models[position]
            getSharedPreferences("app_config", MODE_PRIVATE).edit()
                .putString("current_model", File(modelsDir, selected).absolutePath).apply()
        }
    }
}
