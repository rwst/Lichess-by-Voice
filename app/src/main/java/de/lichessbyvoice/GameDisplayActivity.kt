package de.lichessbyvoice

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import de.lichessbyvoice.service.LichessService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

// Copyright 2022 Ralf Stephan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class GameDisplayActivity  : AppCompatActivity() {
    private lateinit var webView : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(GameDisplayObserver(this))

        val uri : String? = intent.extras?.getString("uri")
        val gameId : String? = intent.extras?.getString("gameId")
        Log.i(TAG, "Got $uri $gameId")

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
            if (gameId != null) {
                runBlocking {
                    CoroutineScope(Dispatchers.IO).launch {
                        val channel = LichessService.StreamConnection.open(gameId)
                        if (channel != null) {
                            launch {
                                LichessService.StreamConnection.readStateStream()
                            }
                            launch {
                                actOnStateStream(channel)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun actOnStateStream(channel: Channel<LichessService.GameState?>) {
        while (!channel.isClosedForReceive) {
            val state : LichessService.GameState? = channel.receive()
            if (state == null) {
                Log.i(TAG, "null GameState")
            }
            else {
                Log.i(TAG, "status: ${state!!.status}")
                if (state!!.status == "resign") break
            }
        }
        onStop()
        finish()
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

        webView.isFocusable = true;
        webView.removeAllViews();
        webView.clearHistory();
        webView.destroy()
    }

    companion object {
        private const val TAG = "GameDisplayActivity"
    }
}
