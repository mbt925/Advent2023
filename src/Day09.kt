import java.util.Scanner

fun main() {

    fun List<Long>.predictNextNum(): Long {
        var allEquals = false
        val uniquesSet = mutableSetOf<Long>()
        var nextSet = this
        val lastNums = mutableListOf<Long>()
        lastNums.add(nextSet.last())
        while (!allEquals) {
            nextSet = buildList {
                for (i in 0..nextSet.size - 2) {
                    val diff = nextSet[i + 1] - nextSet[i]
                    add(diff)
                    uniquesSet.add(diff)
                }
            }
            lastNums.add(nextSet.last())
            allEquals = uniquesSet.size == 1
            uniquesSet.clear()
        }
        return lastNums.sum()
    }

    fun List<Long>.predictPreviousNum(): Long {
        var allEquals = false
        val uniquesSet = mutableSetOf<Long>()
        var nextSet = this
        val firstNums = mutableListOf<Long>()
        firstNums.add(nextSet.first())
        while (!allEquals) {
            nextSet = buildList {
                for (i in 0..nextSet.size - 2) {
                    val diff = nextSet[i + 1] - nextSet[i]
                    add(diff)
                    uniquesSet.add(diff)
                }
            }
            firstNums.add(nextSet.first())
            allEquals = uniquesSet.size == 1
            uniquesSet.clear()
        }
        return firstNums.foldIndexed(0) {index, acc, num ->
            acc + num * if (index % 2 == 0) 1 else -1
        }
    }

    fun part1(input: List<String>): Long {
        return input.sumOf { line ->
            val scanner = Scanner(line)
            buildList {
                while (scanner.hasNext()) add(scanner.nextLong())
            }.predictNextNum()
        }
    }

    fun part2(input: List<String>): Long {
        return input.sumOf { line ->
            val scanner = Scanner(line)
            buildList {
                while (scanner.hasNext()) add(scanner.nextLong())
            }.predictPreviousNum()
        }
    }

    val testInput1 = readInput("Day09_test")
    check(part1(testInput1) == 114L)
    check(part2(testInput1) == 2L)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}

