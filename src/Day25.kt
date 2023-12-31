import java.util.*

fun main() {

    data class CutOfThePhase(
        val node: String,
        val weight: Int,
    )

    data class Graph(
        val connections: Map<String, Set<String>>,
    ) {
        val nodes = connections.keys.filterNot { it.isEmpty() }

        // minimum cut: Stoer–Wagner algorithm
        fun minimumCut(): Int {
            val currentPartition = mutableSetOf<String>()
            val bestPartition = mutableSetOf<String>()
            var maxScore = Int.MIN_VALUE
            val cutWeight = IntArray(nodes.size)
            val merged = BooleanArray(nodes.size)

            for (i in 0..<nodes.lastIndex) {
                val cutOfThePhase = maximumAdjacencySearch(merged, cutWeight)
                currentPartition.add(cutOfThePhase.node)
                // only interested in cuts with exactly 3 edges and the maximum score
                val score = currentPartition.size.let { it * (nodes.size - it) }
                val edges = outEdges(currentPartition)
                if (edges == 3L && score > maxScore) {
                    maxScore = score
                    bestPartition.clear()
                    bestPartition.addAll(currentPartition)
                }
                merge(cutOfThePhase.node, merged, cutWeight)
            }
            return maxScore
        }

        private fun maximumAdjacencySearch(merged: BooleanArray, cutWeight: IntArray): CutOfThePhase {
            var maxCutWeight = -1
            var pickedNode = ""

            for (j in nodes.indices) {
                if (!merged[j]) {
                    val weight = cutWeight[j]
                    if (weight >= maxCutWeight) {
                        maxCutWeight = weight
                        pickedNode = nodes[j]
                    }
                }
            }
            return CutOfThePhase(pickedNode, maxCutWeight)
        }

        private fun merge(node: String, merged: BooleanArray, cutWeight: IntArray) {
            val nodeIndex = nodes.indexOf(node)
            merged[nodeIndex] = true

            for (i in nodes.indices) {
                if (!merged[i]) {
                    cutWeight[i] += if (connections[node]!!.contains(nodes[i])) 1 else 0
                }
            }
        }

        private fun outEdges(partition: Set<String>): Long {
            return partition.sumOf { from ->
                connections[from]!!.sumOf { to ->
                    if (partition.contains(to)) 0L else 1L
                }
            }
        }
    }

    fun List<String>.toGraph(): Graph {
        val edges = mutableMapOf<String, MutableSet<String>>()
        forEach { line ->
            val scanner = Scanner(line)
            val from = scanner.next().dropLast(1)
            val to = buildSet { while (scanner.hasNext()) add(scanner.next()) }
            edges.putIfAbsent(from, mutableSetOf())
            to.forEach { connection ->
                edges[from]!!.add(connection)
                edges.putIfAbsent(connection, mutableSetOf())
                edges[connection]!!.add(from)
            }
        }
        return Graph(edges)
    }

    fun part1(input: List<String>): Int {
        return input.toGraph().minimumCut()
    }

    val testInput1 = readInput("Day25_test")
    check(part1(testInput1) == 54)

    val input = readInput("Day25")
    part1(input).println()
}
