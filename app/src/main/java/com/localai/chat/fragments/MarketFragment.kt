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
    private val huggingFaceModels = listOf(
        ModelInfo("meta-llama/Llama-2-7b-chat", "LLaMA-2-7B-Chat", "7B", "4.1GB", "Meta"),
        ModelInfo("meta-llama/Llama-2-13b-chat", "LLaMA-2-13B-Chat", "13B", "7.9GB", "Meta"),
        ModelInfo("Qwen/Qwen-7B-Chat", "Qwen-7B-Chat", "7B", "4.2GB", "Alibaba"),
        ModelInfo("THUDM/chatglm3-6b", "ChatGLM3-6B", "6B", "3.6GB", "THUDM"),
        ModelInfo("01-ai/Yi-6B-Chat", "Yi-6B-Chat", "6B", "3.5GB", "01.AI")
    )
    
    private val modelScopeModels = listOf(
        ModelInfo("damo/nlp_structbert_siamese-uie_chinese-base", "StructBERT", "Base", "400MB", "阿里达摩院"),
        ModelInfo("baichuan-inc/Baichuan2-7B-Chat", "Baichuan2-7B", "7B", "4.1GB", "百川智能"),
        ModelInfo("ZhipuAI/chatglm3-6b", "ChatGLM3-6B", "6B", "3.6GB", "智谱AI"),
        ModelInfo("qwen/Qwen-7B-Chat", "Qwen-7B-Chat", "7B", "4.2GB", "通义千问")
    )
    
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
        loadModels()
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
                loadModels()
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
    
    private fun loadModels() {
        val models = if (currentSource == "HuggingFace") huggingFaceModels else modelScopeModels
        modelAdapter.submitList(models)
    }
    
    private fun searchModels(query: String) {
        if (query.isEmpty()) {
            loadModels()
            return
        }
        val allModels = if (currentSource == "HuggingFace") huggingFaceModels else modelScopeModels
        val filtered = allModels.filter { it.name.contains(query, ignoreCase = true) }
        modelAdapter.submitList(filtered)
    }
    
    private fun downloadModel(model: ModelInfo) {
        Toast.makeText(requireContext(), "开始下载: ${model.displayName}", Toast.LENGTH_SHORT).show()
    }
}
