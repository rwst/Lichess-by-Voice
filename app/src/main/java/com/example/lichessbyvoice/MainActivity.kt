package com.example.lichessbyvoice

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = applicationContext.getSharedPreferences("prefs", MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        val mainButton: Button = findViewById(R.id.main_button)
        val token: String? = sharedPrefs.getString("accessToken", null)
        if (token == null) {
            mainButton.isEnabled = false
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
        else {
            mainButton.setOnClickListener { gameView() }
        }
        val accountButton: Button = findViewById(R.id.account_main_button)
        accountButton.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
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