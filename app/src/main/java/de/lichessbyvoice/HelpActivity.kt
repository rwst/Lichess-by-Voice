package de.lichessbyvoice

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val txtView = findViewById(R.id.help_textView)
//        val str = find(R.string.help_textview)
//        txtView.setText(Html.fromHtml(
        setContentView(R.layout.help_activity)
    }
}

