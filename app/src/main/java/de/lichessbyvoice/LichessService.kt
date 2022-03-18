package de.lichessbyvoice

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header


object LichessService {
    private const val TAG = "LichessService"
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
    }

    object RetrofitHelper {

        private const val baseUrl = "https://lichess.org/"
        // private const val baseUrl = "https://ptsv2.com/"
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
    )

    data class GameData(var nowPlaying: List<GameDataEntry> = emptyList()) {
//        val nowPlaying: List<GameDataEntry> = TODO()
    }

    interface AccountPlayingApi {
        @GET("/api/account/playing")
//        @GET("t/yrq59-1647616889/post")
        suspend fun getAccountPlaying(@Header("Authorization") token: String ) : Response<GameData>
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
}