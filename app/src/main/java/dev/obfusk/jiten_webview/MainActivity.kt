package dev.obfusk.jiten_webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
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
                            if (it.authority == "ko-fi.com") {
                                supportDialog()
                            } else {
                                startActivity(Intent(Intent.ACTION_VIEW, it))
                            }
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

    private fun supportDialog(): Unit = AlertDialog.Builder(this).run {
        setTitle("Buy the developer(s) a cup of tea")
        setView(TextView(this@MainActivity).apply {
            val html = """
                → Jiten [Online] is also <a href="https://f-droid.org/app/dev.obfusk.jiten_webview">available on F-Droid</a>.
                <br/><br/>
                <h6><s>Computer</s>Google says "no"</h6>
                Google says we're not allowed to link to a page that allows you to buy the developer(s) a cup of tea.
                Please consider donating to one of these non-profit organisations instead:
                <br/><br/>
                <a href="https://supporters.eff.org/donate">Electronic Frontier Foundation</a>
                <br/><br/>
                <a href="https://donate.mozilla.org">Mozilla</a>
                <br/><br/>
                <a href="https://my.fsfe.org/donate">Free Software Foundation Europe</a>
                <br/><br/>
                <a href="https://www.debian.org/donations">Debian</a>
            """.trimIndent()
            text = if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(html, 0) else Html.fromHtml(html)
            movementMethod = LinkMovementMethod.getInstance()
            val f = { x: Float -> (x * resources.displayMetrics.density + 0.5f).toInt() }
            setPadding(f(30f), f(20f), f(30f), 0)
        })
        setPositiveButton(android.R.string.ok) { _, _ -> }
        show()
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