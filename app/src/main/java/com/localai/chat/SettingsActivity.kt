package com.localai.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = buildUI()
        setContentView(root)
    }

    private fun buildUI(): View {
        val dp = resources.displayMetrics.density
        val pad8 = (8 * dp).toInt()
        val pad12 = (12 * dp).toInt()
        val pad16 = (16 * dp).toInt()

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // 顶部栏
            addView(LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(pad12, pad12, pad12, pad12)
                setBackgroundColor(Color.parseColor("#1E1E1E"))
                gravity = Gravity.CENTER_VERTICAL

                addView(ImageView(this@SettingsActivity).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    setColorFilter(Color.WHITE)
                    setOnClickListener { finish() }
                }, LinearLayout.LayoutParams(pad16 * 2, pad16 * 2))

                addView(TextView(this@SettingsActivity).apply {
                    text = "设置"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setPadding(pad12, 0, 0, 0)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { weight = 1f })
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            // 滚动内容
            addView(ScrollView(this@SettingsActivity).apply {
                setPadding(0, pad12, 0, pad12)

                addView(LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(pad16, 0, pad16, 0)

                    addSectionTitle("模型与记忆")
                    addRow("模型提供商", "配置模型地址和密钥") { showModelProviderDialog() }
                    addRow("场景模型配置", "绑定模型到场景") { showSceneConfigDialog() }
                    addRow("本地模型服务", "管理本地推理模型") {
                        Toast.makeText(this@SettingsActivity, "本地模型管理功能开发中", Toast.LENGTH_SHORT).show()
                    }
                    addSwitchRow("Workspace记忆配置", "启用后支持语义检索", "workspace_memory_enabled", true)

                    addSectionTitle("服务与环境")
                    addRow("MCP工具", "添加/管理远程MCP服务") { showMcpToolsDialog() }
                    addSwitchRow("本机服务", "访问本地MCP服务", "local_service_enabled", false)
                    addRow("Alpine环境", "内置Linux终端环境") {
                        Toast.makeText(this@SettingsActivity, "Alpine环境功能开发中", Toast.LENGTH_SHORT).show()
                    }
                    addSwitchRow("后台隐藏", "从最近任务中隐藏", "background_hide", false)

                    addSectionTitle("体验与外观")
                    addRow("闹钟设置", "配置铃声和提醒") {
                        Toast.makeText(this@SettingsActivity, "闹钟设置功能开发中", Toast.LENGTH_SHORT).show()
                    }
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }
    }

    private fun LinearLayout.addSectionTitle(title: String) {
        val dp = resources.displayMetrics.density
        addView(TextView(this@SettingsActivity).apply {
            text = title
            setTextColor(Color.parseColor("#888888"))
            textSize = 12f
            setPadding(0, (dp * 8).toInt(), 0, (dp * 4).toInt())
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun LinearLayout.addRow(title: String, desc: String, onClick: () -> Unit) {
        val dp = resources.displayMetrics.density
        addView(LinearLayout(this@SettingsActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((dp * 16).toInt(), (dp * 12).toInt(), (dp * 16).toInt(), (dp * 12).toInt())
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setOnClickListener { onClick() }

            addView(TextView(this@SettingsActivity).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 14f
            })
            addView(TextView(this@SettingsActivity).apply {
                text = desc
                setTextColor(Color.parseColor("#888888"))
                textSize = 12f
                setPadding(0, (dp * 4).toInt(), 0, 0)
            })
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = (dp * 4).toInt()
        })
    }

    private fun LinearLayout.addSwitchRow(
        title: String,
        desc: String,
        prefKey: String,
        default: Boolean
    ) {
        val dp = resources.displayMetrics.density
        val row = LinearLayout(this@SettingsActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((dp * 16).toInt(), (dp * 12).toInt(), (dp * 16).toInt(), (dp * 12).toInt())
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            gravity = Gravity.CENTER_VERTICAL
        }

        val leftLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        leftLayout.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 14f
        })
        leftLayout.addView(TextView(this).apply {
            text = desc
            setTextColor(Color.parseColor("#888888"))
            textSize = 12f
            setPadding(0, (dp * 4).toInt(), 0, 0)
        })

        val switchView = Switch(this).apply {
            isChecked = getSharedPreferences("app_settings", MODE_PRIVATE).getBoolean(prefKey, default)
            setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit().putBoolean(prefKey, isChecked).apply()
            }
        }

        row.addView(leftLayout, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(switchView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = (dp * 4).toInt()
        addView(row, params)
    }

    private fun showModelProviderDialog() {
        val providers = arrayOf("OpenAI", "Claude", "Qwen", "DeepSeek", "本地模型")
        AlertDialog.Builder(this)
            .setTitle("选择模型提供商")
            .setSingleChoiceItems(providers, -1) { dialog, which ->
                getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit().putString("model_provider", providers[which]).apply()
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showSceneConfigDialog() {
        AlertDialog.Builder(this)
            .setTitle("场景模型配置")
            .setMessage("为不同场景绑定默认模型：\n\n· 聊天对话：qwen3.5-plus\n· 代码生成：qwen3.5-flash\n· 文档总结：qwen-plus\n· 图片理解：qwen3-vl-plus")
            .setPositiveButton("配置", null)
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showMcpToolsDialog() {
        AlertDialog.Builder(this)
            .setTitle("MCP工具管理")
            .setMessage("可添加、启动/停止远程MCP服务。\n\n可用工具：\n· web_search\n· calculator\n· file_manager\n· terminal")
            .setPositiveButton("添加工具", null)
            .setNegativeButton("关闭", null)
            .show()
    }
}

