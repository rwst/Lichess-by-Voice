package de.lichessbyvoice

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_activity)
        val txtView: TextView = findViewById(R.id.help_textView)
        val str = resources.getString(R.string.help_textview)
        txtView.text = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT)
    }
}

