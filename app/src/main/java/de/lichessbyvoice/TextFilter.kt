package de.lichessbyvoice

import android.util.Log
import de.lichessbyvoice.chess.Move
import kotlinx.coroutines.channels.Channel

object TextFilter {
    private const val TAG = "TextFilter"
    enum class State { FROM_COL, FROM_ROW, TO_COL, TO_ROW }
    lateinit var state : State
    lateinit var channel: Channel<String?>

    suspend fun getPossibleMove() : Move? {
        state = State.FROM_COL
        for (i in 1..4) {
            Log.i(TAG, "receiving")
            val text = channel.receive()
            Log.i(TAG, "received: $text")
        }
        return Move()
    }
}