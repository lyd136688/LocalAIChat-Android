package com.localai.chat

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val fragmentMap = mutableMapOf<String, Fragment>()

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
                R.id.nav_service -> { switchFragment("service"); true }
                R.id.nav_market -> { switchFragment("market"); true }
                R.id.nav_chat -> { switchFragment("chat"); true }
                R.id.nav_workspace -> { switchFragment("workspace"); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            switchFragment("chat")
        }

        checkPermissions()
    }

    private fun switchFragment(tag: String) {
        val fragment = getOrCreateFragment(tag) ?: return
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commitAllowingStateLoss()
    }

    private fun getOrCreateFragment(tag: String): Fragment? {
        val cached = fragmentMap[tag]
        if (cached != null) return cached

        val fqn = when (tag) {
            "service" -> "com.localai.chat.fragments.ServiceFragment"
            "market" -> "com.localai.chat.fragments.MarketFragment"
            "chat" -> "com.localai.chat.fragments.ChatFragment"
            "workspace" -> "com.localai.chat.fragments.WorkspaceFragment"
            else -> null
        } ?: return SimplePlaceholderFragment.newInstance(tag)

        return try {
            val cls = Class.forName(fqn)
            val instance = cls.getDeclaredConstructor().newInstance() as Fragment
            fragmentMap[tag] = instance
            instance
        } catch (e: Throwable) {
            SimplePlaceholderFragment.newInstance(tag)
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showStoragePermissionDialog()
                return
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要存储权限")
            .setMessage("请授予所有文件访问权限，以便保存模型和记忆数据。")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
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

class SimplePlaceholderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val title = arguments?.getString(ARG_TAG, "页面") ?: "页面"

        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val titleView = TextView(context).apply {
            text = title
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }

        val hintView = TextView(context).apply {
            text = "此页面正在开发中"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#888888"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        root.addView(titleView)
        root.addView(hintView)
        return root
    }

    companion object {
        private const val ARG_TAG = "tag"

        fun newInstance(tag: String): SimplePlaceholderFragment {
            return SimplePlaceholderFragment().apply {
                arguments = Bundle().apply { putString(ARG_TAG, tag) }
            }
        }
    }
}

