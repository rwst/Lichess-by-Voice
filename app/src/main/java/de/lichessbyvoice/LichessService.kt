package de.lichessbyvoice

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
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
    var aiGameParamChannel = Channel<AiGameParams>()

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
        val id: String,
        val rating: Int,
        val username: String
        )

    data class VariantType (
        val key: String,
        val name: String,
        val short: String
    )

    data class StatusType (
        val id: Int,
        val name: String
    )

    data class GameDataEntry (
        val gameId: String,
        val fullId: String,
        val color: String,
        val fen: String,
        val hasMoved: Boolean,
        val isMyTurn: Boolean,
        val lastMove: String,
        val opponent: User,
        val perf: String,
        val rated: Boolean,
        val secondsLeft: Int,
        val source: String,
        val speed: String,
        val variant: VariantType,
        val player: String,
        val turns: Int,
        val startedAtTurn: Int,
        val status: StatusType,
        val createdAt: Long,
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
        suspend fun postChallengeAi(token: String, params: AiGameParams) : Response<GameDataEntry> {
            return postChallengeAiHelper(token, params.level, params.variant, params.color)
        }

        @FormUrlEncoded
        @POST("/api/challenge/ai")
        suspend fun postChallengeAiHelper(
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
        val result = challengeAiApi.postChallengeAi("Bearer $theToken", params)
        if (result.isSuccessful) {
            Log.i(TAG, result.body().toString())
            return result.body()
        }
        Log.i(TAG, "error code: ${result.code()}, msg: ${result.message()}")
        return null
    }

    fun gameView(view: View, gameCode: String) {
        val gameUrl: Uri = Uri.parse("https://lichess.org/$gameCode")
        val intent = Intent(Intent.ACTION_VIEW, gameUrl)
        view.context.startActivity(intent)
    }
}