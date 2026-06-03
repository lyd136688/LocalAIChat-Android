package com.localai.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.MyApplication
import com.localai.chat.R
import com.localai.chat.adapters.ModelListAdapter
import com.localai.chat.data.models.ModelInfo
import com.localai.chat.network.RetrofitClient
import com.localai.chat.utils.HardwareDetector
import kotlinx.coroutines.launch

class MarketFragment : Fragment() {
    
    private lateinit var spinnerSource: Spinner
    private lateinit var editSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var textRecommendation: TextView
    private lateinit var recyclerModels: RecyclerView
    private lateinit var modelAdapter: ModelListAdapter
    
    private var currentSource = "HuggingFace"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_market, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupSourceSelector()
        setupRecyclerView()
        updateRecommendation()
        
        // 默认加载热门模型
        searchModels("")
    }
    
    private fun initViews(view: View) {
        spinnerSource = view.findViewById(R.id.spinnerSource)
        editSearch = view.findViewById(R.id.editSearch)
        btnSearch = view.findViewById(R.id.btnSearch)
        textRecommendation = view.findViewById(R.id.textRecommendation)
        recyclerModels = view.findViewById(R.id.recyclerModels)
        
        btnSearch.setOnClickListener {
            searchModels(editSearch.text.toString())
        }
    }
    
    private fun setupSourceSelector() {
        val sources = arrayOf("HuggingFace", "ModelScope")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sources)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSource.adapter = adapter
        
        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSource = sources[position]
                searchModels(editSearch.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupRecyclerView() {
        modelAdapter = ModelListAdapter { model ->
            downloadModel(model)
        }
        recyclerModels.layoutManager = LinearLayoutManager(requireContext())
        recyclerModels.adapter = modelAdapter
    }
    
    private fun updateRecommendation() {
        val ramGB = MyApplication.instance.hardwareDetector.getTotalRAMGB()
        val recommendedSize = when {
            ramGB >= 16 -> "13B"
            ramGB >= 8 -> "7B"
            else -> "3B"
        }
        textRecommendation.text = "设备内存: ${ramGB}GB | 推荐模型: ${recommendedSize}"
    }
    
    private fun searchModels(query: String) {
        lifecycleScope.launch {
            try {
                val models = if (currentSource == "HuggingFace") {
                    searchHuggingFace(query)
                } else {
                    searchModelScope(query)
                }
                modelAdapter.submitList(models)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
                // 加载默认数据
                modelAdapter.submitList(getDefaultModels())
            }
        }
    }
    
    private suspend fun searchHuggingFace(query: String): List<ModelInfo> {
        return try {
            val response = RetrofitClient.huggingFaceApi.searchModels(
                query = query.ifEmpty { "llama" },
                limit = 20
            )
            response.items.map { item ->
                ModelInfo(
                    id = item.modelId,
                    displayName = item.modelId.substringAfterLast("/"),
                    parameters = item.tags.find { it.endsWith("B") } ?: "Unknown",
                    size = "Unknown",
                    author = item.author ?: "Unknown"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getDefaultModels()
        }
    }
    
    private suspend fun searchModelScope(query: String): List<ModelInfo> {
        return try {
            val response = RetrofitClient.modelScopeApi.searchModelScopeModels(
                query = query.ifEmpty { "llama" }
            )
            response.Data?.Models?.map { model ->
                ModelInfo(
                    id = model.ModelId,
                    displayName = model.ModelName ?: model.ModelId,
                    parameters = "Unknown",
                    size = "Unknown",
                    author = model.UserName ?: "Unknown"
                )
            } ?: getDefaultModels()
        } catch (e: Exception) {
            e.printStackTrace()
            getDefaultModels()
        }
    }
    
    private fun getDefaultModels(): List<ModelInfo> {
        return listOf(
            ModelInfo("meta-llama/Llama-2-7b-chat", "LLaMA-2-7B-Chat", "7B", "4.1GB", "Meta"),
            ModelInfo("meta-llama/Llama-2-13b-chat", "LLaMA-2-13B-Chat", "13B", "7.9GB", "Meta"),
            ModelInfo("Qwen/Qwen-7B-Chat", "Qwen-7B-Chat", "7B", "4.2GB", "Alibaba"),
            ModelInfo("THUDM/chatglm3-6b", "ChatGLM3-6B", "6B", "3.6GB", "THUDM")
        )
    }
    
    private fun downloadModel(model: ModelInfo) {
        Toast.makeText(requireContext(), "开始下载: ${model.displayName}", Toast.LENGTH_SHORT).show()
        // 启动下载服务
        val downloadUrl = RetrofitClient.getGgufDownloadUrl(model.id, currentSource)
        // ... 启动DownloadService
    }
}

