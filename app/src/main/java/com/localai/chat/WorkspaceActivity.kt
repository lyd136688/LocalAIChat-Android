package com.localai.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var rvFiles: RecyclerView
    private lateinit var tvPath: TextView
    private lateinit var fileAdapter: FileListAdapter
    private var currentPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace)

        rvFiles = findViewById(R.id.rv_files)
        tvPath = findViewById(R.id.tv_path)

        fileAdapter = FileListAdapter(
            onFileClick = { file ->
                Toast.makeText(this, "打开文件: ${file.name}", Toast.LENGTH_SHORT).show()
            },
            onFolderClick = { folder -> onFolderClick(folder) }
        )
        rvFiles.layoutManager = LinearLayoutManager(this)
        rvFiles.adapter = fileAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        val btnAi = findViewById<View>(R.id.btn_ai_mode)
        val btnFile = findViewById<View>(R.id.btn_file_mode)
        btnAi.setOnClickListener {
            btnAi.setBackgroundResource(R.drawable.bg_tab_selected)
            btnFile.setBackgroundResource(0)
        }
        btnFile.setOnClickListener {
            btnFile.setBackgroundResource(R.drawable.bg_tab_selected)
            btnAi.setBackgroundResource(0)
        }

        loadFiles()
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
        }?.sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name })
            ?: emptyList()

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
            }?.sortedWith(compareByDescending<WorkspaceFileItem> { it.isDirectory }.thenBy { it.name })
                ?: emptyList()

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_file_name)
        private val tvInfo: TextView = view.findViewById(R.id.tv_file_info)

        fun bind(file: WorkspaceFileItem) {
            tvName.text = file.name
            if (file.isDirectory) {
                tvInfo.text = "文件夹"
                itemView.setOnClickListener { onFolderClick(file) }
            } else {
                val sizeKB = file.size / 1024
                val sizeText = if (sizeKB < 1024) "${sizeKB} KB" else "${sizeKB / 1024} MB"
                tvInfo.text = "$sizeText | ${dateFormat.format(Date(file.lastModified))}"
                itemView.setOnClickListener { onFileClick(file) }
            }
        }
    }
}

