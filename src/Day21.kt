fun main() {

    data class Point(
        val row: Int,
        val col: Int,
    ) {
        operator fun plus(p: Point) = Point((row + p.row), col + p.col)
        fun normalize(rows: Int, cols: Int) = Point((row % rows + rows) % rows, (col % cols + cols) % cols)
    }

    val directions = arrayOf(Point(-1, 0), Point(1, 0), Point(0, -1), Point(0, 1))

    fun List<String>.findStart(): Point {
        forEachIndexed { row, line ->
            val index = line.indexOf('S')
            if (index != -1) return Point(row, index)
        }
        error("No start found")
    }

    operator fun List<String>.get(point: Point) = this[point.row][point.col]

    fun List<String>.isValid(point: Point) = this[point.normalize(size, this[0].length)] != '#'

    fun List<String>.bfs(start: Point, targetSteps: Int): Int {
        var currPoints = mutableSetOf(start)
        var steps = 0
        val visited = mutableSetOf<Point>()
        val totalEvenOdd = mutableListOf(0, 0)
        totalEvenOdd[0] = 1

        while (steps < targetSteps) {
            val nextPoints = mutableSetOf<Point>()
            currPoints.forEach { point ->
                visited.add(point)
                directions.forEach { direction ->
                    val adj = point + direction
                    if (isValid(adj) && !visited.contains(adj)) nextPoints.add(adj)
                }
            }
            currPoints = nextPoints
            steps++
            totalEvenOdd[steps % 2] += nextPoints.size
        }
        return totalEvenOdd[steps % 2]
    }

    fun calculateY(coefficients: Triple<Double, Double, Double>, x: Long): Double {
        return coefficients.first * x * x + coefficients.second * x + coefficients.third
    }

    /**
     * After having crossed the border of the first grid, all further border crossings are separated by n steps (length of grid)
     * Therefore, the total number of grids to traverse in any direction is finalX = 26_501_365 / n
     * Assumption: at step 26_501_365 another border crossing is taking place
     * If so, then it follows that the first crossing takes place at interval = 26_501_365 % n
     */
    fun List<String>.bfsInfinity(start: Point, targetSteps: Int): Long {
        val rows = size
        val cols = this[0].length
        assert(rows == cols)

        val finalX = targetSteps / cols
        val Y = mutableListOf<Int>()
        val interval = targetSteps % cols
        val borderCrossings = listOf(interval, interval + cols, interval + 2 * cols)
        borderCrossings.println()

        var currPoints = mutableSetOf(start)
        var steps = 0
        val visited = mutableSetOf<Point>()
        val totalEvenOdd = mutableListOf(0, 0)
        totalEvenOdd[0] = 1

        while (steps < borderCrossings.last()) {
            val nextPoints = mutableSetOf<Point>()
            currPoints.forEach { point ->
                visited.add(point)
                directions.forEach { direction ->
                    val adj = point + direction
                    if (isValid(adj) && !visited.contains(adj)) nextPoints.add(adj)
                }
            }
            currPoints = nextPoints
            steps++
            totalEvenOdd[steps % 2] += nextPoints.size

            if (steps in borderCrossings) Y.add(totalEvenOdd[steps % 2])
        }
        val X = listOf(0.0, 1.0, 2.0)
        val coefficients3 = findParabolaEquation(X, Y.map { it.toDouble() })
        return calculateY(coefficients3, finalX.toLong()).toLong()
    }

    fun part1(input: List<String>, steps: Int): Int {
        return input.bfs(input.findStart(), steps)
    }

    fun part2(input: List<String>, steps: Int): Long {
        return input.bfsInfinity(input.findStart(), steps)
    }

    val testInput1 = readInput("Day21_test")
    check(part1(testInput1, 6) == 16)
    check(part2(testInput1, 6) == 16L)
    check(part2(testInput1, 10) == 50L)

    val input = readInput("Day21")
    part1(input, 64).println()
    part2(input, 26501365).println()
}
