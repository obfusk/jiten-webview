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
    val server = "jiten.obfusk.dev"
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
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                    Uri.parse(url).let {
                        if (it.host != server) {
                            startActivity(Intent(Intent.ACTION_VIEW, it))
                            true
                        } else {
                            false
                        }
                    }

                override fun onPageFinished(view: WebView?, url: String?) {
                    CookieManager.getInstance().flush()
                }
            }
            loadUrl("https://$server")
        }
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.toString()?.let { url ->
            Uri.parse(url).let { if (it.host == server) webview?.loadUrl(url) }
        }
    }

    override fun onBackPressed() {
        webview?.let { if (it.canGoBack()) { it.goBack(); return } }
        super.onBackPressed()
    }
}