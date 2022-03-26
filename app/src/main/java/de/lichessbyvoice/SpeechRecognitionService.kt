package de.lichessbyvoice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private val modelChannel = Channel<Model>()
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    val channel = Channel<String>(100)

    fun setLogLevel(level: LogLevel) {
        LibVosk.setLogLevel(level)
    }

    private fun initModel(context: Context) {
        StorageService.unpack(context, "model-en-us", "model",
            { model: Model ->
                runBlocking {
                    modelChannel.send(model)
                }
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

    private suspend fun recognizeMicrophone() {
        if (speechService != null) {
            speechService!!.stop()
            speechService = null
        } else {
            try {
                val model = modelChannel.receive()
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

        /* Used to handle permission request */
        const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
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

    fun start(
        activity: AppCompatActivity,
        launcher: ActivityResultLauncher<Intent>,
        gameCode: String
    ) {
        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck = ContextCompat.checkSelfPermission(
            activity.applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
            return
        }

        initModel(activity)
        runBlocking {
            LichessService.gameView(launcher, gameCode)
            recognizeMicrophone()
            launch {
                while(true) {
                    val move = TextFilter.getPossibleMove(this@SpeechRecognitionService) ?: break
                    if (move.isLegal())
                        LichessService.performMove(gameCode, move)
                }
            }
        }
    }
}