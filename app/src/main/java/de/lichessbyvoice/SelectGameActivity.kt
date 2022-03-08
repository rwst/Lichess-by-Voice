package de.lichessbyvoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauthdemo.AuthStateManager
import net.openid.appauthdemo.Configuration

class SelectGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mAuthStateManager = AuthStateManager.getInstance(this)
        val mConfiguration = Configuration.getInstance(this)

        if (mAuthStateManager.current.isAuthorized) {
            val mainButton: Button = findViewById(R.id.main_button)
            mainButton.setOnClickListener { gameView() }
            setContentView(R.layout.selectgame_activity)
        }
        else {
            val intent = Intent(this, AuthFailedActivity::class.java)
            startActivity(intent)
        }
    }
    private fun gameView() {
        val gameUrl: Uri = Uri.parse("https://lichess.org/0YquyYuP")
        val v: View = findViewById(R.id.main)
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        v.context.startActivity(intent)
    }
}