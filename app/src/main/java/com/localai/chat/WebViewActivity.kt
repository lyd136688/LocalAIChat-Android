package com.localai.chat

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace)
        
        initViews()
        setupWebView()
        
        val url = intent.getStringExtra("url") ?: "https://www.google.com"
        webView.loadUrl(url)
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnToggleMode)
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
        
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { webView.reload() }
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) android.view.View.GONE else android.view.View.VISIBLE
            }
        }
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
