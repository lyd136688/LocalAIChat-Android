package com.localai.chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.localai.chat.R
import com.localai.chat.WorkspaceActivity

class WorkspaceFragment : Fragment() {
    
    private lateinit var textTitle: TextView
    private lateinit var btnOpenWorkspace: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workspace, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        textTitle = view.findViewById(R.id.textTitle)
        btnOpenWorkspace = view.findViewById(R.id.btnOpenWorkspace)
        
        textTitle.text = "工作区"
        
        btnOpenWorkspace.setOnClickListener {
            startActivity(Intent(requireContext(), WorkspaceActivity::class.java))
        }
    }
}

