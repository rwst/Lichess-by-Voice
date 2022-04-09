package de.lichessbyvoice.chess

import android.util.Log
import com.google.gson.Gson

object ChessGrammar {
    private var phraseList = mutableListOf<String>()

    fun init() {
        if (phraseList.isNotEmpty()) return
        val strTagMap = mutableMapOf<ChessTag,List<String>>()
        strTag.forEach {
            strTagMap[it.first] = it.second
        }
        filetags.forEach { it1 ->
            rowtags.forEach { it2 ->
                strTagMap[it1]?.forEach { it3 ->
                    strTagMap[it2]?.forEach { it4 ->
                        phraseList.add("$it3 $it4")
                    }
                }
            }
        }
        Log.i(TAG, "grammar with ${phraseList.size} phrases initialized")
    }

    fun jsonString() : String {
        return Gson().toJson(phraseList)
    }

    private const val TAG = "ChessGrammar"
}