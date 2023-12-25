fun main() {

    data class Coord(
        val row: Int,
        val col: Int,
    ) {

        operator fun plus(coord: Coord) = Coord(row + coord.row, col + coord.col)
        fun isValid(rows: Int, cols: Int) = row in 0..<rows && col in 0..<cols

        operator fun List<String>.get(coord: Coord) = this[coord.row][coord.col]

        fun neighbors(graph: List<String>, slopesAllowed: Boolean): List<Coord> {
            val directions = listOf(Coord(-1, 0), Coord(1, 0), Coord(0, 1), Coord(0, -1))
            return directions.mapNotNull { adj ->
                (this + adj)
                    .takeIf { it.isValid(graph.size, graph[0].length) }
                    ?.takeIf {
                        val cell = graph[it]
                        when {
                            cell == '.' -> true
                            cell == '#' -> false
                            slopesAllowed -> true
                            cell == '>' && it.col < col -> false
                            cell == '<' && it.col > col -> false
                            cell == 'v' && it.row < row -> false
                            cell == '^' && it.row > row -> false
                            else -> true
                        }
                    }
            }
        }
    }

    // dfs
    fun List<String>.longestPath(
        current: Coord,
        destination: Coord,
        visited: MutableSet<Coord>,
        currPathLen: Int,
        slopesAllowed: Boolean,
    ): Int {
        visited.add(current)
        val newPathLen = if (current == destination) {
            currPathLen + 1
        } else {
            val neighbors = current.neighbors(this, slopesAllowed).filterNot { visited.contains(it) }
            neighbors.maxOfOrNull { longestPath(it, destination, visited, currPathLen + 1, slopesAllowed) }
                ?: Int.MIN_VALUE
        }

        visited.remove(current)
        return newPathLen
    }

    fun List<String>.longestPath(slopesAllowed: Boolean): Int {
        val visited = mutableSetOf<Coord>()
        val start = Coord(0, this[0].indexOfFirst { it == '.' })
        val destination = Coord(lastIndex, this[lastIndex].indexOfFirst { it == '.' })

        return longestPath(start, destination, visited, 0, slopesAllowed) - 1
    }

    fun part1(input: List<String>): Int {
        return input.longestPath(slopesAllowed = false)
    }

    fun part2(input: List<String>): Int {
        return input.longestPath(slopesAllowed = true)
    }

    val testInput1 = readInput("Day23_test")
    check(part1(testInput1) == 94)
    check(part2(testInput1) == 154)

    val input = readInput("Day23")
    part1(input).println()
    part2(input).println()
}
