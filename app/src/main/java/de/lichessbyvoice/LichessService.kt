package de.lichessbyvoice

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import de.lichessbyvoice.chess.Move
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.*
import retrofit2.http.*
import retrofit2.converter.gson.GsonConverterFactory


object LichessService {
    private const val TAG = "LichessService"
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
    }
    val aiGameParamChannel = Channel<AiGameParams>()
    val newGameDataChannel = Channel<GameDataEntry?>()

    object RetrofitHelper {

        private const val baseUrl = "https://lichess.org/"
        private var logging = HttpLoggingInterceptor()

        fun getInstance(): Retrofit {
            logging.level = Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)
            return Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
    }

    data class User (
        val id: String = "",
        val rating: Int = 0,
        val username: String = ""
        )

    data class VariantType (
        val key: String = "",
        val name: String = "",
        val short: String = ""
    )

    data class StatusType (
        val id: Int = 0,
        val name: String = ""
    )

    data class GameDataEntry (
        val id: String = "",
        val gameId: String = "",
        val fullId: String = "",
        val color: String = "",
        val fen: String = "",
        val hasMoved: Boolean = false,
        val isMyTurn: Boolean = false,
        val lastMove: String = "",
        val opponent: User = User(),
        val perf: String = "",
        val rated: Boolean = false,
        val secondsLeft: Int = 0,
        val source: String = "",
        val speed: String = "",
        val variant: VariantType = VariantType(),
        val player: String = "",
        val turns: Int = 0,
        val startedAtTurn: Int = 0,
        val status: StatusType = StatusType(),
        val createdAt: Long = 0,
   )

    data class AiGameParams (
        var level: Int,
        var color: String,
        var variant: String
    )

    data class GameData(var nowPlaying: List<GameDataEntry> = emptyList())

    interface AccountPlayingApi {
        @GET("/api/account/playing")
        suspend fun getAccountPlaying(
            @Header("Authorization") token: String ) : Response<GameData>
    }

    interface ChallengeAiApi {
        @FormUrlEncoded
        @POST("/api/challenge/ai")
        suspend fun postChallengeAi(
            @Header("Authorization") token: String,
            @Field("level") level: Int,
            @Field("variant") variant: String,
            @Field("color") color: String
        ) : Response<GameDataEntry>
    }

    suspend fun getSuspendedGames(): GameData? {
        val accountPlayingApi = RetrofitHelper.getInstance().create(AccountPlayingApi::class.java)
        val result = accountPlayingApi.getAccountPlaying("Bearer $theToken")
        if (result.isSuccessful) {
            Log.i(TAG, result.body().toString())
            return result.body()
        }
        Log.i(TAG, "error code: ${result.code()}, msg: ${result.message()}")
        return null
    }

    suspend fun postChallengeAi(params: AiGameParams): GameDataEntry? {
        val challengeAiApi = RetrofitHelper.getInstance().create(ChallengeAiApi::class.java)
        val result = challengeAiApi.postChallengeAi("Bearer $theToken",
            params.level,
            params.variant,
            params.color)
        if (result.isSuccessful) {
            Log.i(TAG, result.body().toString())
            return result.body()
        }
        Log.i(TAG, "error code: ${result.code()}, msg: ${result.message()}")
        return null
    }

    suspend fun mockChallengeAi(): GameDataEntry {
        Log.i(TAG, "mockChallengeAi()")
        return GameDataEntry(id = "JsVm8oTX")
    }

    // Here we start the threads that 1. show tbe current game; 2. transcribe any speech;
    // and 3. filter the transcription for valid moves, and actually perform those moves
    // in the current game
    fun gameView(
        launcher: ActivityResultLauncher<Intent>,
        gameCode: String) {
        val gameUrl: Uri = Uri.parse("https://lichess.org/$gameCode")
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        launcher.launch(intent)
    }

    fun performMove (gameCode: String, move: Move) {
        Log.i(TAG, "game: $gameCode, move: $move")
    }
}