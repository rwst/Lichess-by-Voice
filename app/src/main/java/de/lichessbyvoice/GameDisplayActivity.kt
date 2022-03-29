package de.lichessbyvoice

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class GameDisplayActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri : String? = intent.extras?.getString("uri")
        Log.i(TAG, "Got $uri")

        val webView = WebView(this)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        setContentView(webView)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = false
        webView.settings.setSupportZoom(false)
        webView.settings.setJavaScriptEnabled(true);  // TODO
        webView.setBackgroundColor(Color.TRANSPARENT)
        if (uri != null) {
            webView.loadUrl("https://lichess.org/JsVm8oTX")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognitionService.destroy()
    }

    final val TAG = "GameDisplayActivity"
}
