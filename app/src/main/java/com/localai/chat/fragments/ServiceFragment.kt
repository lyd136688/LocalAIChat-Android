package com.localai.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.localai.chat.MyApplication
import com.localai.chat.R
import com.localai.chat.utils.InferenceScheduler
import kotlinx.coroutines.launch

class ServiceFragment : Fragment() {
    
    private lateinit var textStatus: TextView
    private lateinit var textModelInfo: TextView
    private lateinit var textHardwareInfo: TextView
    private lateinit var btnLoadModel: Button
    private lateinit var btnUnloadModel: Button
    
    private lateinit var scheduler: InferenceScheduler
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        scheduler = InferenceScheduler(requireContext())
        
        textStatus = view.findViewById(R.id.textStatus)
        textModelInfo = view.findViewById(R.id.textModelInfo)
        textHardwareInfo = view.findViewById(R.id.textHardwareInfo)
        btnLoadModel = view.findViewById(R.id.btnLoadModel)
        btnUnloadModel = view.findViewById(R.id.btnUnloadModel)
        
        updateServiceStatus()
        
        btnLoadModel.setOnClickListener {
            loadModel()
        }
        
        btnUnloadModel.setOnClickListener {
            scheduler.unloadLocalModel()
            updateServiceStatus()
        }
    }
    
    private fun updateServiceStatus() {
        val hardware = MyApplication.instance.hardwareDetector
        val ramGB = hardware.getTotalRAMGB()
        val cpuInfo = hardware.getCPUInfo()
        
        textHardwareInfo.text = "硬件信息:\n${hardware.getDeviceInfo()}"
        
        if (scheduler.isLocalModelLoaded()) {
            textStatus.text = "本地推理服务: 运行中 ✅"
            textStatus.setTextColor(resources.getColor(R.color.accent_primary, null))
            textModelInfo.text = scheduler.getCurrentModelInfo()
            btnLoadModel.visibility = View.GONE
            btnUnloadModel.visibility = View.VISIBLE
        } else {
            textStatus.text = "本地推理服务: 未启动 ❌"
            textStatus.setTextColor(resources.getColor(R.color.accent_error, null))
            textModelInfo.text = "未加载模型\n推荐模型大小: ${hardware.getRecommendedModelSize()}"
            btnLoadModel.visibility = View.VISIBLE
            btnUnloadModel.visibility = View.GONE
        }
    }
    
    private fun loadModel() {
        lifecycleScope.launch {
            val modelsDir = requireContext().getExternalFilesDir("models")
            val ggufFiles = modelsDir?.listFiles { _, name -> name.endsWith(".gguf") }
            
            if (!ggufFiles.isNullOrEmpty()) {
                val loaded = scheduler.loadLocalModel(ggufFiles[0].absolutePath)
                if (loaded) {
                    Toast.makeText(requireContext(), "模型加载成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "模型加载失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "请先下载模型", Toast.LENGTH_SHORT).show()
            }
            updateServiceStatus()
        }
    }
}

