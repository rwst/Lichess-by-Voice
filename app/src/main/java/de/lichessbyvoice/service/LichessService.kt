package de.lichessbyvoice.service

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.launchdarkly.eventsource.EventSource
import de.lichessbyvoice.GameStreamEventHandler
import kotlinx.coroutines.channels.Channel
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.URI
import java.time.Duration

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
    }
    fun isTokenSet(): Boolean { return this::theToken.isInitialized }
    lateinit var currentGameId: String
    val aiGameParamChannel = Channel<AiGameParams>()
    val newGameDataChannel = Channel<GameDataEntry?>()

    object RetrofitHelper {

        const val baseUrl = "https://lichess.org/"
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

    abstract class ServerSideEvent {}

    data class GameStateEvent (
        val type: String = "gameState",
        val moves: String = "",
        val status: String = "",
        val winner: String = ""
    ) : ServerSideEvent()

    data class ChatLineEvent (
        val type: String = "chatLine",
        val username: String = "",
        val text: String = "",
        val room: String = ""
    ) : ServerSideEvent()

    data class GameState (
        val type: String = "gameState",
        val moves: String = "",
        val wtime: Int = 0,
        val btime: Int = 0,
        val winc: Int = 0,
        val binc: Int = 0,
        val status: String = "started"
    )

    data class GameFullEvent (
        val type: String = "gameFull",
        val id: String = "",
        val rated: Boolean = false,
        val variant: VariantType = VariantType(),
        val perf: String = "",
        val source: String = "",
        val speed: String = "",
        val createdAt: Long = 0,
        val white: User = User(),
        val black: User = User(),
        val initialFen: String = "",
        val state: GameState = GameState()
    ) : ServerSideEvent()

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBoardGameStream(gameId: String) {
        val eventHandler = GameStreamEventHandler()
        val url = RetrofitHelper.baseUrl + String.format("/api/board/game/stream/{gameId}");
        val headers: Headers = Headers.headersOf("Authorization", "Bearer $theToken")
        val builder = EventSource.Builder(eventHandler, URI.create(url))
            .reconnectTime(Duration.ofMillis(3000))
            .headers(headers)

        val eventSource = builder.build()
        eventSource.start()
    }

    fun mockChallengeAi(): GameDataEntry {
        Log.i(TAG, "mockChallengeAi()")
        return GameDataEntry(id = "9qigB6bGA5u9")
    }
}