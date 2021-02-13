package dev.obfusk.jiten_webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    val website = "https://jiten.obfusk.dev"
    private var webview: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webview = findViewById<WebView>(R.id.webview).apply {
            settings.run {
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
            }
            webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url?.startsWith(website) != true) {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(i)
                        return true
                    }
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    CookieManager.getInstance().flush()
                }
            }
            loadUrl(website)
        }
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        val url = intent?.data?.toString()
        if (url?.startsWith(website) == true) webview?.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webview?.canGoBack() == true) webview?.goBack() else super.onBackPressed()
    }
}