package de.lichessbyvoice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException

class SpeechRecognitionService : RecognitionListener {
    private lateinit var model: Model
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    val channel = Channel<String>(100)

    fun setLogLevel(level: LogLevel) {
        LibVosk.setLogLevel(level)
    }

    fun initModel(context: Context) {
        StorageService.unpack(context, "model-en-us", "model",
            { model: Model ->
                this.model = model
            },
            { exception: IOException ->
                setErrorState(
                    "Failed to unpack the model" + exception.message
                )
            })
    }

    private fun setErrorState(message: String) {
        Log.e(TAG, message)
    }

    fun destroy() {
        if (speechService != null) {
            speechService!!.stop()
            speechService!!.shutdown()
        }

        speechStreamService?.stop()
    }

    fun recognizeMicrophone() {
        if (speechService != null) {
            speechService!!.stop()
            speechService = null
        } else {
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                e.message?.let { setErrorState(it) }
            }
        }
    }

    private fun pause(checked: Boolean) {
        speechService?.setPause(checked)
    }

    private suspend fun send(text: String) {
        channel.send(text)
    }

    companion object {
        private const val TAG = "SpeechRecognitionService"
    }

    override fun onPartialResult(hypothesis: String?) {
        runBlocking {
            launch {
                send(hypothesis + "\n")
            }
        }
    }

    override fun onResult(hypothesis: String?) {
        runBlocking {
            launch {
                send(hypothesis + "\n")
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        runBlocking {
            launch {
                send(hypothesis + "\n")
            }
        }
        if (speechStreamService != null) {
            speechStreamService = null
        }
    }

    override fun onError(exception: Exception?) {
        if (exception != null) {
            exception.message?.let { setErrorState(it) }
        }
    }

    override fun onTimeout() {
        Log.i(TAG, "timeout")
    }
}