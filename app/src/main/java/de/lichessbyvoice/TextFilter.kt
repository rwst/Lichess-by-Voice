package de.lichessbyvoice

import android.util.Log
import de.lichessbyvoice.chess.Move
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.*
import kotlinx.serialization.json.*

object TextFilter {
    private const val TAG = "TextFilter"
    enum class State { FROM_COL, FROM_ROW, TO_COL, TO_ROW }
    lateinit var state : State
    lateinit var channel: Channel<String?>

    suspend fun getPossibleMove() : Move? {
        state = State.FROM_COL
        for (i in 1..4) {
            // Log.i(TAG, "receiving")
            val textJson = channel.receive()
            val obj = textJson?.let { Json.decodeFromString<Map<String,String>>(it) }
            if (obj != null) {
                if (obj.containsKey("text") && obj["text"]?.isNotEmpty() == true) {
                    Log.i(TAG, "received: ${obj["text"]}")
                }
            }
        }
        return Move()
    }
}