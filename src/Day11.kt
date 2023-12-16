import java.util.LinkedList
import kotlin.math.abs

fun main() {

    data class Coord(
        val row: Int,
        val col: Int,
    ) {
        operator fun plus(c: Coord) = Coord(row + c.row, col + c.col)

        fun isValid(rows: Int, cols: Int) = !(row < 0 || col < 0 || row >= rows || col >= cols)
    }

    data class Node(
        val coord: Coord,
        val colsCount: Int = 1,
        val rowsCount: Int = 1,
    ) {
        fun apply(diff: Coord): Int {
            val newCoord = coord + diff
            val newColDiff = abs(newCoord.col - coord.col)
            val newRowDiff = abs(newCoord.row - coord.row)

            val isValid = newColDiff < colsCount && newRowDiff < rowsCount
            return if (isValid) {
                if (newColDiff > 0) colsCount else rowsCount
            }
            else 1
        }
    }

    data class Space(
        val nodes: List<Node>,
        val rows: Int,
        val cols: Int,
    ) {
        val map = nodes.associateBy { it.coord }
    }

    val directions = listOf(Coord(-1, 0), Coord(1, 0), Coord(0, -1), Coord(0, 1))

    fun List<String>.expandEmptyRowsAndCols(count: Int): Space {
        val rows = size
        val cols = this[0].length

        val emptyColsSet = mutableSetOf<Int>()
        for (col in this[0].lastIndex downTo 0) {
            var foundGalaxy = false
            for (row in indices) {
                if (this[row][col] == '#') {
                    foundGalaxy = true
                    break
                }
            }
            if (!foundGalaxy) emptyColsSet.add(col)
        }
        val emptyRow = ".".repeat(cols)
        val emptyRowsSet = mutableSetOf<Int>()
        for (row in indices) if (this[row] == emptyRow) emptyRowsSet.add(row)

        val nodes = buildList {
            for (row in this@expandEmptyRowsAndCols.indices) {
                val line = this@expandEmptyRowsAndCols[row]
                val rowsCount = if (row in emptyRowsSet) count else 1
                for (col in line.indices) {
                    val coord = Coord(row, col)
                    val colsCount = if (col in emptyColsSet) count else 1
                    add(Node(coord = coord, colsCount = colsCount, rowsCount = rowsCount))
                }
            }
        }
        return Space(nodes = nodes, rows = rows, cols = cols)
    }

    fun List<String>.extractGalaxies(): List<Node> {
        return buildList {
            for (row in this@extractGalaxies.indices) {
                val line = this@extractGalaxies[row]
                addAll(
                    line.mapIndexedNotNull { col, c -> if (c == '#') Node(Coord(row, col)) else null }
                )
            }
        }
    }

    fun Space.shortestPathFrom(index: Int, to: List<Node>): List<Long> {
        val queue = LinkedList<Node>()
        val shortestPathMap = mutableMapOf<Node, Long>()
        val start = to[index]
        queue.add(start)
        shortestPathMap[start] = 0

        while (queue.isNotEmpty()) {
            val currNode = queue.removeFirst()
            val currPathLen = shortestPathMap[currNode]!!

            directions.forEach { direction ->
                val jump = currNode.apply(direction)
                val adj = currNode.coord.plus(direction)
                    .takeIf { it.isValid(rows, cols) }
                    .let { map[it] }

                if (adj != null && !shortestPathMap.containsKey(adj)) {
                    queue.add(adj)
                    shortestPathMap[adj] = currPathLen + jump
                }
            }
        }
        return to.map { shortestPathMap[it]!! }
    }

    fun part(input: List<String>, count: Int): Long {
        val expanded = input.expandEmptyRowsAndCols(count)
        val galaxies = input.extractGalaxies()

        return galaxies.foldIndexed(0L) { index, acc, g ->
            acc + expanded.shortestPathFrom(index, galaxies).sum()
        } / 2
    }

    val testInput1 = readInput("Day11_test")
    check(part(testInput1, 2) == 374L)
    check(part(testInput1, 10) == 1030L)
    check(part(testInput1, 100) == 8410L)

    val input = readInput("Day11")
    part(input, 2).println()
    part(input, 1000000).println()
}
