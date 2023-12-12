fun main() {

    data class Key(
        val value: String,
    ) {
        val endWithA by lazy { value.endsWith("A") }
        val endWithZ by lazy { value.endsWith("Z") }
    }

    fun String.toKey() = Key(this)

    fun greatestCommonDivisor(a: Long, b: Long): Long {
        return if (b == 0L) a else greatestCommonDivisor(b, a % b)
    }

    fun leastCommonMultiple(a: Long, b: Long): Long {
        return if (a == 0L || b == 0L) 0 else (a * b) / greatestCommonDivisor(a, b)
    }

    fun leastCommonMultiple(numbers: List<Int>): Long {
        return numbers.fold(1L) { acc, num ->
            leastCommonMultiple(acc, num.toLong())
        }
    }

    data class Node(
        val value: Key,
        val left: Key,
        val right: Key,
    )

    class Tree {
        val map = mutableMapOf<Key, Node>()

        fun addNode(node: Node) {
            map.putIfAbsent(node.value, node)
        }

        fun stepsToReach(instruction: String, start: Key, isTarget: (target: Key) -> Boolean): Int {
            var currValue = start
            var steps = 0
            while (!isTarget(currValue)) {
                val currKey = map[currValue]!!
                val index = steps % instruction.length
                val direction = instruction[index]
                currValue = if (direction == 'L') currKey.left else currKey.right
                steps++
            }
            return steps
        }

        fun stepsToReachGhost(instruction: String): Long {
            val sourceNodes = map.keys.filter { it.endWithA }
            val sourceSteps = sourceNodes.map { key ->
                stepsToReach(instruction, key, isTarget = { it.endWithZ })
            }
            return leastCommonMultiple(sourceSteps)
        }
    }

    fun String.toNode(): Node {
        return Node(
            value = substring(0, 3).toKey(),
            left = substring(7, 10).toKey(),
            right = substring(12, 15).toKey(),
        )
    }

    fun part1(input: List<String>): Int {
        val instruction = input[0]

        val nodes = buildList {
            for (i in 2..input.lastIndex) {
                add(input[i].toNode())
            }
        }
        val tree = Tree()
        nodes.forEach { tree.addNode(it) }
        return tree.stepsToReach(instruction, "AAA".toKey(), isTarget = { it.value == "ZZZ" })
    }

    fun part2(input: List<String>): Long {
        val instruction = input[0]

        val nodes = buildList {
            for (i in 2..input.lastIndex) {
                add(input[i].toNode())
            }
        }
        val tree = Tree()
        nodes.forEach { tree.addNode(it) }
        return tree.stepsToReachGhost(instruction)
    }

    val testInput1 = readInput("Day08_test")
    val testInput2 = readInput("Day08_test2")
    val testInputPart2 = readInput("Day08_test_part2")
    check(part1(testInput1) == 2)
    check(part1(testInput2) == 6)
    check(part2(testInputPart2) == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}

