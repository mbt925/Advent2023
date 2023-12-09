import kotlin.math.max
import kotlin.math.min

private typealias Matrix = List<String>

fun main() {

    data class EngineNumber(
        val value: Int,
        val row: Int,
        val startCol: Int,
        val endCol: Int,
    ) {
        val colRange = IntRange(startCol, endCol)

        fun isAdjacent(row: Int, col: Int): Boolean {
            return (row in IntRange(this.row - 1, this.row + 1) && col in IntRange(startCol - 1, endCol + 1))
        }
    }

    val onlyDigitsRegex = Regex("\\d+")

    fun String.toSparseEngineNumbers(row: Int): List<EngineNumber> {
        val allNumbers = onlyDigitsRegex.findAll(this)
        return allNumbers.map {
            EngineNumber(
                value = it.value.toInt(),
                row = row,
                startCol = it.range.first,
                endCol = it.range.last,
            )
        }.toList()
    }

    fun Matrix.isPartNumber(number: EngineNumber): Boolean {
        val startRow = max(0, number.row - 1)
        val endRow = min(size - 1, number.row + 1)
        val startCol = max(0, number.startCol - 1)
        val endCol = min(this[0].lastIndex, number.endCol + 1)
        for (row in startRow..endRow) {
            for (col in startCol..endCol) {
                if (row == number.row && col in number.colRange) continue
                if (this[row][col] != '.') return true
            }
        }
        return false
    }

    fun Matrix.findPartNumbers(numbers: List<EngineNumber>): List<EngineNumber> {
        return buildList {
            numbers.forEach { number ->
                if (this@findPartNumbers.isPartNumber(number)) add(number)
            }
        }
    }

    fun Matrix.gearRatioIfHasExactlyTwoAdjacentParts(
        row: Int,
        col: Int,
        numbersByRow: Map<Int, List<EngineNumber>>
    ): Int {
        val lastRow = lastIndex
        val adjacentNumbers = buildList {
            if (row > 0) numbersByRow[row - 1]?.let { add(it) }
            numbersByRow[row]?.let { add(it) }
            if (row < lastRow) numbersByRow[row + 1]?.let { add(it) }
        }
            .flatten()
            .filter { it.isAdjacent(row, col) }

        return if (adjacentNumbers.size == 2) adjacentNumbers[0].value * adjacentNumbers[1].value
        else 0
    }

    fun part1(input: Matrix): Int {
        val engineNumbers = input.mapIndexed { row, line ->
            line.toSparseEngineNumbers(row)
        }.flatten()

        return input.findPartNumbers(engineNumbers).fold(0) { acc, part ->
            acc + part.value
        }
    }

    fun part2(input: Matrix): Int {
        val engineNumbersByRow = input.mapIndexed { row, line ->
            line.toSparseEngineNumbers(row)
        }
            .filterNot { it.isEmpty() }
            .associateBy { it.first().row }

        val regex = Regex("\\*")
        return input.foldIndexed(0) { row, acc, line ->
            var ratioSum = 0
            val gears = regex.findAll(line)
            val iterator = gears.iterator()
            while (iterator.hasNext()) {
                val gear = iterator.next()
                ratioSum += input.gearRatioIfHasExactlyTwoAdjacentParts(row, gear.range.first, engineNumbersByRow)
            }
            acc + ratioSum
        }
    }

    val testInput1 = readInput("Day03_test")
    check(part1(testInput1) == 4361)
    check(part2(testInput1) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
