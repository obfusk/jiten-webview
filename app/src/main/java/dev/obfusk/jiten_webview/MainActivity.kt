package dev.obfusk.jiten_webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    val defaultServer = "jiten.obfusk.dev"
    private var prefs: SharedPreferences? = null
    private var server: String? = null
    private var webview: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getPreferences(Context.MODE_PRIVATE)
        server = prefs?.getString("server", null)
        if (server == null) chooseServer() else setupWebView()
    }

    private fun chooseServer(): Unit = AlertDialog.Builder(this).run {
        val input = EditText(this@MainActivity).apply { setText("https://$defaultServer") }
        setTitle("Choose a Jiten server")
        setMessage("(must support https)")
        setView(input)
        setPositiveButton(android.R.string.ok) { _, _ ->
            Uri.parse(input.text.toString().let {
                if (it.toLowerCase(Locale.getDefault()).startsWith("https://")) it else "https://$it"
            }).let {
                if (!it.authority.isNullOrEmpty()) {
                    server = it.authority
                    prefs?.edit()?.run { putString("server", server); apply() }
                    setupWebView()
                } else {
                    chooseServer()
                }
            }
        }
        show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
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
                        if (it.authority != server) {
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
            Uri.parse(url).let {
                if (it.authority == defaultServer) {
                    webview?.loadUrl(it.buildUpon().run {
                        scheme("https"); authority(server); build().toString()
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        webview?.let { if (it.canGoBack()) { it.goBack(); return } }
        super.onBackPressed()
    }
}