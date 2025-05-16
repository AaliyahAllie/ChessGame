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
                    if ((row + col) % 2 == 0) Color.parseColor("#C8E6C9") // Light green
                    else Color.parseColor("#4CAF50") // Dark green
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

    private fun onCellClick(row: Int, col: Int) {
        if (gameOver) return

        val piece = pieceBoard[row][col]

        if (selectedCell == null) {
            if (piece.isNotEmpty() && isCorrectTurn(piece)) {
                selectedCell = Pair(row, col)
                board[row][col].setBackgroundColor(Color.YELLOW)
            }
        } else {
            val (fromRow, fromCol) = selectedCell!!
            val movingPiece = pieceBoard[fromRow][fromCol]

            if (isValidMove(movingPiece, fromRow, fromCol, row, col)) {
                val capturedPiece = pieceBoard[row][col]
                pieceBoard[row][col] = movingPiece
                pieceBoard[fromRow][fromCol] = ""

                // Check if a King was captured
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
            drawBoard()
        }
    }

    private fun isCorrectTurn(piece: String): Boolean {
        return (isWhitePiece(piece) && isWhiteTurn) || (isBlackPiece(piece) && !isWhiteTurn)
    }

    private fun drawBoard() {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = pieceBoard[row][col]
                board[row][col].text = piece

                // Set square color
                board[row][col].setBackgroundColor(
                    if ((row + col) % 2 == 0)
                        Color.parseColor("#C8E6C9") // light green
                    else
                        Color.parseColor("#4CAF50") // green
                )

                // Set text color based on piece color
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

        // Black
        for (i in 0..7) {
            pieceBoard[0][i] = blackPieces[i]
            pieceBoard[1][i] = "♟"
        }

        // White
        for (i in 0..7) {
            pieceBoard[6][i] = "♙"
            pieceBoard[7][i] = whitePieces[i]
        }
    }

    private fun isValidMove(piece: String, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val dx = toCol - fromCol
        val dy = toRow - fromRow
        val target = pieceBoard[toRow][toCol]

        if (isWhitePiece(piece) && isWhitePiece(target)) return false
        if (isBlackPiece(piece) && isBlackPiece(target)) return false

        return when (piece) {
            "♙" -> dy == -1 && dx == 0 && target == "" || dy == -2 && fromRow == 6 && dx == 0 && target == "" ||
                    dy == -1 && Math.abs(dx) == 1 && isBlackPiece(target)
            "♟" -> dy == 1 && dx == 0 && target == "" || dy == 2 && fromRow == 1 && dx == 0 && target == "" ||
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
