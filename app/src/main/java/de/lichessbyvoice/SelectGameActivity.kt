package de.lichessbyvoice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauthdemo.AuthStateManager


class SelectGameActivity : AppCompatActivity() {
    private lateinit var mAuthStateManager: AuthStateManager
    private lateinit var appAuthService: AppAuthService
    private var currentGameCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuthStateManager = AuthStateManager.getInstance(this)
        appAuthService = AppAuthService.getInstance(this)
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectgame_activity)
        val newGameButton: Button = findViewById(R.id.newgame_button)
        val lastGameButton: Button = findViewById(R.id.lastgame_button)
        lastGameButton.isEnabled = false
        ProgressIndicator.setShowFunc { showProgress() }
        ProgressIndicator.setHideFunc { hideProgress() }
        hideProgress()

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        if (resp == null) {
            // the activity is opened normally
            if (mAuthStateManager.isAuthorized()) {
                LichessService.setToken(mAuthStateManager.current.accessToken)
                newGameButton.setOnClickListener { newGame() }
                lastGameButton.setOnClickListener { lastGame() }
                val model: ActiveGamesViewModel by viewModels()
                model.getGames().observe(this) { games ->
                    if (games != null && games.nowPlaying.isNotEmpty()) {
                        lastGameButton.isEnabled = true
                        currentGameCode = games.nowPlaying[0].gameId
                    }
                    else
                    {
                        lastGameButton.isEnabled = false
                    }
                }
            } else {
                Log.i(TAG, "authorization missing")
                val intent = Intent(this, AuthFailedActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        else {
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

    private fun newGame() {
        val intent = Intent(this, NewGameActivity::class.java)
        startActivity(intent)
    }

    private fun lastGame() {
        currentGameCode?.let { LichessService.gameView(findViewById(R.id.main), it) }
    }

    companion object {
        private const val TAG = "SelectGameActivity"
    }
}