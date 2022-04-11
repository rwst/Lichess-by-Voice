package de.lichessbyvoice

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle

class GameDisplayActivity  : AppCompatActivity() {
    private lateinit var webView : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(GameDisplayObserver(this))

        val uri : String? = intent.extras?.getString("uri")
        Log.i(TAG, "Got $uri")

        webView = WebView(this)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = false
        webView.settings.setSupportZoom(false)
        webView.settings.setJavaScriptEnabled(true);
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE;
        webView.settings.domStorageEnabled = true
        webView.setBackgroundColor(Color.TRANSPARENT)
        setContentView(webView)
        if (uri != null) {
            webView.loadUrl(uri)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop()")

        webView.setFocusable(true);
        webView.removeAllViews();
        webView.clearHistory();
        webView.destroy()
    }

    companion object {
        private const val TAG = "GameDisplayActivity"
    }
}
