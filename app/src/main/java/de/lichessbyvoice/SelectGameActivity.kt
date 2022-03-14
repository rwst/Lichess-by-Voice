package de.lichessbyvoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauthdemo.AuthStateManager


class SelectGameActivity : AppCompatActivity() {
    private lateinit var mAuthStateManager: AuthStateManager
    private lateinit var appAuthService: AppAuthService
    private lateinit var lichess: LichessService

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuthStateManager = AuthStateManager.getInstance(this)
        appAuthService = AppAuthService.getInstance(this)
        lichess = LichessService.getInstance(this)
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectgame_activity)

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        if (resp == null) {
            // the activity is opened normally
            if (mAuthStateManager.isAuthorized()) {
                lichess.setToken(mAuthStateManager.current.accessToken)
                val newGameButton: Button = findViewById(R.id.newgame_button)
                newGameButton.setOnClickListener { newGame() }
                val lastGameButton: Button = findViewById(R.id.lastgame_button)
                lastGameButton.setOnClickListener { lastGame() }
                val code: String? = lichess.getLastSuspendedGameCode()
                lastGameButton.isEnabled = (code != null)
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

    private fun newGame() {
    }

    private fun lastGame() {
    }

    private fun gameView(gameCode: String) {
        val gameUrl: Uri = Uri.parse("https://lichess.org/$gameCode")
        val v: View = findViewById(R.id.main)
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        v.context.startActivity(intent)
    }
    companion object {
        private const val TAG = "SelectGameActivity"
    }
}