import kotlin.math.abs
import kotlin.math.max

fun main() {

    data class Coord(
        val x: Int,
        val y: Int,
        val z: Int,
    )

    data class Size(
        val width: Int,
        val height: Int,
        val depth: Int,
    )

    data class Brick(
        val name: String,
        val start: Coord,
        val size: Size,
    ) {
        val topEdgeZ = start.z + size.depth - 1
        fun lowerTo(z: Int) = copy(start = start.copy(z = max(1, z)))
        fun supports(brick: Brick): Boolean {
            val xOverlap = (start.x < brick.start.x + brick.size.width) && (start.x + size.width > brick.start.x)
            val yOverlap = (start.y < brick.start.y + brick.size.height) && (start.y + size.height > brick.start.y)
            return xOverlap && yOverlap
        }
    }

    data class Graph(
        val brickToSupportingBricks: Map<Brick, List<Brick>>,
        val brickToSupportedBricks: Map<Brick, List<Brick>>,
    ) {

        // 1: bricks which don't support other bricks
        // 2: bricks which support other bricks, and every supported brick is supported by at least another brick
        fun countSafelyRemovable(): Int {
            val removableBricks = mutableSetOf<Brick>()
            brickToSupportedBricks.forEach { brick ->
                if (brick.value.isEmpty()) removableBricks.add(brick.key)
                else {
                    if (
                        brick.value.all { supportedBrick ->
                            (brickToSupportingBricks.getOrDefault(supportedBrick, emptyList()).any { it != brick.key })
                        }) {
                        removableBricks.add(brick.key)
                    }
                }
            }
            return removableBricks.size
        }

        // remove a brick and recursively remove every brick with all their supporting bricks removed
        fun removeBrick(brick: Brick, removedBricks: MutableSet<Brick>) {
            removedBricks.add(brick)

            // find all bricks which can be removed
            val removableBricks = brickToSupportedBricks[brick]!!.filter { supportedBrick ->
                (removedBricks.containsAll(brickToSupportingBricks[supportedBrick]!!))
            }

            removableBricks.forEach { removableBrick ->
                removeBrick(removableBrick, removedBricks)
            }
        }

        fun sumOfRemovedBricks(): Int {
            return brickToSupportedBricks.keys.fold(0) { acc, brick ->
                val removedBricks = mutableSetOf<Brick>()
                removeBrick(brick, removedBricks)
                acc + removedBricks.size - 1
            }
        }
    }

    fun Map<Int, List<Brick>>.toGraph(): Graph {
        val supportingBricks = mutableMapOf<Brick, MutableList<Brick>>()
        val supportedBricks = mutableMapOf<Brick, MutableList<Brick>>()
        values.flatten().forEach { brick ->
            supportingBricks.putIfAbsent(brick, mutableListOf())
            supportedBricks.putIfAbsent(brick, mutableListOf())
        }

        val maxZ = keys.maxBy { it }
        for (z in 1..maxZ) {
            getOrDefault(z, listOf()).forEach { brick ->
                // get all bricks on the upper topEdgeZ
                val upperTopZEdgeBricks = getOrDefault(brick.topEdgeZ + 1, emptyList())
                upperTopZEdgeBricks.forEach { upperBrick ->
                    if (brick.supports(upperBrick)) {
                        supportingBricks[upperBrick]!!.add(brick)
                        supportedBricks[brick]!!.add(upperBrick)
                    }
                }
            }
        }
        return Graph(
            brickToSupportingBricks = supportingBricks,
            brickToSupportedBricks = supportedBricks,
        )
    }

    fun List<Brick>.fall(): Map<Int, List<Brick>> {
        val sortedByZ = sortedBy { it.start.z }
        val bricksPerZMap = mutableMapOf<Int, MutableList<Brick>>()
        val bricksTopEdgePerZMap = mutableMapOf<Int, MutableList<Brick>>()

        sortedByZ.forEach { brick ->
            for (z in brick.start.z downTo 1 step 1) {
                val loweredBrick = brick.lowerTo(z)
                val loweredZ = max(1, z - 1)

                // at the bottom or hit a brick at the level below
                if (z == 1 || bricksTopEdgePerZMap.getOrDefault(loweredZ, mutableListOf()).any { it.supports(brick) }) {
                    // can't go down further
                    bricksPerZMap.putIfAbsent(z, mutableListOf())
                    bricksPerZMap[z]!!.add(loweredBrick)

                    bricksTopEdgePerZMap.putIfAbsent(loweredBrick.topEdgeZ, mutableListOf())
                    bricksTopEdgePerZMap[loweredBrick.topEdgeZ]!!.add(loweredBrick)
                    break
                }
            }
        }
        return bricksPerZMap
    }

    fun String.toBrick(name: String): Brick {
        val split = split("~")
        val (x1, y1, z1) = split[0].split(",").map { it.toInt() }
        val (x2, y2, z2) = split[1].split(",").map { it.toInt() }
        assert(x1 != x2 && y1 != y2)
        val width = abs(x1 - x2) + 1
        val height = abs(y1 - y2) + 1
        val depth = abs(z1 - z2) + 1
        return Brick(
            name = name,
            start = Coord(x1, y1, z1),
            size = Size(width, height, depth),
        )
    }

    fun List<String>.toBricks() = mapIndexed { index, line -> line.toBrick(('A' + index).toString()) }

    fun part1(input: List<String>): Int {
        return input.toBricks().fall().toGraph().countSafelyRemovable()
    }

    fun part2(input: List<String>): Int {
        return input.toBricks().fall().toGraph().sumOfRemovedBricks().also { it.println() }
    }

    val testInput1 = readInput("Day22_test")
    check(part1(testInput1) == 5)
    check(part2(testInput1) == 7)

    val input = readInput("Day22")
    part1(input).println()
    part2(input).println()
}
