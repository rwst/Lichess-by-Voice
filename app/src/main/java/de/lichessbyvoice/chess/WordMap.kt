package de.lichessbyvoice.chess

val strTag : List<Pair<ChessTag,List<String>>> = listOf(
    Pair(A_FILE, listOf("a", "hey", "ay")),
    Pair(B_FILE, listOf("b", "be", "bee", "been")),
    Pair(C_FILE, listOf("c", "cee", "see", "sea", "seen", "seek", "seas")),
    Pair(D_FILE, listOf("d", "dee", "the", "dear", "deer", "dean")),
    Pair(E_FILE, listOf("e", "ee", "he", "it", "eat")),
    Pair(F_FILE, listOf("f", "ef", "eff", "if", "i've", "have", "of")),
    Pair(G_FILE, listOf("g", "gee", "chi")),
    Pair(H_FILE, listOf("h", "aitch", "age", "etch", "hatch")),
    Pair(ROW_1, listOf("1", "one", "won")),
    Pair(ROW_2, listOf("2", "two", "to", "too")),
    Pair(ROW_3, listOf("3", "three", "tree", "free")),
    Pair(ROW_4, listOf("4", "four", "for")),
    Pair(ROW_5, listOf("5", "five", "fiver")),
    Pair(ROW_6, listOf("6", "six", "sex", "sax")),
    Pair(ROW_7, listOf("7", "seven")),
    Pair(ROW_8, listOf("8", "eight")),
)

var theMap : MutableMap<String,ChessTag> = mutableMapOf()

object WordMap {
    fun init() {
        strTag.forEach { pair ->
            pair.second.forEach {
                theMap[it] = pair.first
            }
        }
    }

    fun keys() : Set<String> { return theMap.keys }

    fun toTag(string: String) : ChessTag {
        return theMap[string] as ChessTag
    }
}