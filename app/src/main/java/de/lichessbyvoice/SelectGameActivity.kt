package de.lichessbyvoice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.lichessbyvoice.service.AppAuthService
import de.lichessbyvoice.service.LichessService
import de.lichessbyvoice.service.SpeechRecognitionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauthdemo.AuthStateManager
import java.io.IOException

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

class SelectGameActivity : FinishableActivity() {
    private lateinit var mAuthStateManager: AuthStateManager
    private lateinit var appAuthService: AppAuthService
    private var currentGameCode: String? = null
    private var currentGameSide: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuthStateManager = AuthStateManager.getInstance(this)
        appAuthService = AppAuthService.getInstance(this)
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectgame_activity)
        val newGameButton: Button = findViewById(R.id.newgame_button)
        val lastGameButton: Button = findViewById(R.id.lastgame_button)
        lastGameButton.isEnabled = (currentGameCode != null)
        val helpButton: Button = findViewById(R.id.help_button)
        hideProgress()

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        Log.i(TAG, "resp:$resp, ex:$ex")
        if (resp == null || LichessService.isTokenSet()) {
            // the activity is opened normally
            if (mAuthStateManager.isAuthorized()) {
                LichessService.setToken(mAuthStateManager.current.accessToken)
                newGameButton.setOnClickListener { newGame() }
                lastGameButton.setOnClickListener { lastGame() }
                helpButton.setOnClickListener { help() }
                setCurrentGame()
            } else {
                Log.i(TAG, "authorization missing")
                val intent = Intent(this, AuthFailedActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        } else {
            // the activity is opened from AppAuth after OAuth2 first step
            mAuthStateManager.updateAfterAuthorization(resp, ex)
            appAuthService.performTokenRequest(
                resp.createTokenExchangeRequest()
            ) { theResp, theEx ->
                mAuthStateManager.updateAfterTokenResponse(theResp, theEx)
                if (theResp != null) {
                    // exchange succeeded
                    Log.i(TAG, "token exchange succeeded: $theResp")
                    val intent = Intent(this, SelectGameActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    Log.i(TAG, "token exchange failed: $theEx")
                }
            }
        }

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck = ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun setCurrentGame() {
        Log.i(TAG, "setCurrentGame()")
        val lastGameButton: Button = findViewById(R.id.lastgame_button)
        runBlocking {
            val channel = Channel<Any?>()
            showProgress()
            CoroutineScope(Dispatchers.IO).launch {
                val obj: Any? = try {
                    LichessService.getSuspendedGames()
                } catch (e : Throwable) {
                    e
                }
                channel.send(obj)
            }
            launch {
                when (val obj = channel.receive()) {
                    null -> lastGameButton.isEnabled = false
                    is LichessService.GameData -> {
                        if (obj.nowPlaying.isNotEmpty()) {
                            obj.nowPlaying.forEach {
                                Log.i(TAG, "currentGame: ${it.gameId} ismyturn: ${it.fen}")
                            }
                            lastGameButton.isEnabled = true
                            currentGameCode = obj.nowPlaying[0].gameId
                            currentGameSide = obj.nowPlaying[0].color
                        }
                    }
                    is IOException -> {
                        val newFragment = AlertDialogFragment(
                            this@SelectGameActivity,
                            R.string.no_connection_alert,
                            R.string.no_connection_alert_text,
                            R.string.exit_app_button
                        )
                        newFragment.show(supportFragmentManager, null)
                    }
                }
                hideProgress()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!LichessService.isTokenSet()) {
            finish()
            return
        }
        setCurrentGame()
        Log.i(TAG, "onResume()")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showProgress() {
        val spinner: ProgressBar = findViewById(R.id.selectGame_progressBar)
        spinner.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        val spinner: ProgressBar = findViewById(R.id.selectGame_progressBar)
        spinner.visibility = View.GONE
    }

    private fun help() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }

    private fun newGame() {
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
    }

    private fun lastGame() {
        if (currentGameCode != null) {
            SpeechRecognitionService.start(
                this,
                Intent.FLAG_ACTIVITY_NEW_TASK,
                currentGameCode!!,
                currentGameSide!!
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                val newFragment = AlertDialogFragment(
                    this,
                    R.string.mic_permission_alert,
                    R.string.mic_permission_alert_text,
                    R.string.exit_app_button
                )
                newFragment.show(supportFragmentManager, null)
            }
        }
    }

    override fun doFinish() {
        finish()
    }

    companion object {
        private const val TAG = "SelectGameActivity"
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }
}