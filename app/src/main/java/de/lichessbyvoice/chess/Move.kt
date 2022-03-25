package de.lichessbyvoice.chess

class Move {
    private val from = "x2"
    private val to = "x4"

    fun isLegal() : Boolean { return true }
    override fun toString() : String { return "$from-$to"}
}
