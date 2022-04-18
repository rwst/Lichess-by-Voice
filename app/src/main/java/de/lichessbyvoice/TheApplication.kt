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

package de.lichessbyvoice

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@Suppress("unused")
class TheApplication : Application() {
    override fun onCreate() {
        Log.i(TAG, "onCreate")
        super.onCreate()
        mainScope = MainScope()
//        val intent = Intent(this, SelectGameActivity::class.java)
//        startActivity(intent)
    }

    companion object {
        private const val TAG = "TheApplication"
        lateinit var mainScope: CoroutineScope
    }
}