import TiltDirection.*

enum class TiltDirection { North, West, South, East }

fun main() {

    fun MutableList<String>.moveRocksHorizontally(
        roundedRocks: List<Int>,
        blockStart: Int,
        row: Int,
        targetEmptyCell: (Int, Int) -> Int
    ) {
        roundedRocks.forEachIndexed { index, rockCol ->
            val targetCell = targetEmptyCell(index, blockStart)
            this[row] = this[row]
                .replaceRange(rockCol, rockCol + 1, ".")
                .replaceRange(targetCell, targetCell + 1, "O")
        }
    }

    fun MutableList<String>.moveRocksVertically(
        roundedRocks: List<Int>,
        blockStart: Int,
        col: Int,
        targetEmptyCell: (Int, Int) -> Int
    ) {
        roundedRocks.forEachIndexed { index, rockRow ->
            val targetCell = targetEmptyCell(index, blockStart)
            this[rockRow] = this[rockRow].replaceRange(col, col + 1, ".")
            this[targetCell] = this[targetCell].replaceRange(col, col + 1, "O")
        }
    }

    fun MutableList<String>.tiltWest(): MutableList<String> {
        val rows = size
        val cols = this[0].length
        val roundedRocks = mutableListOf<Int>()
        val targetEmptyCellTranslator: (Int, Int) -> Int = { index, block -> block + index + 1 }

        for (row in 0..<rows) {
            var blockStart = -1
            for (col in 0..<cols) {
                val cell = this[row][col]
                when (cell) {
                    '#' -> {
                        moveRocksHorizontally(roundedRocks, blockStart, row, targetEmptyCellTranslator)
                        roundedRocks.clear()
                        blockStart = col
                    }

                    'O' -> roundedRocks.add(col)
                }
            }
            moveRocksHorizontally(roundedRocks, blockStart, row, targetEmptyCellTranslator)
            roundedRocks.clear()
        }
        return this
    }

    fun MutableList<String>.tiltEast(): MutableList<String> {
        val rows = size
        val cols = this[0].length
        val roundedRocks = mutableListOf<Int>()
        val targetEmptyCellTranslator: (Int, Int) -> Int = { index, block -> block - index - 1 }

        for (row in 0..<rows) {
            var blockStart = cols
            for (col in cols - 1 downTo 0) {
                val cell = this[row][col]
                when (cell) {
                    '#' -> {
                        moveRocksHorizontally(roundedRocks, blockStart, row, targetEmptyCellTranslator)
                        roundedRocks.clear()
                        blockStart = col
                    }

                    'O' -> roundedRocks.add(col)
                }
            }
            moveRocksHorizontally(roundedRocks, blockStart, row, targetEmptyCellTranslator)
            roundedRocks.clear()
        }
        return this
    }

    fun MutableList<String>.tiltNorth(): MutableList<String> {
        val rows = size
        val cols = this[0].length
        val roundedRocks = mutableListOf<Int>()
        val targetEmptyCellTranslator: (Int, Int) -> Int = { index, block -> block + index + 1 }

        for (col in 0..<cols) {
            var blockStart = -1
            for (row in 0..<rows) {
                val cell = this[row][col]
                when (cell) {
                    '#' -> {
                        moveRocksVertically(roundedRocks, blockStart, col, targetEmptyCellTranslator)
                        roundedRocks.clear()
                        blockStart = row
                    }

                    'O' -> roundedRocks.add(row)
                }
            }
            moveRocksVertically(roundedRocks, blockStart, col, targetEmptyCellTranslator)
            roundedRocks.clear()
        }
        return this
    }

    fun MutableList<String>.tiltSouth(): MutableList<String> {
        val rows = size
        val cols = this[0].length
        val roundedRocks = mutableListOf<Int>()
        val targetEmptyCellTranslator: (Int, Int) -> Int = { index, block -> block - index - 1 }

        for (col in 0..<cols) {
            var blockStart = rows
            for (row in rows - 1 downTo 0) {
                val cell = this[row][col]
                when (cell) {
                    '#' -> {
                        moveRocksVertically(roundedRocks, blockStart, col, targetEmptyCellTranslator)
                        roundedRocks.clear()
                        blockStart = row
                    }

                    'O' -> roundedRocks.add(row)
                }
            }
            moveRocksVertically(roundedRocks, blockStart, col, targetEmptyCellTranslator)
            roundedRocks.clear()
        }
        return this
    }

    fun MutableList<String>.tilt(direction: TiltDirection): MutableList<String> {
        return when (direction) {
            North -> tiltNorth()
            West -> tiltWest()
            South -> tiltSouth()
            East -> tiltEast()
        }
    }

    fun List<String>.load(): Long {
        return foldIndexed(0L) { index, acc, line ->
            val roundedRocks = line.count { it == 'O' }
            acc + roundedRocks * (size - index)
        }
    }

    fun part1(input: List<String>): Long {
        return input.toMutableList().tilt(North).load()
    }

    fun part2(input: List<String>): Long {
        val mutableInput = input.toMutableList()
        val cache = mutableMapOf<String, Int>()
        val cache2 = mutableMapOf<Int, String>()
        var firstSeenIndex = -1
        var secondSeenIndex = -1

        val iterations = 1000000000
        for (i in 1..iterations) {
            val fullCycle = mutableInput.tilt(North).tilt(West).tilt(South).tilt(East)
            val flat = fullCycle.joinToString()
            if (cache.containsKey(flat)) {
                firstSeenIndex = cache[flat]!!
                secondSeenIndex = i
                break
            } else {
                cache[flat] = i
                cache2[i] = flat
            }
        }

        val period = secondSeenIndex - firstSeenIndex
        val targetIndex = (iterations - firstSeenIndex) % period + firstSeenIndex
        val target = cache2[targetIndex]!!.split(",").toMutableList()
        return target.load()
    }

    val testInput1 = readInput("Day14_test")
    check(part1(testInput1) == 136L)
    check(part2(testInput1) == 64L)

    val input = readInput("Day14")
    part1(input).println()
    part2(input).println()
}
