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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

// ------ 本地数据类（不依赖 data.models 包） ------
data class LocalMemoryItem(
    val id: String,
    val content: String,
    val type: String,
    val source: String,
    val createdAt: Long,
    val tags: List<String>,
    val relevanceScore: Float
)

// ------ 自包含 Adapter（不依赖 adapters 包） ------
class InlineMemoryAdapter(
    private val onDeleteClick: (LocalMemoryItem) -> Unit,
    private val onRecallClick: (LocalMemoryItem) -> Unit
) : RecyclerView.Adapter<InlineMemoryAdapter.ViewHolder>() {

    private var list: List<LocalMemoryItem> = emptyList()

    fun setData(data: List<LocalMemoryItem>) {
        this.list = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val density = context.resources.displayMetrics.density
        val dp8 = (8 * density).toInt()
        val dp12 = (12 * density).toInt()
        val dp16 = (16 * density).toInt()

        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp12, dp12, dp12, dp12)
            setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
            val params = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dp8
            layoutParams = params
        }

        val content = TextView(context).apply {
            id = android.view.View.generateViewId()
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            maxLines = 4
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val meta = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, dp8, 0, 0)
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val source = TextView(context).apply {
            id = android.view.View.generateViewId()
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val time = TextView(context).apply {
            id = android.view.View.generateViewId()
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#888888"))
            setPadding(dp16, 0, dp16, 0)
        }

        val btnRecall = ImageView(context).apply {
            id = android.view.View.generateViewId()
            setImageResource(android.R.drawable.ic_menu_recent_history)
            setPadding(dp8, 0, dp8, 0)
            contentDescription = "唤起"
        }

        val btnDelete = ImageView(context).apply {
            id = android.view.View.generateViewId()
            setImageResource(android.R.drawable.ic_menu_delete)
            setPadding(dp8, 0, dp8, 0)
            contentDescription = "删除"
        }

        meta.addView(source)
        meta.addView(time)
        meta.addView(btnRecall)
        meta.addView(btnDelete)

        container.addView(content)
        container.addView(meta)

        val holder = ViewHolder(container, content, source, time, btnRecall, btnDelete)
        container.tag = holder
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(
        itemView: View,
        private val content: TextView,
        private val source: TextView,
        private val time: TextView,
        private val btnRecall: ImageView,
        private val btnDelete: ImageView
    ) : RecyclerView.ViewHolder(itemView) {

        private var currentItem: LocalMemoryItem? = null

        init {
            btnRecall.setOnClickListener { currentItem?.let { onRecallClick(it) } }
            btnDelete.setOnClickListener { currentItem?.let { onDeleteClick(it) } }
        }

        fun bind(item: LocalMemoryItem) {
            currentItem = item
            content.text = item.content.take(200)
            source.text = "来源: ${item.source}"
            time.text = formatTime(item.createdAt)
        }

        private fun formatTime(ts: Long): String {
            val formatter = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
            return formatter.format(java.util.Date(ts))
        }
    }
}

// ------ Activity 主体 ------
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

    private lateinit var memoryAdapter: InlineMemoryAdapter
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

        memoryAdapter = InlineMemoryAdapter(
            onDeleteClick = { memory -> deleteMemory(memory) },
            onRecallClick = { memory -> recallMemory(memory) }
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
            val memories = try {
                val app = application as? MyApplication
                if (app != null) app.memoryManager.getAllMemoriesOrderedByTime()
                else emptyList()
            } catch (e: Throwable) {
                emptyList()
            }
            showResult(memories, isSearch = false, query = null)
            progressBar.visibility = View.GONE
        }
    }

    private fun semanticSearch(query: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val memories = try {
                val app = application as? MyApplication
                if (app != null) app.memoryManager.searchMemoriesByVector(query, topK = 20)
                else emptyList()
            } catch (e: Throwable) {
                emptyList()
            }
            showResult(memories, isSearch = true, query = query)
            progressBar.visibility = View.GONE
        }
    }

    private fun showResult(memories: List<LocalMemoryItem>, isSearch: Boolean, query: String?) {
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

    private fun deleteMemory(memory: LocalMemoryItem) {
        lifecycleScope.launch {
            try {
                val app = application as? MyApplication
                app?.memoryManager?.deleteMemory(memory.id)
            } catch (e: Throwable) { /* ignore */ }

            if (isSearchMode) {
                val q = etSearch.text.toString().trim()
                if (q.isNotEmpty()) semanticSearch(q) else loadAllMemories()
            } else {
                loadAllMemories()
            }
        }
    }

    private fun recallMemory(memory: LocalMemoryItem) {
        val hint = if (memory.content.length > 30) memory.content.take(30) + "…" else memory.content
        Toast.makeText(this, "已唤起记忆：$hint", Toast.LENGTH_SHORT).show()
    }
}

