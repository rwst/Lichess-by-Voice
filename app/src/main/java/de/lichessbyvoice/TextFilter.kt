package de.lichessbyvoice

import android.util.Log
import de.lichessbyvoice.chess.Move

object TextFilter {
    private const val TAG = "TextFilter"
    enum class State { FROM_COL, FROM_ROW, TO_COL, TO_ROW }
    lateinit var state : State
    suspend fun getPossibleMove(speechRecognitionService: SpeechRecognitionService) : Move? {
        state = State.FROM_COL
        for (i in 1..4) {
            val text = speechRecognitionService.channel.onReceive
            Log.i(TAG, "received: $text")
        }
        return Move()
    }
}