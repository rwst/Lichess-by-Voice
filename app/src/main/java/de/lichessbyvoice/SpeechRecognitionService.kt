package de.lichessbyvoice

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.lichessbyvoice.chess.ChessGrammar
import de.lichessbyvoice.vosk.ErrorListener
import de.lichessbyvoice.vosk.SpeechService
import kotlinx.coroutines.channels.Channel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.IOException

object SpeechRecognitionService : ErrorListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val channel = Channel<String?>()

    init {
        TextFilter.channel = channel
    }

    private fun unpackModel(context: Context, sourcePath: String, targetPath: String) {
        try {
            val outputPath = de.lichessbyvoice.vosk.StorageService.sync(context, sourcePath, targetPath)
            Log.i(TAG, "unpackmodel() outputPath: $outputPath")
            model = Model(outputPath)
        } catch (e: IOException) {
            setErrorState(
                "Failed to unpack the model: ${e.message}, ${e.toString()}")
        }
        // TODO: make this a pre-deploy test
        if (model == null) throw Exception("null model")
    }

    private fun initModel(context: Context) {
        Log.i(TAG, "unpacking model")
        unpackModel(context, "models/model-en-us", "model")
        ChessGrammar.init()
    }

    private fun setErrorState(message: String) {
        Log.e(TAG, message)
    }

    fun destroy() {
        if (speechService != null) {
            speechService!!.shutdown()
        }
    }

    suspend fun recognizeMicrophone(context: Context) {
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
        flags: Int,
        gameCode: String,
        gameColor: String,
    ) {
        LichessService.currentGameId = gameCode
        if (model == null) {
            initModel(activity)  // TODO: move this to app start
            if (model == null) return // TODO: error msg to user, bailout
            }

        val gameUrl = "https://lichess.org/$gameCode/$gameColor"
        val intent = Intent(activity, GameDisplayActivity::class.java)
        intent.flags = flags
        intent.putExtra("uri", gameUrl)
        activity.startActivity(intent)
    }

    private const val TAG = "SpeechRecognitionServic"
}