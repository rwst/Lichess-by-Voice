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
    private val channel = Channel<String?>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
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
        if (!recJob.isActive) {
            recJob = GlobalScope.launch {
                delay(2000L)
                SpeechRecognitionService.recognizeMicrophone(channel, context)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "onStop()")
        if (recJob.isActive) recJob.cancel()
        SpeechRecognitionService.destroy()
    }

    companion object {
        private const val TAG = "GameDisplayObserver"
    }
}