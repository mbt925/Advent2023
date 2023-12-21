import DigDirection.*
import kotlin.math.abs

enum class DigDirection {
    Up, Down, Left, Right;

    companion object {
        fun from(c: Char) = when (c) {
            'R' -> Right
            'D' -> Down
            'L' -> Left
            'U' -> Up
            else -> error("Invalid input: $c")
        }

        fun from(i: Int) = when (i) {
            0 -> Right
            1 -> Down
            2 -> Left
            3 -> Up
            else -> error("Invalid input: $i")
        }
    }
}

fun main() {

    data class Point(
        val x: Long,
        val y: Long,
    )

    data class Polygon(
        val points: List<Point>,
        val pointsOnBoundary: Int,
    ) {
        // Shoelace formula
        fun area(): Double {
            var area = 0.0
            for (i in points.indices) {
                val currNode = points[i]
                val nextNode = points[(i + 1) % points.size]
                area += currNode.x * nextNode.y - nextNode.x * currNode.y
            }
            return abs(area / 2.0)
        }
    }

    data class DigCommand(
        val direction: DigDirection,
        val amount: Int,
        val color: String,
    ) {
        val colorDirection = DigDirection.from(color.last().digitToInt())
        val colorAmount = color.drop(1).dropLast(1).toInt(16)
    }

    fun List<String>.toDigCommands(): List<DigCommand> {
        return map { line ->
            val split = line.split(" ")
            DigCommand(
                direction = DigDirection.from(split[0][0]),
                amount = split[1].toInt(),
                color = split[2].drop(1).dropLast(1),
            )
        }
    }

    fun List<DigCommand>.toPolygon(
        getDirection: (DigCommand) -> DigDirection,
        getAmount: (DigCommand) -> Int,
    ): Polygon {
        var pointsOnBoundary = 0
        val points = buildList {
            var startX = 0L
            var startY = 0L
            add(Point(startX, startY))

            this@toPolygon.forEach { command ->
                val direction = getDirection(command)
                val amount = getAmount(command)
                when (direction) {
                    Up -> startY -= amount
                    Down -> startY += amount
                    Left -> startX -= amount
                    Right -> startX += amount
                }
                add(Point(startX, startY))
                pointsOnBoundary += amount
            }
        }
        return Polygon(
            points = points,
            pointsOnBoundary = pointsOnBoundary,
        )
    }

    fun Polygon.pointsOnBoundaryAndInside(): Long {
        val area = area()
        val bHalf = pointsOnBoundary / 2

        // Pick's theorem: Area = i + b/2 - 1
        // Given a polygon with integer coordinates
        // i: the number of integer points interior to the polygon
        // b: the number of integer points on its boundary
        val i = (area - bHalf + 1).toLong()
        return i + pointsOnBoundary
    }

    fun part1(input: List<String>): Long {
        return input
            .toDigCommands()
            .toPolygon(getDirection = { it.direction }, getAmount = { it.amount })
            .pointsOnBoundaryAndInside()
    }

    fun part2(input: List<String>): Long {
        return input
            .toDigCommands()
            .toPolygon(getDirection = { it.colorDirection }, getAmount = { it.colorAmount })
            .pointsOnBoundaryAndInside()
    }

    val testInput1 = readInput("Day18_test")
    check(part1(testInput1) == 62L)
    check(part2(testInput1) == 952408144115L)

    val input = readInput("Day18")
    part1(input).println()
    part2(input).println()
}
