package de.lichessbyvoice.chess

abstract class ChessTag(val str : String)

class FileTag(str: String) : ChessTag(str)

val A_FILE = FileTag("a")
val B_FILE = FileTag("b")
val C_FILE = FileTag("c")
val D_FILE = FileTag("d")
val E_FILE = FileTag("e")
val F_FILE = FileTag("f")
val G_FILE = FileTag("g")
val H_FILE = FileTag("h")

class RowTag(str: String) : ChessTag(str)

val ROW_1 = RowTag("1")
val ROW_2 = RowTag("2")
val ROW_3 = RowTag("3")
val ROW_4 = RowTag("4")
val ROW_5 = RowTag("5")
val ROW_6 = RowTag("6")
val ROW_7 = RowTag("7")
val ROW_8 = RowTag("8")

class PieceTag(str: String) : ChessTag(str)

val KNIGHT = PieceTag("N")
val BISHOP = PieceTag("B")
val ROOK = PieceTag("R")
val QUEEN = PieceTag("Q")

val fileTags = listOf(A_FILE, B_FILE, C_FILE, D_FILE, E_FILE, F_FILE, G_FILE, H_FILE)
val rowTags = listOf(ROW_1, ROW_2, ROW_3, ROW_4, ROW_5, ROW_6, ROW_7, ROW_8)
val promPieceTag = listOf(KNIGHT, BISHOP, ROOK, QUEEN)
