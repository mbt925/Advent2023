import java.util.*
import kotlin.math.min

fun main() {

    data class MapRange(
        val sourceStart: Long,
        val destStart: Long,
        val len: Long,
    )

    fun map(input: Long, mapper: List<MapRange>): Long {
        mapper.forEach { range ->
            if (input >= range.sourceStart && input <= range.sourceStart + range.len) {
                return range.destStart + input - range.sourceStart
            }
        }
        return input
    }

    fun List<String>.readRanges(title: String): List<MapRange> {
        return buildList {
            this@readRanges.forEachIndexed { index, line ->
                if (line.startsWith(title)) {
                    for (nextIndex in (index + 1)..this@readRanges.lastIndex) {
                        val line = this@readRanges[nextIndex]
                        if (line.trim().isEmpty()) return@buildList
                        val parts = line.split(" ")
                        add(
                            MapRange(
                                sourceStart = parts[1].toLong(),
                                destStart = parts[0].toLong(),
                                len = parts[2].toLong(),
                            )
                        )
                    }
                }
            }
        }
    }

    fun String.seeds(): List<Long> {
        return buildList {
            val scanner = Scanner(this@seeds)
            scanner.next()
            while (scanner.hasNextLong()) add(scanner.nextLong())
        }
    }

    fun part1(input: List<String>): Long {
        val seeds = input[0].seeds()

        val mappers = buildList {
            add(input.readRanges("seed-to-soil map:"))
            add(input.readRanges("soil-to-fertilizer map:"))
            add(input.readRanges("fertilizer-to-water map:"))
            add(input.readRanges("water-to-light map:"))
            add(input.readRanges("light-to-temperature map:"))
            add(input.readRanges("temperature-to-humidity map:"))
            add(input.readRanges("humidity-to-location map:"))
        }

        val locations = seeds.map { seed ->
            mappers.fold(seed) { acc, mapper ->
                map(acc, mapper)
            }
        }

        return locations.min()
    }

    fun part2(input: List<String>): Long {
        val seeds = input[0].seeds()

        val mappers = buildList {
            add(input.readRanges("seed-to-soil map:"))
            add(input.readRanges("soil-to-fertilizer map:"))
            add(input.readRanges("fertilizer-to-water map:"))
            add(input.readRanges("water-to-light map:"))
            add(input.readRanges("light-to-temperature map:"))
            add(input.readRanges("temperature-to-humidity map:"))
            add(input.readRanges("humidity-to-location map:"))
        }

        var minLocation = Long.MAX_VALUE
        for (i in seeds.indices step 2) {
            val startSeed = seeds[i]
            val count = seeds[i + 1]

            for (seed in startSeed..(startSeed + count)) {
                val location = mappers.fold(seed) { acc, mapper ->
                    map(acc, mapper)
                }
                minLocation = min(minLocation, location)
            }
        }

        return minLocation
    }

    val testInput1 = readInput("Day05_test")
    check(part1(testInput1) == 35L)
    check(part2(testInput1) == 46L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}

