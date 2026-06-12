package com.localai.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.adapters.MemoryListAdapter
import com.localai.chat.data.models.MemoryItem
import kotlinx.coroutines.launch

class MemoryCenterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tabAll: TextView
    private lateinit var tabSearch: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptyDesc: TextView
    private lateinit var rvMemories: RecyclerView

    private lateinit var memoryAdapter: MemoryListAdapter
    private var isSearchMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_center)

        btnBack = findViewById(R.id.btn_back)
        tabAll = findViewById(R.id.tab_short_term)
        tabSearch = findViewById(R.id.tab_long_term)
        etSearch = findViewById(R.id.et_memory_search)
        btnSearch = findViewById(R.id.btn_memory_search)
        progressBar = findViewById(R.id.progress_bar)
        emptyState = findViewById(R.id.empty_state)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptyDesc = findViewById(R.id.tv_empty_desc)
        rvMemories = findViewById(R.id.rv_memories)

        tabAll.text = "全部记忆"
        tabSearch.text = "语义检索"

        memoryAdapter = MemoryListAdapter(
            onDeleteClick = { memory -> deleteMemory(memory) },
            onArchiveClick = { memory -> recallMemory(memory) }
        )

        rvMemories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMemories.adapter = memoryAdapter

        btnBack.setOnClickListener { finish() }

        tabAll.setOnClickListener {
            isSearchMode = false
            updateTabUI()
            loadAllMemories()
        }

        tabSearch.setOnClickListener {
            isSearchMode = true
            updateTabUI()
            loadAllMemories()
        }

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                semanticSearch(query)
            } else {
                loadAllMemories()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        updateTabUI()
        loadAllMemories()
    }

    private fun updateTabUI() {
        if (isSearchMode) {
            tabAll.setBackgroundResource(0)
            tabSearch.setBackgroundResource(R.drawable.bg_tab_selected)
            etSearch.visibility = View.VISIBLE
            btnSearch.visibility = View.VISIBLE
        } else {
            tabAll.setBackgroundResource(R.drawable.bg_tab_selected)
            tabSearch.setBackgroundResource(0)
            etSearch.visibility = View.GONE
            btnSearch.visibility = View.GONE
        }
    }

    private fun loadAllMemories() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val manager = (application as MyApplication).memoryManager
            val memories = manager.getAllMemoriesOrderedByTime()
            showResult(memories, isSearch = false, query = null)
            progressBar.visibility = View.GONE
        }
    }

    private fun semanticSearch(query: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val manager = (application as MyApplication).memoryManager
            val memories = manager.searchMemoriesByVector(query, topK = 20)
            showResult(memories, isSearch = true, query = query)
            progressBar.visibility = View.GONE
        }
    }

    private fun showResult(memories: List<MemoryItem>, isSearch: Boolean, query: String?) {
        if (memories.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvMemories.visibility = View.GONE
            if (isSearch && !query.isNullOrEmpty()) {
                tvEmptyTitle.text = "没有匹配到相关记忆"
                tvEmptyDesc.text = "记忆向量数据库中未找到与「$query」接近的内容，试试换个关键词？"
            } else {
                tvEmptyTitle.text = "还没有记忆"
                tvEmptyDesc.text = "对话中的信息会以向量形式存入本地记忆库，支持语义检索，不做压缩。"
            }
        } else {
            emptyState.visibility = View.GONE
            rvMemories.visibility = View.VISIBLE
            memoryAdapter.setData(memories)
        }
    }

    private fun deleteMemory(memory: MemoryItem) {
        lifecycleScope.launch {
            (application as MyApplication).memoryManager.deleteMemory(memory.id)
            if (isSearchMode) {
                val q = etSearch.text.toString().trim()
                if (q.isNotEmpty()) semanticSearch(q) else loadAllMemories()
            } else {
                loadAllMemories()
            }
        }
    }

    private fun recallMemory(memory: MemoryItem) {
        val hint = if (memory.content.length > 30) memory.content.take(30) + "…" else memory.content
        android.widget.Toast.makeText(
            this,
            "已唤起记忆：$hint",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
