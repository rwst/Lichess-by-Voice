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

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuthStateManager = AuthStateManager.getInstance(this)
        appAuthService = AppAuthService.getInstance(this)
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectgame_activity)

        if (intent.action == "android.intent.action.MAIN") {
            if (mAuthStateManager.isAuthorized()) {
                val mainButton: Button = findViewById(R.id.main_button)
                mainButton.setOnClickListener { gameView() }
            } else {
                Log.i(TAG, "authorization missing")
                val intent = Intent(this, AuthFailedActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        else {
            val resp = AuthorizationResponse.fromIntent(intent)
            val ex = AuthorizationException.fromIntent(intent)
            if (resp != null) {
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
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun gameView() {
        val gameUrl: Uri = Uri.parse("https://lichess.org/0YquyYuP")
        val v: View = findViewById(R.id.main)
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        v.context.startActivity(intent)
    }
    companion object {
        private const val TAG = "SelectGameActivity"
    }
}