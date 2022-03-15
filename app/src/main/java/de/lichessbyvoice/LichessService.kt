package de.lichessbyvoice

import android.content.Context
import java.lang.RuntimeException

class LichessService private constructor(context: Context) {
    private lateinit var theToken: String
    fun setToken(token: String?) {
        theToken = token ?: throw RuntimeException("null token")
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

    fun getLastSuspendedGameCode(): String? {
        return null
    }

    companion object : SingletonHolder<LichessService, Context>(::LichessService) {
        private const val TAG = "LichessService"
    }
}