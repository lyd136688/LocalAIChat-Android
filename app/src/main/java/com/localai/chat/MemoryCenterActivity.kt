package com.localai.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
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
import com.localai.chat.data.models.MemoryItem
import com.localai.chat.utils.MemoryManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryCenterActivity : AppCompatActivity() {

    private lateinit var memoryManager: MemoryManager
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var tabShortTerm: TextView
    private lateinit var tabLongTerm: TextView
    private lateinit var tabSearchResult: TextView
    private lateinit var tvStats: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptyDesc: TextView
    private lateinit var rvMemories: RecyclerView
    private lateinit var memoryAdapter: InlineMemoryAdapter

    private var currentMode: Int = 0
    private var lastQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_center)

        val app = application
        memoryManager = (app as? MyApplication)?.memoryManager
            ?: error("MyApplication 未正确初始化 MemoryManager")

        etSearch = findViewById(R.id.et_search)
        btnSearch = findViewById(R.id.btn_search)
        tabShortTerm = findViewById(R.id.tab_short_term)
        tabLongTerm = findViewById(R.id.tab_long_term)
        tabSearchResult = findViewById(R.id.tab_search_result)
        tvStats = findViewById(R.id.tv_stats)
        progressBar = findViewById(R.id.progress_bar)
        emptyState = findViewById(R.id.empty_state)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptyDesc = findViewById(R.id.tv_empty_desc)
        rvMemories = findViewById(R.id.rv_memories)

        memoryAdapter = InlineMemoryAdapter(
            onDeleteClick = { memory -> deleteMemory(memory) }
        )
        rvMemories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMemories.adapter = memoryAdapter

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        tabShortTerm.setOnClickListener { switchMode(0) }
        tabLongTerm.setOnClickListener { switchMode(1) }
        tabSearchResult.setOnClickListener { /* 已是搜索模式 */ }

        btnSearch.setOnClickListener { performSearch(etSearch.text.toString()) }
        etSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch(etSearch.text.toString())
                true
            } else false
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank() && currentMode == 2) switchMode(0)
            }
        })

        switchMode(0)
        refreshStats()
    }

    private fun switchMode(mode: Int) {
        currentMode = mode
        tabShortTerm.setBackgroundResource(if (mode == 0) R.drawable.bg_tab_selected else 0)
        tabLongTerm.setBackgroundResource(if (mode == 1) R.drawable.bg_tab_selected else 0)
        tabSearchResult.setBackgroundResource(if (mode == 2) R.drawable.bg_tab_selected else 0)
        tabSearchResult.visibility = if (lastQuery.isNotBlank()) View.VISIBLE else View.GONE
        loadForCurrentMode()
    }

    private fun loadForCurrentMode() {
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        lifecycleScope.launch {
            val memories: List<MemoryItem> = when (currentMode) {
                1 -> memoryManager.getLongTermMemories()
                2 -> if (lastQuery.isNotBlank()) memoryManager.searchMemoriesByVector(lastQuery, 50) else emptyList()
                else -> memoryManager.getShortTermMemories()
            }
            renderList(memories)
            progressBar.visibility = View.GONE
            if (memories.isEmpty()) showEmptyState() else {
                emptyState.visibility = View.GONE
                rvMemories.visibility = View.VISIBLE
            }
        }
    }

    private fun renderList(memories: List<MemoryItem>) {
        memoryAdapter.setData(memories, currentMode == 2)
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        rvMemories.visibility = View.GONE
        when (currentMode) {
            1 -> {
                tvEmptyTitle.text = "还没有长期记忆"
                tvEmptyDesc.text = "整理后的重要信息会沉淀到长期记忆，可随时检索回顾。"
            }
            2 -> {
                tvEmptyTitle.text = "未找到相关记忆"
                tvEmptyDesc.text = "尝试其他关键词。\n提示：所有记忆均以向量方式存储，可被语义检索。"
            }
            else -> {
                tvEmptyTitle.text = "还没有短期记忆"
                tvEmptyDesc.text = "会话中的过程性信息会沉淀到短期记忆。"
            }
        }
    }

    private fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) { switchMode(0); return }
        lastQuery = trimmed
        switchMode(2)
    }

    private fun refreshStats() {
        lifecycleScope.launch {
            try {
                val stats = memoryManager.getStats()
                tvStats.text = "短期 ${stats.shortTermCount} 条 · 长期 ${stats.longTermCount} 条 · 向量检索已启用"
            } catch (t: Throwable) {
                tvStats.text = "向量化检索已启用 · 内容不压缩"
            }
        }
    }

    private fun deleteMemory(memory: MemoryItem) {
        lifecycleScope.launch {
            memoryManager.deleteMemory(memory.id)
            refreshStats()
            loadForCurrentMode()
        }
    }
}

/** 内联 RecyclerView 适配器（不依赖外部 XML 或 adapter 包） */
class InlineMemoryAdapter(
    private val onDeleteClick: (MemoryItem) -> Unit
) : RecyclerView.Adapter<InlineMemoryAdapter.ViewHolder>() {

    private var items: List<MemoryItem> = emptyList()
    private var showScore: Boolean = false

    fun setData(newItems: List<MemoryItem>, showScore: Boolean = false) {
        this.items = newItems
        this.showScore = showScore
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val density = context.resources.displayMetrics.density
        val dp12 = (12 * density).toInt()
        val dp8 = (8 * density).toInt()
        val dp4 = (4 * density).toInt()

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp12, dp12, dp12, dp12)
            setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp4, 0, dp4) }
        }

        val top = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        titleLp.rightMargin = dp8
        val tvTitle = TextView(context).apply {
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            textSize = 14f
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = titleLp
        }

        val btnDelete = Button(context).apply {
            text = "删除"
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
            setPadding(dp8, dp4, dp8, dp4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        top.addView(tvTitle)
        top.addView(btnDelete)

        val bottom = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp8, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvSource = TextView(context).apply {
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvScore = TextView(context).apply {
            setTextColor(android.graphics.Color.parseColor("#888888"))
            textSize = 12f
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        bottom.addView(tvSource)
        bottom.addView(tvScore)

        root.addView(top)
        root.addView(bottom)
        return ViewHolder(root, onDeleteClick, tvTitle, tvSource, tvScore, btnDelete)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], showScore)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        view: View,
        private val onDeleteClick: (MemoryItem) -> Unit,
        private val tvContent: TextView,
        private val tvSource: TextView,
        private val tvScore: TextView,
        private val btnDelete: Button
    ) : RecyclerView.ViewHolder(view) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(memory: MemoryItem, showScore: Boolean) {
            tvContent.text = memory.content
            val typeLabel = when (memory.type) {
                "long_term" -> "长期"
                else -> "短期"
            }
            tvSource.text = "$typeLabel · ${memory.source} · ${dateFormat.format(Date(memory.createdAt))}"
            if (showScore && memory.relevanceScore > 0f) {
                tvScore.visibility = View.VISIBLE
                tvScore.text = String.format(Locale.getDefault(), "相似度 %.2f", memory.relevanceScore)
            } else {
                tvScore.visibility = View.GONE
            }
            btnDelete.setOnClickListener { onDeleteClick(memory) }
        }
    }
}

