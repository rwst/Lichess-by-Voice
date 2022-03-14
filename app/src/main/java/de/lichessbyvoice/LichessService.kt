package de.lichessbyvoice

import android.content.Context
import java.lang.RuntimeException
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class LichessService private constructor(context: Context) {
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
    }

    @Serializable
    data class user (
        val id: String,
        val rating: Int,
        val username: String
        )

    @Serializable
    data class variantType (
        val key: String,
        val name: String
    )

    @Serializable
    data class gameData (
        val gameId: String,
        val fullId: String,
        val color: String,
        val fen: String,
        val hasMoved: Boolean,
        val isMyTurn: Boolean,
        val lastMove: String,
        val opponent: user,
        val perf: String,
        val rated: Boolean,
        val secondsLeft: Int,
        val source: String,
        val speed: String,
        val variant: variantType,
    )

    fun getLastSuspendedGameCode(): String? {
        return null
    }

    companion object : SingletonHolder<LichessService, Context>(::LichessService) {
        private const val TAG = "LichessService"
    }
}