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
        // normal move squares: a1, b7 ....
        fileTags.forEach { it1 ->
            rowTags.forEach { it2 ->
                strTagMap[it1]?.forEach { it3 ->
                    strTagMap[it2]?.forEach { it4 ->
                        phraseList.add("$it3 $it4")
                    }
                }
            }
        }
        // promotion squares: e8Q, h1N ....
        fileTags.forEach { it1 ->
            promPieceTag.forEach { it2 ->
                strTagMap[it1]?.forEach { it3 ->
                    strTagMap[ROW_1]?.forEach { it4 ->
                        strTagMap[it2]?.forEach { it5 ->
                            phraseList.add("$it3 $it4 $it5")
                        }
                    }
                    strTagMap[ROW_8]?.forEach { it4 ->
                        strTagMap[it2]?.forEach { it5 ->
                            phraseList.add("$it3 $it4 $it5")
                        }
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