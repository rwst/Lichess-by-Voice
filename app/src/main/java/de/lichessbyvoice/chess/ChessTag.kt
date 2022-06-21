package de.lichessbyvoice.chess

// Copyright 2022 Ralf Stephan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

val KNIGHT = PieceTag("n")
val BISHOP = PieceTag("b")
val ROOK = PieceTag("r")
val QUEEN = PieceTag("q")

val fileTags = listOf(A_FILE, B_FILE, C_FILE, D_FILE, E_FILE, F_FILE, G_FILE, H_FILE)
val rowTags = listOf(ROW_1, ROW_2, ROW_3, ROW_4, ROW_5, ROW_6, ROW_7, ROW_8)
val promPieceTag = listOf(KNIGHT, BISHOP, ROOK, QUEEN)
