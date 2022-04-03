package de.lichessbyvoice

import android.util.Log
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


object LichessService {
    private const val TAG = "LichessService"
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
    }
    lateinit var currentGameId: String
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

    data class MoveResponse(var ok: Boolean)

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

    interface BoardMoveApi { // TODO: draw offer/agree
        @FormUrlEncoded
        @POST("/api/board/game/{gameId}/move/{move}")
        suspend fun boardMove(
            @Header("Authorization") token: String,
            @Path("gameId") gameId: String,
            @Path("move") move: String,
        ) : Response<MoveResponse>
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

    suspend fun postBoardMove(move: String): Boolean {
        val boardMoveApi = RetrofitHelper.getInstance().create(BoardMoveApi::class.java)
        val result = boardMoveApi.boardMove("Bearer $theToken",
            currentGameId,
            move)
        if (result.isSuccessful) {
            Log.i(TAG, "move $move sent ok")
            return true
        }
        Log.i(TAG, "move $move rejected")
        return false
    }

    suspend fun mockChallengeAi(): GameDataEntry {
        Log.i(TAG, "mockChallengeAi()")
        return GameDataEntry(id = "JsVm8oTX")
    }
}