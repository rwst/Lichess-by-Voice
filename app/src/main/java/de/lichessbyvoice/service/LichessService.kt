package de.lichessbyvoice.service

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


// Copyright 2022 Ralf Stephan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

object LichessService {
    private const val TAG = "LichessService"
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
//        Log.i(TAG, "token: $theToken")
    }
    fun isTokenSet(): Boolean { return this::theToken.isInitialized }
    lateinit var currentGameId: String
    val aiGameParamChannel = Channel<AiGameParams>()
    val newGameDataChannel = Channel<GameDataEntry?>()

    // TODO: reproduce and test no internet; catch java.net.SocketTimeoutException

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

    class GameState (
        val type: String = "gameState",
        val moves: String = "",
        val wtime: Int = 0,
        val btime: Int = 0,
        val winc: Int = 0,
        val binc: Int = 0,
        val status: String = "",
        val winner: String = ""
    )

    data class GameData(var nowPlaying: List<GameDataEntry> = emptyList())

    data class MoveResponse(var ok: Boolean)

    interface AccountPlayingApi {
        @GET("/api/account/playing")
        suspend fun getAccountPlaying(
            @Header("Authorization") token: String
        ) : Response<GameData>
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
            @Field("offeringDraw") draw: Boolean
        ) : Response<MoveResponse>
    }

    object StreamConnection {
        private lateinit var conn : HttpsURLConnection
        private lateinit var channel: Channel<GameState?>
        private const val TAG = "StreamConnection"
        fun open(gameId: String) : Channel<GameState?>? {
            val url = URL("https://lichess.org/api/board/game/stream/${gameId}")
            conn = url.openConnection() as HttpsURLConnection
            conn.setRequestProperty ("Authorization", "Bearer $theToken")
            conn.requestMethod = "GET"
            conn.readTimeout = 60 * 1000
            conn.connectTimeout = 60 * 1000
            conn.connect()
            val responseCode = conn.responseCode
            if (responseCode != 200) {
                Log.i(TAG, "response: ${conn.responseCode} ${conn.responseMessage}")
                Log.i(TAG, "connection: $conn ${conn.url}")
                return null
            }

            val encoding =
                if (conn.contentEncoding == null) "UTF-8" else conn.contentEncoding
            Log.i(TAG, "encoding: $encoding")
            channel = Channel()
            return channel
        }

        suspend fun readStateStream() {
            val scan = Scanner(conn.inputStream)
            val gson = Gson()

            while(scan.hasNextLine()) {
                val line = scan.nextLine()
                if (line != null && line.contains("gameState")) {
                    val obj : GameState = gson.fromJson(line, GameState::class.java)
                    channel.send(obj)
                }
            }
        }
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
            move,
        false)
        if (result.isSuccessful) {
            Log.i(TAG, "move $move sent ok")
            return true
        }
        Log.i(TAG, "move $move rejected")
        return false
    }

    fun mockChallengeAi(): GameDataEntry {
        Log.i(TAG, "mockChallengeAi()")
        return GameDataEntry(id = "9qigB6bGA5u9")
    }
}