package com.localai.chat

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.localai.chat.fragments.ChatFragment
import com.localai.chat.fragments.MarketFragment
import com.localai.chat.fragments.ServiceFragment
import com.localai.chat.fragments.WorkspaceFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnSettings: ImageButton
    private lateinit var btnMemory: ImageButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupBottomNavigation()
        setupTopBar()
        
        if (savedInstanceState == null) {
            loadFragment(ServiceFragment())
        }
    }
    
    private fun initViews() {
        bottomNav = findViewById(R.id.bottomNav)
        btnSettings = findViewById(R.id.btnSettings)
        btnMemory = findViewById(R.id.btnMemory)
    }
    
    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_service -> {
                    loadFragment(ServiceFragment())
                    true
                }
                R.id.nav_market -> {
                    loadFragment(MarketFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    true
                }
                R.id.nav_workspace -> {
                    loadFragment(WorkspaceFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupTopBar() {
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        btnMemory.setOnClickListener {
            startActivity(Intent(this, MemoryCenterActivity::class.java))
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
