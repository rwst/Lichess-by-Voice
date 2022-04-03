package de.lichessbyvoice

import android.util.Log
import de.lichessbyvoice.chess.ChessTag
import de.lichessbyvoice.chess.WordMap
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object TextFilter {
    private const val TAG = "TextFilter"
    lateinit var channel: Channel<String?>

    private fun init() {
        WordMap.init()
    }

    suspend fun start() {
        init()
        while(true) {
            val move = getPossibleMove()
//            if (move.isLegal())
            LichessService.postBoardMove(move)
        }
    }

    private suspend fun getPossibleMove() : String {
        while (true) {
            val textJson = channel.receive()
            val obj = textJson?.let { Json.decodeFromString<Map<String,String>>(it) }
            if ((obj == null
                        || !obj.containsKey("text")
                        || obj["text"] == null) || obj["text"]?.isEmpty() == true)
                continue
            val text: String = obj["text"] as String
            if (text == "huh") continue
            Log.i(TAG, "received: $text")
            val words = text.split(' ')
            val features = mutableListOf<ChessTag>()
            var moveString = ""
            words.forEach {
                 if (it in WordMap.keys()) {
                     val tag = WordMap.toTag(it)
                     features.add(tag)
                     moveString += tag.str
                 }
            }
            Log.i(TAG, "movestr: $moveString")
            if (moveString.length != 4
                || moveString[0] !in 'a'..'h'
                || moveString[2] !in 'a'..'h'
                || moveString[1] !in '1'..'8'
                || moveString[3] !in '1'..'8'
            )
                continue
            Log.i(TAG, "possible move")
            return moveString
        }
    }
}