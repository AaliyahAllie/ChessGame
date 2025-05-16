package com.example.chessgame

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var board: Array<Array<Button>>
    private lateinit var pieceBoard: Array<Array<String>>
    private var selectedCell: Pair<Int, Int>? = null
    private var isWhiteTurn = true
    private var gameOver = false
    private val highlightedCells = mutableListOf<Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val grid = findViewById<GridLayout>(R.id.chessGrid)
        board = Array(8) { row ->
            Array(8) { col ->
                val button = Button(this)
                button.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                }
                button.setBackgroundColor(
                    if ((row + col) % 2 == 0) Color.parseColor("#C8E6C9")
                    else Color.parseColor("#4CAF50")
                )
                grid.addView(button)
                button.setOnClickListener {
                    onCellClick(row, col)
                }
                button
            }
        }

        pieceBoard = Array(8) { Array(8) { "" } }
        setupInitialPieces()
        drawBoard()
    }

    private fun isCorrectTurn(piece: String): Boolean {
        return (isWhiteTurn && isWhitePiece(piece)) || (!isWhiteTurn && isBlackPiece(piece))
    }

    private fun onCellClick(row: Int, col: Int) {
        if (gameOver) return

        val piece = pieceBoard[row][col]

        if (selectedCell == null) {
            if (piece.isNotEmpty() && isCorrectTurn(piece)) {
                selectedCell = Pair(row, col)
                showValidMoves(piece, row, col)
            }
        } else {
            val (fromRow, fromCol) = selectedCell!!
            val movingPiece = pieceBoard[fromRow][fromCol]

            if (isValidMove(movingPiece, fromRow, fromCol, row, col)) {
                val capturedPiece = pieceBoard[row][col]
                pieceBoard[row][col] = movingPiece
                pieceBoard[fromRow][fromCol] = ""

                if (capturedPiece == "♔") {
                    Toast.makeText(this, "Black wins! 🏁", Toast.LENGTH_LONG).show()
                    gameOver = true
                } else if (capturedPiece == "♚") {
                    Toast.makeText(this, "White wins! 🏁", Toast.LENGTH_LONG).show()
                    gameOver = true
                }

                isWhiteTurn = !isWhiteTurn
                if (!gameOver) {
                    Toast.makeText(
                        this,
                        if (isWhiteTurn) "White's turn ⬜" else "Black's turn ⬛",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            selectedCell = null
            highlightedCells.clear()
            drawBoard()
        }
    }

    private fun showValidMoves(piece: String, fromRow: Int, fromCol: Int) {
        highlightedCells.clear()

        for (toRow in 0..7) {
            for (toCol in 0..7) {
                if (isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
                    highlightedCells.add(Pair(toRow, toCol))
                }
            }
        }

        drawBoard()

        board[fromRow][fromCol].setBackgroundColor(Color.YELLOW)
        for ((r, c) in highlightedCells) {
            board[r][c].setBackgroundColor(Color.parseColor("#FFEB3B")) // Yellow highlight
        }
    }

    private fun drawBoard() {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = pieceBoard[row][col]
                board[row][col].text = piece

                // Set default square color
                val baseColor = if ((row + col) % 2 == 0)
                    Color.parseColor("#C8E6C9")
                else
                    Color.parseColor("#4CAF50")

                board[row][col].setBackgroundColor(baseColor)

                // If in highlighted cells, show possible move
                if (highlightedCells.contains(Pair(row, col))) {
                    board[row][col].setBackgroundColor(Color.parseColor("#FFEB3B"))
                }

                // Set text color
                board[row][col].setTextColor(
                    when {
                        isWhitePiece(piece) -> Color.WHITE
                        isBlackPiece(piece) -> Color.BLACK
                        else -> Color.TRANSPARENT
                    }
                )
            }
        }
    }

    private fun setupInitialPieces() {
        val whitePieces = arrayOf("♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖")
        val blackPieces = arrayOf("♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜")

        for (i in 0..7) {
            pieceBoard[0][i] = blackPieces[i]
            pieceBoard[1][i] = "♟"
            pieceBoard[6][i] = "♙"
            pieceBoard[7][i] = whitePieces[i]
        }
    }

    private fun isValidMove(piece: String, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        if (fromRow == toRow && fromCol == toCol) return false

        val dx = toCol - fromCol
        val dy = toRow - fromRow
        val target = pieceBoard[toRow][toCol]

        if (isWhitePiece(piece) && isWhitePiece(target)) return false
        if (isBlackPiece(piece) && isBlackPiece(target)) return false

        return when (piece) {
            "♙" -> dy == -1 && dx == 0 && target == "" ||
                    dy == -2 && fromRow == 6 && dx == 0 && pieceBoard[fromRow - 1][fromCol] == "" && target == "" ||
                    dy == -1 && Math.abs(dx) == 1 && isBlackPiece(target)
            "♟" -> dy == 1 && dx == 0 && target == "" ||
                    dy == 2 && fromRow == 1 && dx == 0 && pieceBoard[fromRow + 1][fromCol] == "" && target == "" ||
                    dy == 1 && Math.abs(dx) == 1 && isWhitePiece(target)
            "♖", "♜" -> isClearPath(fromRow, fromCol, toRow, toCol) && (dx == 0 || dy == 0)
            "♗", "♝" -> isClearPath(fromRow, fromCol, toRow, toCol) && Math.abs(dx) == Math.abs(dy)
            "♕", "♛" -> isClearPath(fromRow, fromCol, toRow, toCol) &&
                    (dx == 0 || dy == 0 || Math.abs(dx) == Math.abs(dy))
            "♘", "♞" -> (Math.abs(dx) == 2 && Math.abs(dy) == 1) || (Math.abs(dx) == 1 && Math.abs(dy) == 2)
            "♔", "♚" -> Math.abs(dx) <= 1 && Math.abs(dy) <= 1
            else -> false
        }
    }

    private fun isClearPath(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val dx = Integer.signum(toCol - fromCol)
        val dy = Integer.signum(toRow - fromRow)
        var x = fromCol + dx
        var y = fromRow + dy

        while (x != toCol || y != toRow) {
            if (pieceBoard[y][x].isNotEmpty()) return false
            x += dx
            y += dy
        }
        return true
    }

    private fun isWhitePiece(piece: String): Boolean = piece in arrayOf("♙", "♖", "♘", "♗", "♕", "♔")
    private fun isBlackPiece(piece: String): Boolean = piece in arrayOf("♟", "♜", "♞", "♝", "♛", "♚")
}
