package com.localai.chat

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.just.agentweb.AgentWeb

class WebViewActivity : AppCompatActivity() {
    private lateinit var agentWeb: AgentWeb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(findViewById(android.R.id.content), android.widget.LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .createAgentWeb()
            .ready()
            .go("https://www.baidu.com")

        agentWeb.webCreator.webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean = false
        }
    }

    override fun onBackPressed() {
        if (!agentWeb.back()) {
            super.onBackPressed()
        }
    }
}
