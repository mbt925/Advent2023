import kotlin.math.min

fun main() {

    val otherFormsOfDigits = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

    fun String.findAMatch(startIndex: Int, set: List<String>): Int {
        set.forEachIndexed { index, d ->
            if (substring(startIndex, min(this.length, startIndex + d.length)) == d) {
                return index + 1
            }
        }
        return -1
    }

    fun String.matchADigit(index: Int, c: Char): Int {
        if (c.isDigit()) return c.digitToInt()
        return findAMatch(index, otherFormsOfDigits)
    }

    fun String.firstDigit(): Int {
        forEachIndexed { index, c ->
            val match = matchADigit(index, c)
            if (match >= 0) return match
        }
        error("Invalid input")
    }

    fun String.lastDigit(): Int {
        forEachIndexedReversed { index, c ->
            val match = matchADigit(index, c)
            if (match >= 0) return match
        }
        error("Invalid input")
    }

    fun part1(input: List<String>): Int {
        return input.fold(0) { acc, line ->
            val firstDigit = line.first { it.isDigit() }.digitToInt()
            val lastDigit = line.last { it.isDigit() }.digitToInt()

            acc + firstDigit * 10 + lastDigit
        }
    }

    fun part2(input: List<String>): Int {
        return input.fold(0) { acc, line ->
            val firstDigit = line.firstDigit()
            val lastDigit = line.lastDigit()

            acc + firstDigit * 10 + lastDigit
        }
    }

    val testInput1 = readInput("Day01_test_part1")
    check(part1(testInput1) == 142)

    val testInput2 = readInput("Day01_test_part2")
    check(part2(testInput2) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
