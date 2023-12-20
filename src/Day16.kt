import BeamDirection.*
import java.util.LinkedList
import kotlin.math.max

enum class BeamDirection { Left, Up, Right, Down }

fun main() {

    data class Position(
        val row: Int,
        val col: Int,
    ) {
        fun isInside(rows: Int, cols: Int) = row in 0..<rows && col in 0..<cols

        fun move(direction: BeamDirection) = when (direction) {
            Left -> copy(col = col - 1)
            Up -> copy(row = row - 1)
            Right -> copy(col = col + 1)
            Down -> copy(row = row + 1)
        }
    }

    data class Beam(
        val position: Position,
        val direction: BeamDirection,
    ) {
        fun nextPosition() = move(direction)

        fun move(direction: BeamDirection) = copy(position = position.move(direction), direction = direction)
    }

    data class Contraption(
        val board: List<String>
    ) {
        val rows = board.size
        val cols = board[0].length

        private fun moveBeam(beam: Beam): List<Beam> {
            val position = beam.position
            if (!position.isInside(rows, cols)) return emptyList()
            val obj = board[position.row][position.col]
            return buildList {
                when (obj) {
                    '.' -> add(beam.nextPosition())
                    '/' -> {
                        add(
                            when (beam.direction) {
                                Left -> beam.move(Down)
                                Up -> beam.move(Right)
                                Right -> beam.move(Up)
                                Down -> beam.move(Left)
                            }
                        )
                    }

                    '\\' -> {
                        add(
                            when (beam.direction) {
                                Left -> beam.move(Up)
                                Up -> beam.move(Left)
                                Right -> beam.move(Down)
                                Down -> beam.move(Right)
                            }
                        )
                    }

                    '-' -> {
                        when (beam.direction) {
                            Left, Right -> add(beam.nextPosition())
                            Up, Down -> {
                                add(beam.move(Left))
                                add(beam.move(Right))
                            }
                        }
                    }

                    '|' -> {
                        when (beam.direction) {
                            Up, Down -> add(beam.nextPosition())
                            Left, Right -> {
                                add(beam.move(Up))
                                add(beam.move(Down))
                            }
                        }
                    }

                    else -> error("Invalid input: $obj")
                }
            }
        }

        fun energizedCount(start: Position, direction: BeamDirection): Int {
            val cache = mutableSetOf<Beam>()
            val visited = mutableSetOf<Position>()
            val queue = LinkedList<Beam>()
            queue.add(Beam(position = start, direction = direction))

            while (queue.isNotEmpty()) {
                val beam = queue.removeFirst()
                cache.add(beam)

                val next = moveBeam(beam)
                if (next.isNotEmpty()) visited.add(beam.position)
                next.forEach { if (!cache.contains(it)) queue.add(it) }
            }
            return visited.size
        }
    }

    fun part1(input: List<String>): Int {
        return Contraption(input).energizedCount(start = Position(0, 0), direction = Right)
    }

    fun part2(input: List<String>): Int {
        val contraption = Contraption(input)
        val allStartCandidates = buildList {
            // left/right most columns
            for (row in 0..<contraption.rows) {
                add(Beam(Position(row, 0), direction = Right))
                add(Beam(Position(row, contraption.cols - 1), direction = Left))
            }
            // top/bottom most columns
            for (col in 0..<contraption.cols) {
                add(Beam(Position(0, col), direction = Down))
                add(Beam(Position(contraption.rows - 1, col), direction = Up))
            }
        }
        return allStartCandidates.fold(0) { maxEnergized, candidate ->
            val count = contraption.energizedCount(start = candidate.position, direction = candidate.direction)
            max(maxEnergized, count)
        }
    }

    val testInput1 = readInput("Day16_test")
    check(part1(testInput1) == 46)
    check(part2(testInput1) == 51)

    val input = readInput("Day16")
    part1(input).println()
    part2(input).println()
}
