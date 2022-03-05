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
        setContentView(R.layout.activity_main)
        val theButton: Button = findViewById(R.id.button)
        theButton.setOnClickListener { gameView() }
    }
    private fun gameView() {
        val gameUrl: Uri = Uri.parse("https://lichess.org/0YquyYuP")
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        val v: View = findViewById(R.id.main)
        v.context.startActivity(intent)
    }
}