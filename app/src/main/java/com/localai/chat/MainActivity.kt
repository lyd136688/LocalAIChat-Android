package com.localai.chat

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val tagService = "SERVICE"
    private val tagMarket = "MARKET"
    private val tagChat = "CHAT"
    private val tagWorkspace = "WORKSPACE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ivSettings = findViewById<ImageView>(R.id.iv_settings)
        val ivMemory = findViewById<ImageView>(R.id.iv_memory)
        val navView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        ivSettings.setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName(this@MainActivity, "com.localai.chat.SettingsActivity")
            startActivity(intent)
        }

        ivMemory.setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName(this@MainActivity, "com.localai.chat.MemoryCenterActivity")
            startActivity(intent)
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_service -> { switchFragment(tagService); true }
                R.id.nav_market -> { switchFragment(tagMarket); true }
                R.id.nav_chat -> { switchFragment(tagChat); true }
                R.id.nav_workspace -> { switchFragment(tagWorkspace); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            switchFragment(tagChat)
        }

        checkPermissions()
    }

    private fun switchFragment(tag: String) {
        val fm: FragmentManager = supportFragmentManager
        val existing = fm.findFragmentByTag(tag)
        val target: Fragment = existing ?: buildFragment(tag)

        fm.beginTransaction()
            .replace(R.id.fragment_container, target, tag)
            .commitAllowingStateLoss()
    }

    private fun buildFragment(tag: String): Fragment {
        return when (tag) {
            tagService -> ServiceInlineFragment()
            tagMarket -> MarketInlineFragment()
            tagChat -> ChatInlineFragment()
            tagWorkspace -> WorkspaceInlineFragment()
            else -> ChatInlineFragment()
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val denied = grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(this, "部分权限被拒绝，功能可能受限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// ============ 内联 Fragment 实现（不依赖 fragments 包） ============

class ChatInlineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
            setPadding(64, 64, 64, 64)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(context).apply {
            text = "聊天对话"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }

        val desc = TextView(context).apply {
            text = "点击右上角 ➜ 进入 ChatActivity 开始对话。\n对话内容将自动存入向量记忆库，支持语义检索，不做压缩归档。"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        val btnStart = android.widget.Button(context).apply {
            text = "进入聊天"
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            setBackgroundColor(android.graphics.Color.parseColor("#4A90E2"))
            val density = resources.displayMetrics.density
            val padding = (12 * density).toInt()
            setPadding(padding, padding, padding, padding)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (32 * density).toInt(), 0, 0)
                gravity = Gravity.CENTER_HORIZONTAL
            }
            layoutParams = params
            setOnClickListener {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                startActivity(intent)
            }
        }

        root.addView(title)
        root.addView(desc)
        root.addView(btnStart)
        return root
    }
}

class ServiceInlineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
            setPadding(64, 64, 64, 64)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(context).apply {
            text = "服务场景"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }
        val desc = TextView(context).apply {
            text = "智能问答 / 代码助手 / 写作润色 / 翻译 / 数据分析 / 学习辅导 / 会议总结 / 邮件写作\n\n根据选择的场景，AI 会以相应的语气与策略为你服务。"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        val btn = android.widget.Button(context).apply {
            text = "进入场景聊天"
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            setBackgroundColor(android.graphics.Color.parseColor("#7ED321"))
            val density = resources.displayMetrics.density
            val padding = (12 * density).toInt()
            setPadding(padding, padding, padding, padding)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (32 * density).toInt(), 0, 0)
                gravity = Gravity.CENTER_HORIZONTAL
            }
            layoutParams = params
            setOnClickListener {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("SERVICE_TITLE", "默认场景")
                startActivity(intent)
            }
        }

        root.addView(title)
        root.addView(desc)
        root.addView(btn)
        return root
    }
}

class MarketInlineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
            setPadding(64, 64, 64, 64)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val title = TextView(context).apply {
            text = "模型市场"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }
        val desc = TextView(context).apply {
            text = "浏览开源模型，按设备硬件推荐合适的模型版本。"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        root.addView(title)
        root.addView(desc)
        return root
    }
}

class WorkspaceInlineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
            setPadding(64, 64, 64, 64)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val title = TextView(context).apply {
            text = "工作区"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }
        val desc = TextView(context).apply {
            text = "查看本地工作目录中的文件与资源。"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        root.addView(title)
        root.addView(desc)
        return root
    }
}

