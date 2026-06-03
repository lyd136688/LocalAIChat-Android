package com.localai.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.localai.chat.R

class ServiceFragment : Fragment() {
    
    private lateinit var textStatus: TextView
    private lateinit var textModelInfo: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        textStatus = view.findViewById(R.id.textStatus)
        textModelInfo = view.findViewById(R.id.textModelInfo)
        
        updateServiceStatus()
    }
    
    private fun updateServiceStatus() {
        textStatus.text = "本地推理服务运行中"
        textModelInfo.text = "当前模型: 未加载\n内存使用: 0MB\n推理速度: 0 tokens/s"
    }
}
