import java.util.Scanner

fun main() {

    fun String.damaged() = count { it == '#' }
    fun String.questionMarks() = count { it == '?' }

    data class Input(
        val conditions: String,
        val groupsReport: String,
    ) {
        val groupsReportCount by lazy { groupsReport.split(",").map { it.toInt() } }
    }

    fun Input.ifOperational(dp: MutableMap<Input, Long>): Long {
        return dp[copy(conditions = conditions.dropLast(1))]!!
    }

    fun Input.ifDamaged(dp: MutableMap<Input, Long>, arrangement: Input.(dp: MutableMap<Input, Long>) -> Long): Long {
        val lastGroupCount = groupsReportCount.last()
        val lastConditions = conditions.takeLast(lastGroupCount)
        return if (lastConditions.damaged() + lastConditions.questionMarks() != lastGroupCount) 0
        else {
            val separator = if (conditions.length > lastGroupCount) conditions[conditions.lastIndex - lastGroupCount] else '.'
            if (separator == '#') 0
            else {
                val inp = Input(
                    conditions = conditions.dropLast(lastGroupCount + 1),
                    groupsReport = groupsReportCount.dropLast(1).joinToString(",")
                )
                dp[inp] ?: inp.arrangement(dp)
            }
        }
    }

    /**
     * DP: table[conditions][groups]
     * table[0][0] = 1
     * table[0][>0] = 0
     * table[any #][0] = 0
     * table[no #][0] = 1
     *
     * take last symbol
     * 1. if . -> ignore it and return table[conditions.dropLast()][groups]
     * 2. if # -> If not possible to form the last group using this #, return 0
     *           If not possible to leave a . before this group, return 0
     *           else return table[condition.dropLast(lastGroupLen + 1)], 1 is for the separator (.)
     * 3. If ? -> It's either . or #
     *           If . : do as the first case
     *           If # : do as the second case
     */
    fun Input.arrangement(dp: MutableMap<Input, Long> = mutableMapOf()): Long {
        for (conditionsLen in 0..conditions.length) {
            for (reportsCount in 0..groupsReportCount.size) {
                val newInput = Input(
                    conditions = conditions.substring(0, conditionsLen),
                    groupsReport = groupsReportCount.subList(0, reportsCount).joinToString(","),
                )

                if (reportsCount == 0) {
                    dp[newInput] = if (newInput.conditions.damaged() > 0) 0 else 1
                } else {
                    if (conditionsLen == 0) dp[newInput] = 0
                    else {
                        dp[newInput] = when (newInput.conditions.last()) {
                            '.' -> newInput.ifOperational(dp)
                            '#' -> newInput.ifDamaged(dp) { arrangement(it) }
                            '?' -> newInput.ifOperational(dp) + newInput.ifDamaged(dp) { arrangement(it) }
                            else -> error("Invalid input")
                        }
                    }
                }
            }
        }
        return dp[this]!!
    }

    fun part1(input: List<String>): Long {
        return input.fold(0) { acc, line ->
            val scanner = Scanner(line)
            val input = Input(scanner.next(), scanner.next())
            acc + input.arrangement()
        }
    }

    fun part2(input: List<String>): Long {
        return input.fold(0) { acc, line ->
            val scanner = Scanner(line)
            val conditions = scanner.next().plus("?").repeat(5).dropLast(1)
            val groupsReport = scanner.next().plus(",").repeat(5).dropLast(1)
            val input = Input(conditions, groupsReport)
            acc + input.arrangement()
        }
    }

    val testInput1 = readInput("Day12_test")
    check(part1(testInput1) == 21L)
    check(part2(testInput1) == 525152L)

    val input = readInput("Day12")
    part1(input).println()
    part2(input).println()
}
