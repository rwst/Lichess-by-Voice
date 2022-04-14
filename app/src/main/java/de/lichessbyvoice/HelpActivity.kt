package de.lichessbyvoice

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
        val strHelp = resources.getString(R.string.help_textview)
        txtView.text = Html.fromHtml(strHelp, Html.FROM_HTML_MODE_COMPACT)
        val aboutView: TextView = findViewById(R.id.about_textView)
        val strAbout = resources.getString(R.string.about_textview)
        try {
            val pInfo: PackageInfo =
                packageManager.getPackageInfo(this.packageName, 0)
            val version: String = pInfo.versionName
            aboutView.text = strAbout.replace("XXX_VERSION_XXX", version)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
}

