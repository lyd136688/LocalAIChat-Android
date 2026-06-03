package com.localai.chat.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ModelLoadReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.localai.chat.MODEL_LOADED" -> {
                val modelName = intent.getStringExtra("model_name") ?: "未知模型"
                Toast.makeText(context, "模型已自动加载: $modelName", Toast.LENGTH_LONG).show()
            }
        }
    }
}
