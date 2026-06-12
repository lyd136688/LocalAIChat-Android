package com.localai.chat

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.localai.chat.fragments.ChatFragment
import com.localai.chat.fragments.MarketFragment
import com.localai.chat.fragments.ServiceFragment
import com.localai.chat.fragments.WorkspaceFragment

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
        var fragment = fm.findFragmentByTag(tag)

        if (fragment == null) {
            fragment = when (tag) {
                tagService -> ServiceFragment()
                tagMarket -> MarketFragment()
                tagChat -> ChatFragment()
                tagWorkspace -> WorkspaceFragment()
                else -> ChatFragment()
            }
        }

        fm.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commitAllowingStateLoss()
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

