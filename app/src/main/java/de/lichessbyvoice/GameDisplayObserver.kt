package de.lichessbyvoice

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import de.lichessbyvoice.service.SpeechRecognitionService
import kotlinx.coroutines.*

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

class GameDisplayObserver(private val context: Context) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

         GlobalScope.launch {
            delay(2000L)
            SpeechRecognitionService.recognizeMicrophone(context)
         }
        TheApplication.mainScope.launch {
            delay(2000L)
            de.lichessbyvoice.chess.TextFilter.start()
         }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.i(TAG, "onResume()")
        SpeechRecognitionService.pause(false)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.i(TAG, "onPause()")
        SpeechRecognitionService.pause(true)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i(TAG, "onStart()")
        SpeechRecognitionService.pause(false)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "onStop()")
        SpeechRecognitionService.pause(true)
    }

    companion object {
        private const val TAG = "GameDisplayObserver"
    }
}