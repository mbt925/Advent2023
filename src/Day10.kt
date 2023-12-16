import Direction.*
import NodeType.*
import java.util.Stack
import kotlin.math.abs

private enum class Direction {
    Left, Right, Top, Bottom;

    fun inverse() = when (this) {
        Left -> Right
        Right -> Left
        Top -> Bottom
        Bottom -> Top
    }
}

private enum class NodeType(val value: Char) {
    TopBottom('|'),
    LeftRight('-'),
    TopRight('L'),
    TopLeft('J'),
    BottomLeft('7'),
    BottomRight('F'),
    Ground('.'),
    Start('S');

    companion object {
        fun fromValue(value: Char) = entries.first { it.value == value }
    }

    private fun isPossibleToConnectTo(direction: Direction): Boolean {
        if (this == Start) return true
        if (this == Ground) return false
        return when (direction) {
            Left -> this == LeftRight || this == TopLeft || this == BottomLeft
            Right -> this == LeftRight || this == TopRight || this == BottomRight
            Top -> this == TopBottom || this == TopRight || this == TopLeft
            Bottom -> this == TopBottom || this == BottomRight || this == BottomLeft
        }
    }

    fun isConnectedToEdge(neighbor: NodeType, direction: Direction): Boolean {
        return isPossibleToConnectTo(direction) && neighbor.isPossibleToConnectTo(direction.inverse())
    }
}

fun main() {

    data class Coord(
        val row: Int,
        val col: Int,
    ) {
        operator fun plus(c: Coord) = Coord(row + c.row, col + c.col)

        fun isValid(rows: Int, cols: Int) = !(row < 0 || col < 0 || row >= rows || col >= cols)
    }

    data class Node(
        val type: NodeType,
        val coord: Coord,
        val order: Int = -1,
    )

    data class Graph(
        val start: Node,
        val neighborsMap: Map<Node, List<Node>>,
    )

    fun List<String>.findS(): Node {
        this.forEachIndexed { rowIndex, line ->
            val colIndex = line.indexOf('S')
            if (colIndex >= 0) return Node(type = Start, coord = Coord(rowIndex, colIndex))
        }
        error("No S found")
    }

    val adjCoords = listOf(Coord(0, -1), Coord(0, 1), Coord(-1, 0), Coord(1, 0))
    val directions = listOf(Left, Right, Top, Bottom)

    fun List<String>.neighbors(node: Node): List<Node> {
        val rows = this.size
        val cols = this[0].length
        return buildList {
            adjCoords.forEachIndexed { index, adj ->
                val neighborCoor = node.coord + adj
                if (neighborCoor.isValid(rows, cols)) {
                    val neighborNode = Node(
                        type = NodeType.fromValue(this@neighbors[neighborCoor.row][neighborCoor.col]),
                        coord = neighborCoor,
                    )
                    if (node.type.isConnectedToEdge(neighborNode.type, directions[index])) add(neighborNode)
                }
            }
        }
    }

    fun List<String>.toGraph(): Graph {
        val start = findS()
        val coordSet = mutableSetOf<Coord>()
        val neighborsMap = mutableMapOf<Node, List<Node>>()

        var currNodes = listOf(start)
        while (currNodes.isNotEmpty()) {
            val nextNodes = mutableListOf<Node>()
            currNodes.forEach { node ->
                val neighbors = this.neighbors(node)
                neighbors.forEach { if (coordSet.add(it.coord)) nextNodes.add(it) }
                neighborsMap[node] = neighbors
            }
            currNodes = nextNodes
        }
        return Graph(
            start = start,
            neighborsMap = neighborsMap,
        )
    }

    // DFS
    fun Graph.findLoop(): List<Node> {
        val visited = mutableSetOf<Node>()
        val stacks = Stack<Stack<Node>>()
        stacks.push(Stack<Node>().apply { add(start) })

        while (true) {
            val currStack = stacks.peek()
            val currNode = currStack.peek()
            val neighbors = neighborsMap[currNode]!!
            visited.add(currNode)

            val unVisitedNeighbors = neighbors.filterNot { visited.contains(it) }
            if (unVisitedNeighbors.isEmpty()) {
                if (neighbors.find { it == start } != null) break
                currStack.pop()
                if (currStack.isEmpty()) stacks.pop()
            } else {
                val newStack = Stack<Node>()
                newStack.addAll(unVisitedNeighbors)
                stacks.push(newStack)
            }
        }
        return buildList {
            var order = 0
            while (stacks.isNotEmpty()) {
                val currStack = stacks.pop()
                add(currStack.peek().copy(order = order))
                order++
            }
        }
    }

    // Shoelace formula
    fun List<Node>.area(): Double {
        var area = 0.0
        for (i in indices) {
            val currNode = this[i]
            val nextNode = this[(i + 1) % size]
            area += currNode.coord.col * nextNode.coord.row - nextNode.coord.col * currNode.coord.row
        }
        return abs(area / 2.0)
    }

    fun part1(input: List<String>): Int {
        val graph = input.toGraph()
        val loop = graph.findLoop()
        return loop.size / 2
    }

    fun part2(input: List<String>): Int {
        val graph = input.toGraph()
        val loop = graph.findLoop()
        val area = loop.area()
        val bHalf = loop.size / 2

        // Pick's theorem: Area = i + b/2 - 1
        // Given a polygon with integer coordinates
        // i: the number of integer points interior to the polygon
        // b: the number of integer points on its boundary
        return (area - bHalf + 1).toInt()
    }

    val testInput1 = readInput("Day10_test1")
    val testInput2 = readInput("Day10_test2")
    val testInput3 = readInput("Day10_test1_part2")
    val testInput4 = readInput("Day10_test2_part2")

    check(part1(testInput1) == 4)
    check(part1(testInput2) == 8)
    check(part2(testInput3) == 4)
    check(part2(testInput4) == 8)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}
