package com.localai.chat

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var contentContainer: LinearLayout
    private var currentPage = PAGE_CHAT

    companion object {
        private const val PAGE_CHAT = 0
        private const val PAGE_SERVICE = 1
        private const val PAGE_MARKET = 2
        private const val PAGE_WORKSPACE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dp = resources.displayMetrics.density
        val pad8 = (8 * dp).toInt()
        val pad12 = (12 * dp).toInt()
        val pad16 = (16 * dp).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 顶部栏
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(pad12, pad12, pad12, pad12)
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            gravity = Gravity.CENTER_VERTICAL
        }
        topBar.addView(ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_preferences)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                android.content.Intent(this@MainActivity, SettingsActivity::class.java).also {
                    startActivity(it)
                }
            }
        }, LinearLayout.LayoutParams(pad16 * 2, pad16 * 2))

        topBar.addView(TextView(this).apply {
            text = "本地AI聊天"
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        topBar.addView(ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_save)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                android.content.Intent(this@MainActivity, MemoryCenterActivity::class.java).also {
                    startActivity(it)
                }
            }
        }, LinearLayout.LayoutParams(pad16 * 2, pad16 * 2))

        root.addView(topBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        // 内容容器
        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
        }
        root.addView(contentContainer, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        // 底部导航
        val navView = BottomNavigationView(this).apply {
            id = android.R.id.tabs
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            itemIconTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#CCCCCC")
            )
            itemTextColor = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#CCCCCC")
            )
            menu.add(0, PAGE_CHAT, 0, "对话").setIcon(android.R.drawable.ic_menu_send)
            menu.add(0, PAGE_SERVICE, 1, "服务").setIcon(android.R.drawable.ic_menu_help)
            menu.add(0, PAGE_MARKET, 2, "市场").setIcon(android.R.drawable.ic_menu_search)
            menu.add(0, PAGE_WORKSPACE, 3, "工作区").setIcon(android.R.drawable.ic_menu_edit)

            selectedItemId = PAGE_CHAT

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    PAGE_CHAT -> showPage(PAGE_CHAT)
                    PAGE_SERVICE -> showPage(PAGE_SERVICE)
                    PAGE_MARKET -> showPage(PAGE_MARKET)
                    PAGE_WORKSPACE -> showPage(PAGE_WORKSPACE)
                }
                true
            }
        }
        root.addView(navView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (56 * dp).toInt()
        ))

        setContentView(root)
        showPage(PAGE_CHAT)
    }

    private fun showPage(page: Int) {
        currentPage = page
        contentContainer.removeAllViews()
        val pageView: View = when (page) {
            PAGE_SERVICE -> buildServicePage()
            PAGE_MARKET -> buildMarketPage()
            PAGE_WORKSPACE -> buildWorkspacePage()
            else -> buildChatPage()
        }
        contentContainer.addView(pageView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ))
    }

    private fun buildChatPage(): View {
        val dp = resources.displayMetrics.density
        val pad16 = (16 * dp).toInt()

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad16, pad16, pad16, pad16)
            gravity = Gravity.CENTER

            addView(TextView(this@MainActivity).apply {
                text = "💬 对话页面"
                setTextColor(Color.WHITE)
                textSize = 20f
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            addView(TextView(this@MainActivity).apply {
                text = "在这里与AI进行对话\n\n点击下方按钮进入聊天"
                setTextColor(Color.parseColor("#888888"))
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, pad16, 0, pad16)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            addView(TextView(this@MainActivity).apply {
                text = "开始对话"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(pad16 * 2, pad16, pad16 * 2, pad16)
                setBackgroundColor(Color.parseColor("#4A90D9"))
                setOnClickListener {
                    startActivity(android.content.Intent(this@MainActivity, ChatActivity::class.java))
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    private fun buildServicePage(): View {
        val dp = resources.displayMetrics.density
        val pad16 = (16 * dp).toInt()
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad16, pad16, pad16, pad16)
            addView(TextView(this@MainActivity).apply {
                text = "🎯 智能服务"
                setTextColor(Color.WHITE)
                textSize = 20f
            })
            addView(TextView(this@MainActivity).apply {
                text = "\n选择一个服务快速开始\n\n• 智能问答\n• 创意写作\n• 代码辅助\n• 翻译工具\n• 数据分析"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 14f
                setPadding(0, pad16, 0, 0)
            })
        }
    }

    private fun buildMarketPage(): View {
        val dp = resources.displayMetrics.density
        val pad16 = (16 * dp).toInt()
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad16, pad16, pad16, pad16)
            addView(TextView(this@MainActivity).apply {
                text = "🏪 模型市场"
                setTextColor(Color.WHITE)
                textSize = 20f
            })
            addView(TextView(this@MainActivity).apply {
                text = "\n浏览和下载模型\n\n• Llama-2-7B\n• Qwen-7B\n• Mistral-7B\n• Phi-2"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 14f
                setPadding(0, pad16, 0, 0)
            })
        }
    }

    private fun buildWorkspacePage(): View {
        val dp = resources.displayMetrics.density
        val pad16 = (16 * dp).toInt()
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad16, pad16, pad16, pad16)
            addView(TextView(this@MainActivity).apply {
                text = "⚙️ 工作区"
                setTextColor(Color.WHITE)
                textSize = 20f
            })
            addView(TextView(this@MainActivity).apply {
                text = "\n管理你的工作文件和设置\n\n点击按钮进入 Workspace"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 14f
                setPadding(0, pad16, 0, 0)
            })
            addView(TextView(this@MainActivity).apply {
                text = "打开 Workspace"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(pad16 * 2, pad16, pad16 * 2, pad16)
                setBackgroundColor(Color.parseColor("#4A90D9"))
                setPadding(pad16 * 2, pad16, pad16 * 2, pad16)
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = pad16 }
                setOnClickListener {
                    startActivity(android.content.Intent(this@MainActivity, WorkspaceActivity::class.java))
                }
            })
        }
    }
}

