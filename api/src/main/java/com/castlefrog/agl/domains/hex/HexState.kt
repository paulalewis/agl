package com.castlefrog.agl.domains.hex

import com.castlefrog.agl.State

import java.io.Serializable

data class HexState(val boardSize: Int,
               val bitBoards: Array<ByteArray>,
               var agentTurn: Byte) : State<HexState>, Serializable {

    companion object {
        val LOCATION_EMPTY = 0
        val LOCATION_BLACK = 1
        val LOCATION_WHITE = 2

        val TURN_BLACK: Byte = 0
        val TURN_WHITE: Byte = 1
    }

    override fun copy(): HexState {
        return HexState(boardSize, bitBoards, agentTurn)
    }

    val locations: Array<ByteArray>
        get() {
            val locations = Array(boardSize) { ByteArray(boardSize) }
            var i = 0
            while (i < boardSize) {
                var j = 0
                while (j < boardSize) {
                    locations[i][j] = getLocation(i, j).toByte()
                    j += 1
                }
                i += 1
            }
            return locations
        }

    fun getLocation(x: Int, y: Int): Int {
        checkLocationArgs(x, y)
        val bitLocation = y * boardSize + x
        val byteLocation = bitLocation / java.lang.Byte.SIZE
        if (bitBoards[0][byteLocation].toInt() and (1 shl bitLocation % java.lang.Byte.SIZE) != 0) {
            return LOCATION_BLACK
        } else if (bitBoards[1][byteLocation].toInt() and (1 shl bitLocation % java.lang.Byte.SIZE) != 0) {
            return LOCATION_WHITE
        } else {
            return LOCATION_EMPTY
        }
    }

    fun isLocationEmpty(x: Int, y: Int): Boolean {
        checkLocationArgs(x, y)
        val bitLocation = y * boardSize + x
        val byteLocation = bitLocation / java.lang.Byte.SIZE
        return bitBoards[0][byteLocation].toInt() or bitBoards[1][byteLocation].toInt() and (1 shl bitLocation % java.lang.Byte.SIZE) == LOCATION_EMPTY
    }

    fun isLocationOnBoard(x: Int, y: Int): Boolean {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize
    }

    val nPieces: Int
        get() {
            var nPieces = 0
            for (i in 0..bitBoards[0].size - 1) {
                var value = bitBoards[0][i].toInt() or bitBoards[1][i].toInt()
                for (j in 0..java.lang.Byte.SIZE - 1) {
                    if (value and 1 != 0) {
                        nPieces += 1
                    }
                    value = value.ushr(1)
                }
            }
            return nPieces
        }

    fun setLocation(x: Int, y: Int, value: Int) {
        checkLocationArgs(x, y)
        val bitLocation = y * boardSize + x
        val byteLocation = bitLocation / java.lang.Byte.SIZE
        val byteShift = bitLocation % java.lang.Byte.SIZE
        if (value == LOCATION_EMPTY) {
            bitBoards[0][byteLocation] = (bitBoards[0][byteLocation].toInt() and (1 shl byteShift xor 0xff)).toByte()
            bitBoards[1][byteLocation] = (bitBoards[1][byteLocation].toInt() and (1 shl byteShift xor 0xff)).toByte()
        } else if (value == LOCATION_BLACK) {
            bitBoards[0][byteLocation] = (bitBoards[0][byteLocation].toInt() or (1 shl byteShift)).toByte()
            bitBoards[1][byteLocation] = (bitBoards[1][byteLocation].toInt() and (1 shl byteShift xor 0xff)).toByte()
        } else if (value == LOCATION_WHITE) {
            bitBoards[0][byteLocation] = (bitBoards[0][byteLocation].toInt() and (1 shl byteShift xor 0xff)).toByte()
            bitBoards[1][byteLocation] = (bitBoards[1][byteLocation].toInt() or (1 shl byteShift)).toByte()
        }
    }

    private fun checkLocationArgs(x: Int, y: Int) {
        if (!isLocationOnBoard(x, y))
            throw IllegalArgumentException("(x=$x,y=$y) out of bounds")
    }

    override fun equals(other: Any?): Boolean {
        if (other is HexState) {
            if (bitBoards.size != other.bitBoards.size) {
                return false
            }
            for (i in bitBoards.indices) {
                if (bitBoards[i].size != other.bitBoards[i].size) {
                    return false
                }
                for (j in bitBoards[i].indices) {
                    if (other.bitBoards[i][j] != bitBoards[i][j]) {
                        return false
                    }
                }
            }
            return other.boardSize == boardSize && other.agentTurn == agentTurn
        }
        return false
    }

    override fun toString(): String {
        val output = StringBuilder()
        output.append("turn = ").append(agentTurn).append("\n")
        var i = boardSize - 1
        while (i >= 0) {
            run {
                var j = i
                while (j < boardSize - 1) {
                    output.append(" ")
                    j += 1
                }
            }
            var j = 0
            while (j < boardSize) {
                val location = getLocation(j, i)
                if (location == LOCATION_BLACK) {
                    output.append("X ")
                } else if (location == LOCATION_WHITE) {
                    output.append("O ")
                } else {
                    output.append("- ")
                }
                j += 1
            }
            output.append("\n")
            i -= 1
        }
        return output.toString()
    }
}
