import MoveDirection.*
import java.util.*

enum class MoveDirection { Left, Up, Right, Down }

fun main() {

    data class Position(
        val row: Int,
        val col: Int,
    ) {
        fun isInside(rows: Int, cols: Int) = row in 0..<rows && col in 0..<cols

        fun move(direction: MoveDirection) = when (direction) {
            Left -> copy(col = col - 1)
            Up -> copy(row = row - 1)
            Right -> copy(col = col + 1)
            Down -> copy(row = row + 1)
        }
    }

    data class Block(
        val position: Position,
        val direction: MoveDirection,
        val pathLen: Int,
    ) {
        fun move(direction: MoveDirection) = copy(
            position = position.move(direction),
            direction = direction,
            pathLen = if (this.direction == direction) pathLen + 1 else 1,
        )
    }

    data class BlockWithHeatLoss(
        val block: Block,
        val aggregatedHeatLoss: Int,
    ) {
        val position: Position = block.position
        val direction: MoveDirection = block.direction
        val pathLen: Int = block.pathLen

        fun moveOrJumpToFourthBlock(direction: MoveDirection): BlockWithHeatLoss {
            var curr = this
            val jumpLen = if (direction == this.direction) 4 - pathLen else 4
            repeat(jumpLen) { curr = curr.move(direction) }
            if (jumpLen <= 0) curr = curr.move(direction)
            return curr
        }

        fun move(direction: MoveDirection) = copy(block = block.move(direction))

        fun isValidForCrucible(rows: Int, cols: Int) = block.pathLen <= 3 && block.position.isInside(rows, cols)
        fun isValidForUltraCrucible(rows: Int, cols: Int) = block.pathLen <= 10 && block.position.isInside(rows, cols)
    }

    data class CityMap(
        val blocks: List<String>
    ) {
        val rows = blocks.size
        val cols = blocks[0].length

        fun moveCrucible(block: BlockWithHeatLoss, direction: MoveDirection, rows: Int, cols: Int): BlockWithHeatLoss? {
            return block.move(direction).takeIf { it.isValidForCrucible(rows, cols) }
                ?.run { copy(aggregatedHeatLoss = aggregatedHeatLoss + blocks[position.row][position.col].digitToInt()) }
        }

        fun moveUltraCrucible(
            block: BlockWithHeatLoss,
            direction: MoveDirection,
            rows: Int,
            cols: Int
        ): BlockWithHeatLoss? {
            return block.moveOrJumpToFourthBlock(direction)
                .takeIf { it.isValidForUltraCrucible(rows, cols) }
                ?.run {
                    val sumOfHeatLoss = sumOfHeatLoss(
                        block.position,
                        direction,
                        if (direction == block.direction) pathLen - block.pathLen else 4
                    )
                    copy(aggregatedHeatLoss = aggregatedHeatLoss + sumOfHeatLoss)
                }
        }

        fun sumOfHeatLoss(start: Position, direction: MoveDirection, len: Int): Int {
            val positions = buildList {
                var curr = start
                repeat(len) {
                    curr = curr.move(direction)
                    add(curr)
                }
            }
            return positions.sumOf { blocks[it.row][it.col].digitToInt() }
        }

        fun crucibleNeighbors(blockWithHeatLoss: BlockWithHeatLoss): List<BlockWithHeatLoss> {
            return buildList {
                moveCrucible(blockWithHeatLoss, blockWithHeatLoss.direction, rows, cols)?.also { add(it) }

                when (blockWithHeatLoss.direction) {
                    Left, Right -> {
                        moveCrucible(blockWithHeatLoss, Up, rows, cols)?.also { add(it) }
                        moveCrucible(blockWithHeatLoss, Down, rows, cols)?.also { add(it) }
                    }

                    Up, Down -> {
                        moveCrucible(blockWithHeatLoss, Left, rows, cols)?.also { add(it) }
                        moveCrucible(blockWithHeatLoss, Right, rows, cols)?.also { add(it) }
                    }
                }
            }
        }

        fun ultraCrucibleNeighbors(blockWithHeatLoss: BlockWithHeatLoss): List<BlockWithHeatLoss> {
            return buildList {
                moveUltraCrucible(blockWithHeatLoss, blockWithHeatLoss.direction, rows, cols)?.also { add(it) }

                if (blockWithHeatLoss.pathLen >= 4) {
                    when (blockWithHeatLoss.direction) {
                        Left, Right -> {
                            moveUltraCrucible(blockWithHeatLoss, Up, rows, cols)?.also { add(it) }
                            moveUltraCrucible(blockWithHeatLoss, Down, rows, cols)?.also { add(it) }
                        }

                        Up, Down -> {
                            moveUltraCrucible(blockWithHeatLoss, Left, rows, cols)?.also { add(it) }
                            moveUltraCrucible(blockWithHeatLoss, Right, rows, cols)?.also { add(it) }
                        }
                    }
                }
            }
        }

        // Dijkstra using a conditional getNeighbors lambda
        private fun minHeatLoss(
            source: Position,
            destination: Position,
            getNeighbors: (BlockWithHeatLoss) -> List<BlockWithHeatLoss>,
        ): Int {
            val minHeatLossMap = mutableMapOf<Position, Int>()
            val visited = mutableSetOf<Block>()
            val queue = PriorityQueue<BlockWithHeatLoss>(compareBy { it.aggregatedHeatLoss })
            val path = mutableMapOf<Position, Position>()
            queue.add(BlockWithHeatLoss(Block(source, Right, 0), 0))
            minHeatLossMap[source] = 0

            while (queue.isNotEmpty()) {
                val blockWithHeatLoss = queue.remove()
                if (blockWithHeatLoss.position == destination) break
                getNeighbors(blockWithHeatLoss).forEach { neighbor ->
                    if (!visited.contains(neighbor.block)) {
                        if (neighbor.aggregatedHeatLoss < minHeatLossMap.getOrDefault(
                                neighbor.position,
                                Int.MAX_VALUE
                            )
                        ) {
                            minHeatLossMap[neighbor.position] = neighbor.aggregatedHeatLoss
                            path[neighbor.block.position] = blockWithHeatLoss.block.position
                        }
                        queue.add(neighbor)
                        visited.add(neighbor.block)
                    }
                }
            }
            return minHeatLossMap[destination]!!
        }

        fun minHeatLossForCrucible(source: Position, destination: Position): Int {
            return minHeatLoss(source, destination, ::crucibleNeighbors)
        }

        fun minHeatLossForUltraCrucible(source: Position, destination: Position): Int {
            return minHeatLoss(source, destination, ::ultraCrucibleNeighbors)
        }
    }

    fun part1(input: List<String>): Int {
        val source = Position(0, 0)
        val destination = Position(input.size - 1, input[0].length - 1)
        return CityMap(input).minHeatLossForCrucible(source, destination)
    }

    fun part2(input: List<String>): Int {
        val source = Position(0, 0)
        val destination = Position(input.size - 1, input[0].length - 1)
        return CityMap(input).minHeatLossForUltraCrucible(source, destination)
    }

    val testInput1 = readInput("Day17_test")
    val testInput2 = readInput("Day17_test2")
    check(part1(testInput1) == 102)
    check(part2(testInput1) == 94)
    check(part2(testInput2) == 71)

    val input = readInput("Day17")
    part1(input).println()
    part2(input).println()
}
