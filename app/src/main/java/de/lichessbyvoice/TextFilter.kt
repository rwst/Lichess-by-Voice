package de.lichessbyvoice

import de.lichessbyvoice.chess.Move

object TextFilter {
    enum class State { FROM_COL, FROM_ROW, TO_COL, TO_ROW }
    lateinit var state : State
    suspend fun getPossibleMove(speechRecognitionService: SpeechRecognitionService) : Move? {
        state = State.FROM_COL
        while (true) {
            val text = speechRecognitionService.channel.onReceive
            return Move()
        }
    }
}