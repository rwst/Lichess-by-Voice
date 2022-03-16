package de.lichessbyvoice

import android.util.Log
import java.lang.RuntimeException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.converter.gson.GsonConverterFactory

object LichessService {
    private const val TAG = "LichessService"
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
    }

    object RetrofitHelper {

        private const val baseUrl = "https://lichess.org/api/"

        fun getInstance(): Retrofit {
            return Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
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

    data class GameData (
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

    interface AccountPlayingApi {
        @GET("/account/playing")
        suspend fun getAccountPlaying() : Response<List<GameData>>
    }

    suspend fun getSuspendedGames(): List<GameData>? {
        val accountPlayingApi = RetrofitHelper.getInstance().create(AccountPlayingApi::class.java)
        val result = accountPlayingApi.getAccountPlaying()
        if (result.isSuccessful) {
            Log.i(TAG, result.body().toString())
            return result.body()
        }
        Log.i(TAG, result.errorBody().toString())
        return null
    }
}