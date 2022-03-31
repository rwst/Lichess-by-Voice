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

class HybridTag(str : String) : ChessTag(str)

val ROW_4_D_FILE = HybridTag("4d")
val ROW_6_D_FILE = HybridTag("6d")
val ROW_7_D_FILE = HybridTag("7d")
val ROW_8_D_FILE = HybridTag("8d")
