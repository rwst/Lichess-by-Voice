package de.lichessbyvoice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.lichessbyvoice.chess.ChessGrammar
import de.lichessbyvoice.vosk.ErrorListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import de.lichessbyvoice.vosk.SpeechService
import kotlinx.coroutines.Job
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException

object SpeechRecognitionService : ErrorListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null

    fun setLogLevel(level: LogLevel) {
        LibVosk.setLogLevel(level)
    }

    private fun unpackModel(context: Context, sourcePath: String, targetPath: String) {
        try {
            val outputPath = StorageService.sync(context, sourcePath, targetPath)
            model = Model(outputPath)
        } catch (e: IOException) {
            setErrorState(
                "Failed to unpack the model" + e.message)
        }
    }

    private fun initModel(context: Context) {
        Log.i(TAG, "unpacking model")
        unpackModel(context, "model-en-us", "model")
        ChessGrammar.init()
    }

    private fun setErrorState(message: String) {
        Log.e(TAG, message)
    }

    fun destroy() {
        if (speechService != null) {
            speechService!!.shutdown()
        }

        //speechStreamService?.stop()
    }

    suspend fun recognizeMicrophone(channel: Channel<String?>, context: Context) {
        Log.i(TAG,"recognizeMicrophone")
        if (speechService == null) {
            Log.i(TAG,"start listening")
            val rec = Recognizer(model, 16000.0f, ChessGrammar.jsonString())
            speechService = SpeechService(context, rec, 16000.0f)
        }
        speechService!!.startListening(this@SpeechRecognitionService, channel)
        Log.i(TAG,"started listening")
    }

    fun pause(checked: Boolean) {
        speechService?.setPause(checked)
    }

    override suspend fun onError(exception: Exception?) {
        if (exception != null) {
            exception.message?.let { setErrorState(it) }
        }
    }

    override fun onTimeout() {
        Log.i(TAG, "timeout")
    }

    fun start(
        activity: AppCompatActivity,
        gameCode: String,
        gameColor: String,
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

        LichessService.currentGameId = gameCode
        if (model == null) {
            initModel(activity)  // TODO: move this to app start
            if (model == null) return // TODO: error msg to user, bailout
            }

        val gameUrl = "https://lichess.org/$gameCode/$gameColor"
        val intent = Intent(activity, GameDisplayActivity::class.java)
        intent.putExtra("uri", gameUrl)
        activity.startActivity(intent)
    }

    private const val TAG = "SpeechRecognitionService"

    /* Used to handle permission request */
    const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
}