package com.localai.chat

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.adapters.MemoryListAdapter
import com.localai.chat.data.models.MemoryItem
import com.localai.chat.utils.MemoryManager
import kotlinx.coroutines.launch

class MemoryCenterActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var tabShortTerm: LinearLayout
    private lateinit var tabLongTerm: LinearLayout
    private lateinit var textShortTerm: TextView
    private lateinit var textLongTerm: TextView
    private lateinit var recyclerMemories: RecyclerView
    private lateinit var memoryAdapter: MemoryListAdapter
    
    private var currentType = MemoryManager.TYPE_SHORT_TERM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_center)
        
        initViews()
        setupTabs()
        setupRecyclerView()
        loadMemories()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tabShortTerm = findViewById(R.id.tabShortTerm)
        tabLongTerm = findViewById(R.id.tabLongTerm)
        textShortTerm = findViewById(R.id.textShortTerm)
        textLongTerm = findViewById(R.id.textLongTerm)
        recyclerMemories = findViewById(R.id.recyclerMemories)
        
        btnBack.setOnClickListener { finish() }
    }
    
    private fun setupTabs() {
        tabShortTerm.setOnClickListener {
            currentType = MemoryManager.TYPE_SHORT_TERM
            updateTabUI()
            loadMemories()
        }
        
        tabLongTerm.setOnClickListener {
            currentType = MemoryManager.TYPE_LONG_TERM
            updateTabUI()
            loadMemories()
        }
        
        updateTabUI()
    }
    
    private fun updateTabUI() {
        if (currentType == MemoryManager.TYPE_SHORT_TERM) {
            textShortTerm.setTextColor(getColor(R.color.accent_primary))
            textLongTerm.setTextColor(getColor(R.color.text_secondary))
        } else {
            textShortTerm.setTextColor(getColor(R.color.text_secondary))
            textLongTerm.setTextColor(R.color.accent_primary)
        }
    }
    
    private fun setupRecyclerView() {
        memoryAdapter = MemoryListAdapter(
            onDelete = { memory -> deleteMemory(memory) },
            onArchive = { memory -> archiveMemory(memory) }
        )
        recyclerMemories.layoutManager = LinearLayoutManager(this)
        recyclerMemories.adapter = memoryAdapter
    }
    
    private fun loadMemories() {
        lifecycleScope.launch {
            val memories = if (currentType == MemoryManager.TYPE_SHORT_TERM) {
                MyApplication.instance.memoryManager.getShortTermMemories()
            } else {
                MyApplication.instance.memoryManager.getLongTermMemories()
            }
            memoryAdapter.submitList(memories)
        }
    }
    
    private fun deleteMemory(memory: MemoryItem) {
        lifecycleScope.launch {
            MyApplication.instance.memoryManager.deleteMemory(memory.id)
            loadMemories()
        }
    }
    
    private fun archiveMemory(memory: MemoryItem) {
        lifecycleScope.launch {
            MyApplication.instance.memoryManager.archiveMemory(memory.id)
            loadMemories()
        }
    }
}
