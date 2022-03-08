package de.lichessbyvoice

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class SelectGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = applicationContext.getSharedPreferences("prefs", MODE_PRIVATE)
        val token: String? = sharedPrefs.getString("accessToken", null)
        if (token == null) {
            val intent = Intent(this, AuthFailedActivity::class.java)
            startActivity(intent)
        }
        else {
            val mainButton: Button = findViewById(R.id.main_button)
            mainButton.setOnClickListener { gameView() }
            setContentView(R.layout.selectgame_activity)
        }
    }
    private fun gameView() {
        val gameUrl: Uri = Uri.parse("https://lichess.org/0YquyYuP")
        val v: View = findViewById(R.id.main)
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        v.context.startActivity(intent)
    }
}