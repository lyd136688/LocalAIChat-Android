package com.localai.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.localai.chat.fragments.ChatFragment
import com.localai.chat.fragments.MarketFragment
import com.localai.chat.fragments.ServiceFragment
import com.localai.chat.fragments.WorkspaceFragment

class MainActivity : AppCompatActivity() {

    private val chatFragment = ChatFragment()
    private val serviceFragment = ServiceFragment()
    private val marketFragment = MarketFragment()
    private val workspaceFragment = WorkspaceFragment()

    private var activeFragment: Fragment = chatFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, chatFragment, "chat")
                .add(R.id.fragment_container, serviceFragment, "service").hide(serviceFragment)
                .add(R.id.fragment_container, marketFragment, "market").hide(marketFragment)
                .add(R.id.fragment_container, workspaceFragment, "workspace").hide(workspaceFragment)
                .commit()
            activeFragment = chatFragment
        }

        navView.selectedItemId = R.id.nav_chat

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_service -> switchFragment(serviceFragment)
                R.id.nav_market -> switchFragment(marketFragment)
                R.id.nav_chat -> switchFragment(chatFragment)
                R.id.nav_workspace -> switchFragment(workspaceFragment)
            }
            true
        }
    }

    private fun switchFragment(target: Fragment) {
        if (target != activeFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }
}

