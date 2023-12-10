import java.util.*
import kotlin.math.pow

fun main() {

    data class Number(
        val value: Int,
        val seen: Boolean,
    )

    data class Point(
        val value: Int,
        val winningNumbers: Int,
    )

    data class Card(
        val id: Int,
        val winningNumbers: List<Int>,
        val myNumbers: List<Int>,
    ) {
        fun points(): Point {
            val winningNumbersMap = mutableMapOf<Int, Number>()

            for (num in winningNumbers) {
                val number = Number(num, false)
                winningNumbersMap[num] = number
            }

            var count = 0.0
            for (num in myNumbers) {
                if (winningNumbersMap.containsKey(num) && !winningNumbersMap[num]!!.seen) {
                    count++
                    winningNumbersMap[num] = winningNumbersMap[num]!!.copy(seen = true)
                }
            }

            return Point(
                value = (2.0.pow(count - 1).toInt().takeIf { count > 0 } ?: 0),
                winningNumbers = count.toInt(),
            )
        }
    }

    fun String.toCard(): Card {
        val cardPrefix = substring(0, indexOf(':')).trim()
        val cardId = cardPrefix.substring("Card ".length).trim().toInt()
        val skipCardPrefix = substring(cardPrefix.length + 2)

        val sets = skipCardPrefix.split("|")
        val winningNumbersScanner = Scanner(sets[0])
        val myNumbersScanner = Scanner(sets[1])

        val winningNumbers = buildList {
            while (winningNumbersScanner.hasNext()) add(winningNumbersScanner.nextInt())
        }

        val myNumbers = buildList {
            while (myNumbersScanner.hasNext()) add(myNumbersScanner.nextInt())
        }

        return Card(
            id = cardId,
            winningNumbers = winningNumbers,
            myNumbers = myNumbers,
        )
    }

    fun part1(input: List<String>): Int {
        return input.fold(0) { acc, line ->
            val card = line.toCard()
            acc + card.points().value
        }
    }

    fun part2(input: List<String>): Int {
        val copiesMap = mutableMapOf<Int, Int>()

        return input.fold(0) { acc, line ->
            val card = line.toCard()
            val points = card.points()
            val copies = copiesMap.getOrDefault(card.id, 1)

            repeat(points.winningNumbers) {
                val id = card.id + it + 1
                copiesMap[id] = copiesMap.getOrDefault(id, 1) + copies
            }
            acc + copies
        }
    }

    val testInput1 = readInput("Day04_test")
    check(part1(testInput1) == 13)
    check(part2(testInput1) == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
