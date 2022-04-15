package de.lichessbyvoice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import de.lichessbyvoice.service.AppAuthService
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

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

open class AuthFailedActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setContentView(R.layout.authfailed_activity)
        val theButton: Button = findViewById(R.id.account_button)
        val service = AppAuthService.getInstance(this)
        theButton.setOnClickListener { service.linkAccount() }

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        Log.i(TAG, "resp:$resp, ex:$ex")
    }

    override fun onNewIntent(intent: Intent) {
        Log.i(TAG, "onNewIntent")
        super.onNewIntent(intent)
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    companion object {
        const val EXTRA_FAILED = "failed"
        private const val TAG = "AuthFailedActivity"
    }
}