import java.util.*

fun main() {

    data class Race(
        val time: Long,
        val record: Long,
    ) {
        fun howManyWins(): Int {
            val halfTime = time / 2
            var count = 0
            for (t in halfTime..time) {
                val distance = t * (time - t)
                if (distance > record) count++
                else break
            }
            return count * 2 - if (time % 2L == 0L) 1 else 2
        }
    }

    fun List<String>.races(): List<Race> {
        return buildList {
            val timeScanner = Scanner(this@races[0])
            val distanceScanner = Scanner(this@races[1])
            timeScanner.next()
            distanceScanner.next()
            while (timeScanner.hasNextLong()) {
                add(
                    Race(
                        time = timeScanner.nextLong(),
                        record = distanceScanner.nextLong(),
                    )
                )
            }
        }
    }

    fun List<Race>.merge(): Race {
        var time = ""
        var record = ""

        forEach { race ->
            time += race.time.toString()
            record += race.record.toString()
        }
        return Race(
            time = time.toLong(),
            record = record.toLong(),
        )
    }

    fun part1(input: List<String>): Int {
        val races = input.races()
        return races.fold(1) { acc, race ->
            acc * race.howManyWins()
        }
    }

    fun part2(input: List<String>): Int {
        val race = input.races().merge()
        return race.howManyWins()
    }

    val testInput1 = readInput("Day06_test")
    check(part1(testInput1) == 288)
    check(part2(testInput1) == 71503)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}

