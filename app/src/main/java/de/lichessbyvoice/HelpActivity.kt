package de.lichessbyvoice

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

// Copyright 2022 Ralf Stephan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class HelpActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_activity)
        val txtView: TextView = findViewById(R.id.help_textView)
        val strHelp = resources.getString(R.string.help_textview)
        txtView.text = Html.fromHtml(strHelp, Html.FROM_HTML_MODE_COMPACT)
        txtView.movementMethod = ScrollingMovementMethod()
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

