package de.lichessbyvoice

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*

class GameDisplayObserver(private val context: Context) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

         GlobalScope.launch {
            delay(2000L)
            SpeechRecognitionService.recognizeMicrophone(context)
         }
         SelectGameActivity.mainScope.launch {
            delay(2000L)
            TextFilter.start()
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