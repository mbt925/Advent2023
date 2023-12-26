import java.math.BigDecimal
import java.util.*
import kotlin.math.abs


fun main() {

    fun Double.isEqual(d: Double) = abs(d - this) < e

    data class Coord2D(
        val x: Double,
        val y: Double,
    ) {
        fun inside(min: Double, max: Double) = x in min..max && y in min..max
    }

    data class Coord3D(
        val x: Double,
        val y: Double,
        val z: Double,
    ) {
        fun isEqual(to: Coord3D) = x.isEqual(to.x) && y.isEqual(to.y) && z.isEqual(to.z)
    }

    // line: x = A * a t
    // x = point.x + velocity.x * t, y = point.y + velocity.y * t
    data class Line(
        val point: Coord3D,
        val velocity: Coord3D,
    ) {
        val slope = velocity.y / velocity.x

        fun belongInFuture2D(newPoint: Coord2D): Boolean {
            val xInFuture = (velocity.x > 0 && newPoint.x > point.x) || velocity.x < 0 && newPoint.x < point.x
            val yInFuture = (velocity.y > 0 && newPoint.y > point.y) || velocity.y < 0 && newPoint.y < point.y
            return xInFuture && yInFuture
        }

        fun findT(point: Coord2D): Double {
            return (point.x - this.point.x) / velocity.x
        }

        fun pointAt(t: Double) = Coord3D(
            x = point.x + velocity.x * t,
            y = point.y + velocity.y * t,
            z = point.z + velocity.z * t,
        )

        fun intersectOnX(with: Line): Double? = if (!with.velocity.x.isEqual(velocity.x)) {
            (point.x - with.point.x) / (with.velocity.x - velocity.x)
        } else null

        fun intersectOnY(with: Line): Double? = if (!with.velocity.y.isEqual(velocity.y)) {
            (point.y - with.point.y) / (with.velocity.y - velocity.y)
        } else null

        fun intersectOnZ(with: Line): Double? = if (!with.velocity.z.isEqual(velocity.z)) {
            (point.z - with.point.z) / (with.velocity.z - velocity.z)
        } else null

        fun isParallel2DTo(line: Line) = slope.isEqual(line.slope)

        fun intersection2DWith(line: Line): Coord2D? {
            if (isParallel2DTo(line)) return null

            val x = (line.point.y - point.y + slope * point.x - line.slope * line.point.x) / (slope - line.slope)
            val y = point.y + slope * (x - point.x)

            return Coord2D(x, y)
        }

        fun intersectionWith(line: Line): Boolean {
            // find a common point u on any axis
            val u = intersectOnX(line)
                ?: intersectOnY(line)
                ?: intersectOnZ(line)
                ?: return false

            // try point u on all three axes
            val point1 = pointAt(u)
            val point2 = line.pointAt(u)
            return point1.isEqual(point2)
        }

        fun project(velocityDelta: Coord3D) = copy(
            velocity = Coord3D(
                velocity.x - velocityDelta.x,
                velocity.y - velocityDelta.y,
                velocity.z - velocityDelta.z
            )
        )
    }

    fun String.toLine(): Line {
        val scanner = Scanner(this)
        val x = scanner.nextDouble()
        val y = scanner.nextDouble()
        val z = scanner.nextDouble()
        scanner.next()
        val vx = scanner.nextDouble()
        val vy = scanner.nextDouble()
        val vz = scanner.nextDouble()

        assert(!vx.isEqual(0.0))
        assert(!vy.isEqual(0.0))
        assert(!vz.isEqual(0.0))

        return Line(point = Coord3D(x, y, z), velocity = Coord3D(vx, vy, vz))
    }

    // brute force: try different integer velocities
    // for each velocity, find the intersection of the first two lines
    // if there's an intersection, find the rock line candidate and verify it works for all the lines
    fun List<Line>.findCrossingLineFromAll(): Line {
        val line1 = this[0]
        val line2 = this[1]

        val range = 500L
        for (vx in -range..range) {
            for (vy in -range..range) {
                for (vz in -range..range) {
                    if (vx == 0L || vy == 0L || vz == 0L) continue
                    val vxd = vx.toDouble()
                    val vyd = vy.toDouble()
                    val vzd = vz.toDouble()

                    // assume the stone is stationary with a previous velocity of sv:(svx,svy),
                    // then all lines velocity can be modified like lv:(lvx-svx, lvy-svy)
                    val modifiedLine1 = line1.project(Coord3D(vxd, vyd, vzd))
                    val modifiedLine2 = line2.project(Coord3D(vxd, vyd, vzd))

                    val intersection = modifiedLine1.intersection2DWith(modifiedLine2) ?: continue

                    // rock intersects line1 & 2 at time t
                    val t = modifiedLine1.findT(intersection).toLong().toDouble() // t is integer

                    // calculate the starting position of rock from the intersection point
                    val rockLineCandidate = Line(
                        point = modifiedLine1.pointAt(t),
                        velocity = Coord3D(vxd, vyd, vzd),
                    )

                    val intersectAll = all { line -> rockLineCandidate.intersectionWith(line) }
                    if (intersectAll) return rockLineCandidate
                }
            }
        }
        error("No answer found")
    }

    fun List<Line>.countFutureCrossPathsInArea(min: Double, max: Double): Int {
        var count = 0
        for (i in indices) {
            val line1 = this[i]
            for (j in i + 1..lastIndex) {
                val line2 = this[j]
                val intersection = line1.intersection2DWith(line2) ?: continue
                val belongInFuture = intersection.let { line1.belongInFuture2D(it) && line2.belongInFuture2D(it) }
                if (!belongInFuture) continue
                val inArea = intersection.inside(min, max)
                count += if (inArea) 1 else 0
            }
        }
        return count
    }

    fun part1(input: List<String>, min: Long, max: Long): Int {
        return input.map { it.toLine() }.countFutureCrossPathsInArea(min.toDouble(), max.toDouble())
    }

    fun part2(input: List<String>): BigDecimal {
        return input.map { it.toLine() }.findCrossingLineFromAll()
            .let { (it.point.x + it.point.y + it.point.z).toBigDecimal() }
    }

    val testInput1 = readInput("Day24_test")
    check(part1(testInput1, 7, 27) == 2)
    check(part2(testInput1) == 47.0.toBigDecimal())

    val input = readInput("Day24")
    part1(input, 200000000000000, 400000000000000).println()
    part2(input).println()
}
