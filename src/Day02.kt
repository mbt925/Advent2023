import kotlin.math.max

fun main() {

    val cubes = mapOf("red" to 12, "green" to 13, "blue" to 14)

    data class Hand(
        val count: Int,
        val color: String,
    )

    data class Game(
        val hands: List<Hand>,
    )

    data class SetOfGames(
        val id: Int,
        val games: List<Game>,
    )

    fun SetOfGames.isPossible(): Boolean {
        games.forEach { game ->
            game.hands.forEach { hand ->
                if (hand.count > cubes[hand.color]!!) return false
            }
        }
        return true
    }

    fun SetOfGames.power(): Int {
        val minCubes = mutableMapOf("red" to 0, "green" to 0, "blue" to 0)

        games.forEach { game ->
            game.hands.forEach { hand ->
                val currMin = minCubes[hand.color]!!
                minCubes[hand.color] = max(currMin, hand.count)
            }
        }
        return minCubes.values.fold(1) { acc, i ->
            acc * i
        }
    }

    fun String.toSetOfGame(): SetOfGames {
        val gamePrefix = substring(0, indexOf(':')).trim()
        val gameId = gamePrefix.substring("Game ".length).toInt()
        val skipGamePrefix = substring(gamePrefix.length + 2)

        val sets = skipGamePrefix.split(";")
        return SetOfGames(
            id = gameId,
            games =
            sets.map { set ->
                val hands = set.trim().split(",")
                Game(
                    hands.map { hand ->
                        val pair = hand.trim().split(" ")
                        val count = pair[0].toInt()
                        val color = pair[1]

                        Hand(count, color)
                    }
                )
            }
        )
    }

    fun part1(input: List<String>): Int {
        return input.foldIndexed(0) { index, acc, line ->
            val setOfGames = line.toSetOfGame()
            acc + if (setOfGames.isPossible()) index + 1 else 0
        }
    }

    fun part2(input: List<String>): Int {
        return input.foldIndexed(0) { index, acc, line ->
            val setOfGames = line.toSetOfGame()
            acc + setOfGames.power()
        }
    }

    val testInput1 = readInput("Day02_test")
    check(part1(testInput1) == 8)
    check(part2(testInput1) == 2286)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
