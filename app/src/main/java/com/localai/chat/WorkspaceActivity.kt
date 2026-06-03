package com.localai.chat

import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.localai.chat.adapters.FileListAdapter
import com.localai.chat.data.models.FileItem
import java.io.File

class WorkspaceActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var btnToggleMode: ImageButton
    private lateinit var textPath: TextView
    private lateinit var recyclerFiles: RecyclerView
    private lateinit var fileAdapter: FileListAdapter
    
    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var isAIMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace)
        
        initViews()
        setupRecyclerView()
        loadFiles(currentPath)
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        textPath = findViewById(R.id.textPath)
        recyclerFiles = findViewById(R.id.recyclerFiles)
        
        btnBack.setOnClickListener {
            val parent = File(currentPath).parentFile
            if (parent != null) {
                currentPath = parent.absolutePath
                loadFiles(currentPath)
            } else {
                finish()
            }
        }
        
        btnToggleMode.setOnClickListener {
            isAIMode = !isAIMode
            Toast.makeText(this, if (isAIMode) "AI模式" else "文件模式", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        fileAdapter = FileListAdapter { fileItem ->
            if (fileItem.isDirectory) {
                currentPath = fileItem.path
                loadFiles(currentPath)
            } else {
                Toast.makeText(this, "打开文件: ${fileItem.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerFiles.layoutManager = LinearLayoutManager(this)
        recyclerFiles.adapter = fileAdapter
    }
    
    private fun loadFiles(path: String) {
        textPath.text = path
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return
        
        val files = dir.listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified()
            )
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
        
        fileAdapter.submitList(files)
    }
}

