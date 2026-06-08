package com.localai.chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.localai.chat.adapters.FileListAdapter
import com.localai.chat.databinding.ActivityWorkspaceBinding
import com.localai.chat.data.models.FileItem
import java.io.File

class WorkspaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkspaceBinding
    private lateinit var fileAdapter: FileListAdapter
    private var currentPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkspaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileAdapter = FileListAdapter(
            onFileClick = { file ->
                Toast.makeText(this, "打开文件: ${file.name}", Toast.LENGTH_SHORT).show()
            },
            onFolderClick = { folder -> onFolderClick(folder) }
        )
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = fileAdapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnAiMode.setOnClickListener {
            binding.btnAiMode.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnFileMode.setBackgroundResource(0)
        }

        binding.btnFileMode.setOnClickListener {
            binding.btnFileMode.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnAiMode.setBackgroundResource(0)
        }

        loadFiles()
    }

    private fun loadFiles() {
        val workspaceDir = File(getExternalFilesDir(null), "workspace")
        if (!workspaceDir.exists()) workspaceDir.mkdirs()

        currentPath = workspaceDir.absolutePath
        binding.tvPath.text = "/workspace"

        val files = workspaceDir.listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified()
            )
        }?.sortedByDescending { it.isDirectory } ?: emptyList()

        fileAdapter.setData(files)
    }

    private fun onFolderClick(folder: FileItem) {
        val dir = File(folder.path)
        if (dir.isDirectory) {
            currentPath = folder.path
            binding.tvPath.text = folder.path.substringAfterLast("workspace").ifEmpty { "/workspace" }

            val files = dir.listFiles()?.map { file ->
                FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = file.lastModified()
                )
            }?.sortedByDescending { it.isDirectory } ?: emptyList()

            fileAdapter.setData(files)
        }
    }
}

