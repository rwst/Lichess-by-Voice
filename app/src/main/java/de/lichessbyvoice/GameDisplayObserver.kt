package de.lichessbyvoice

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameDisplayObserver(private val context: Context) : DefaultLifecycleObserver {
    private lateinit var recJob : Job
    private lateinit var filterJob : Job

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        val channel = Channel<String?>()
        TextFilter.channel = channel


        recJob = GlobalScope.launch {
            delay(2000L)
            SpeechRecognitionService.recognizeMicrophone(channel, context)
        }
        filterJob = owner.lifecycle.coroutineScope.launch {
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
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "onStop()")
    }

    companion object {
        private const val TAG = "GameDisplayObserver"
    }
}