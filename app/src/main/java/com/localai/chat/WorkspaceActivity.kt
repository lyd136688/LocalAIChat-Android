package com.localai.chat

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkspaceActivity : AppCompatActivity() {

    private lateinit var fileAdapter: FileListAdapter
    private lateinit var rvFiles: RecyclerView
    private lateinit var tvPath: TextView
    private var currentPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = buildUI()
        setContentView(root)

        fileAdapter = FileListAdapter(
            onFileClick = { file ->
                Toast.makeText(this, "打开文件: ${file.name}", Toast.LENGTH_SHORT).show()
            },
            onFolderClick = { folder -> onFolderClick(folder) }
        )
        rvFiles.layoutManager = LinearLayoutManager(this)
        rvFiles.adapter = fileAdapter

        loadFiles()
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
            addView(LinearLayout(this@WorkspaceActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(pad12, pad12, pad12, pad12)
                setBackgroundColor(Color.parseColor("#1E1E1E"))
                gravity = Gravity.CENTER_VERTICAL

                addView(ImageView(this@WorkspaceActivity).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    setColorFilter(Color.WHITE)
                    setOnClickListener { finish() }
                }, LinearLayout.LayoutParams(pad16 * 2, pad16 * 2))

                addView(TextView(this@WorkspaceActivity).apply {
                    text = "Workspace"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setPadding(pad12, 0, 0, 0)
                }, LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            // 路径显示
            tvPath = TextView(this@WorkspaceActivity).apply {
                text = "/workspace"
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 12f
                setPadding(pad16, pad12, pad16, pad12)
            }
            addView(tvPath, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            // 文件列表
            rvFiles = RecyclerView(this@WorkspaceActivity).apply {
                layoutManager = LinearLayoutManager(this@WorkspaceActivity)
                setBackgroundColor(Color.parseColor("#121212"))
            }
            addView(rvFiles, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }
    }

    private fun loadFiles() {
        val workspaceDir = File(getExternalFilesDir(null), "workspace")
        if (!workspaceDir.exists()) workspaceDir.mkdirs()

        currentPath = workspaceDir.absolutePath
        tvPath.text = "/workspace"

        val files = workspaceDir.listFiles()?.map { file ->
            WorkspaceFileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified()
            )
        }?.sortedWith(
            compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name }
        ) ?: emptyList()

        fileAdapter.setData(files)
    }

    private fun onFolderClick(folder: WorkspaceFileItem) {
        val dir = File(folder.path)
        if (dir.isDirectory) {
            currentPath = folder.path
            tvPath.text = folder.path.substringAfterLast("workspace").ifEmpty { "/workspace" }

            val files = dir.listFiles()?.map { file ->
                WorkspaceFileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = file.lastModified()
                )
            }?.sortedWith(
                compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name }
            ) ?: emptyList()

            fileAdapter.setData(files)
        }
    }
}

data class WorkspaceFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

class FileListAdapter(
    private val onFileClick: (WorkspaceFileItem) -> Unit,
    private val onFolderClick: (WorkspaceFileItem) -> Unit
) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private var files: List<WorkspaceFileItem> = emptyList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setData(newFiles: List<WorkspaceFileItem>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val dp = parent.resources.displayMetrics.density
        val row = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((dp * 12).toInt(), (dp * 12).toInt(), (dp * 12).toInt(), (dp * 12).toInt())
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (dp * 4).toInt()
            }
        }

        val icon = ImageView(parent.context).apply {
            setImageResource(android.R.drawable.ic_menu_save)
            setColorFilter(Color.parseColor("#CCCCCC"))
            id = android.R.id.icon
        }
        val infoLayout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            id = android.R.id.text1
        }
        val nameTv = TextView(parent.context).apply {
            id = android.R.id.text2
            setTextColor(Color.WHITE)
            textSize = 14f
        }
        val infoTv = TextView(parent.context).apply {
            id = android.R.id.title
            setTextColor(Color.parseColor("#888888"))
            textSize = 12f
            setPadding(0, (dp * 4).toInt(), 0, 0)
        }
        infoLayout.addView(nameTv)
        infoLayout.addView(infoTv)

        row.addView(icon, LinearLayout.LayoutParams((dp * 24).toInt(), (dp * 24).toInt()))
        row.addView(infoLayout, LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = (dp * 12).toInt()
        })

        return ViewHolder(row, nameTv, infoTv, icon)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    inner class ViewHolder(
        view: View,
        private val tvName: TextView,
        private val tvInfo: TextView,
        private val icon: ImageView
    ) : RecyclerView.ViewHolder(view) {

        fun bind(file: WorkspaceFileItem) {
            tvName.text = file.name
            if (file.isDirectory) {
                icon.setImageResource(android.R.drawable.ic_menu_sort_by_size)
                tvInfo.text = "文件夹"
                itemView.setOnClickListener { onFolderClick(file) }
            } else {
                icon.setImageResource(android.R.drawable.ic_menu_save)
                val sizeKB = file.size / 1024
                val sizeText = if (sizeKB < 1024) "${sizeKB} KB" else "${sizeKB / 1024} MB"
                tvInfo.text = "$sizeText | ${dateFormat.format(Date(file.lastModified))}"
                itemView.setOnClickListener { onFileClick(file) }
            }
        }
    }
}

