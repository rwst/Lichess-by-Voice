package de.lichessbyvoice

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GameDisplayObserver(private val context: Context) : DefaultLifecycleObserver {
    private lateinit var recJob : Job
    private lateinit var filterJob : Job

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        val channel = Channel<String?>()
        TextFilter.channel = channel

        runBlocking {
            recJob = launch {
                SpeechRecognitionService.recognizeMicrophone(channel, context)
            }
        }
        runBlocking {
            filterJob = launch {
                TextFilter.start()
            }
        }

    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        SpeechRecognitionService.destroy()
        recJob.cancel()
        filterJob.cancel()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        SpeechRecognitionService.pause(false)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        SpeechRecognitionService.pause(true)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        SpeechRecognitionService.pause(false)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        SpeechRecognitionService.pause(true)
    }
}