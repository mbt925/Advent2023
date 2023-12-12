import java.util.*
import kotlin.Comparator

private enum class HandType { HighCard, OnePair, TwoPair, ThreeOfAKind, FullHouse, FourOfAKind, FiveOfAKind }
private enum class Card {
    Two, Three, Four, Five, Six, Seven, Eight, Nine, T, J, Q, K, A;

    companion object {
        fun from(i: Char) = when (i) {
            'A' -> A
            'K' -> K
            'Q' -> Q
            'J' -> J
            'T' -> T
            '9' -> Nine
            '8' -> Eight
            '7' -> Seven
            '6' -> Six
            '5' -> Five
            '4' -> Four
            '3' -> Three
            '2' -> Two
            else -> error("Invalid card")
        }
    }
}

private enum class CardWithJoker {
    J, Two, Three, Four, Five, Six, Seven, Eight, Nine, T, Q, K, A;

    companion object {
        fun from(i: Char) = when (i) {
            'A' -> A
            'K' -> K
            'Q' -> Q
            'T' -> T
            '9' -> Nine
            '8' -> Eight
            '7' -> Seven
            '6' -> Six
            '5' -> Five
            '4' -> Four
            '3' -> Three
            '2' -> Two
            'J' -> J
            else -> error("Invalid card")
        }
    }
}


fun main() {

    data class Hand(
        val value: String,
        val bid: Long,
    ) {
        val type: HandType by lazy {
            val uniquesCount = value.toSet().size
            val countsMap = value.groupingBy { it }.eachCount()

            when (uniquesCount) {
                5 -> HandType.HighCard
                4 -> HandType.OnePair
                3 -> {
                    if (countsMap.values.max() == 3) HandType.ThreeOfAKind
                    else HandType.TwoPair
                }

                2 -> {
                    if (countsMap.values.max() == 4) HandType.FourOfAKind
                    else HandType.FullHouse
                }

                1 -> HandType.FiveOfAKind
                else -> error("invalid count")
            }
        }
        val typeWithJoker: HandType by lazy {
            val beforeJokerCountsMap = value.groupingBy { it }.eachCount()
            val sortedCountsMap = beforeJokerCountsMap.toList().sortedByDescending { it.second }
            var maxCard = sortedCountsMap.first().first
            if (CardWithJoker.from(maxCard) == CardWithJoker.J) {
                // if J is the max, use the second max
                maxCard = if (sortedCountsMap.size > 1) sortedCountsMap[1].first
                else '2'
            }
            val valueWithJoker = value.replace('J', maxCard)

            return@lazy Hand(valueWithJoker, bid).type
        }
    }

    val comparator = object : Comparator<Hand> {
        override fun compare(o1: Hand, o2: Hand): Int {
            return if (o1.type == o2.type) {
                o1.value.forEachIndexed { index, c1 ->
                    val c2 = o2.value[index]
                    val card1 = Card.from(c1)
                    val card2 = Card.from(c2)
                    if (card1 != card2) return card1.ordinal - card2.ordinal
                }
                return 0
            } else {
                o1.type.ordinal - o2.type.ordinal
            }
        }
    }

    val comparatorWithJoker = object : Comparator<Hand> {
        override fun compare(o1: Hand, o2: Hand): Int {
            return if (o1.typeWithJoker == o2.typeWithJoker) {
                o1.value.forEachIndexed { index, c1 ->
                    val c2 = o2.value[index]
                    val card1 = CardWithJoker.from(c1)
                    val card2 = CardWithJoker.from(c2)
                    if (card1 != card2) return card1.ordinal - card2.ordinal
                }
                return 0
            } else {
                o1.typeWithJoker.ordinal - o2.typeWithJoker.ordinal
            }
        }
    }

    fun part1(input: List<String>): Long {
        val hands = input.map { line ->
            val scanner = Scanner(line)
            Hand(
                value = scanner.next(),
                bid = scanner.nextLong(),
            )
        }
        val score = hands.sortedWith(comparator).foldIndexed(0L) { index, acc, hand ->
            acc + hand.bid * (index + 1)
        }
        return score
    }

    fun part2(input: List<String>): Long {
        val hands = input.map { line ->
            val scanner = Scanner(line)
            Hand(
                value = scanner.next(),
                bid = scanner.nextLong(),
            )
        }
        val score = hands.sortedWith(comparatorWithJoker).foldIndexed(0L) { index, acc, hand ->
            acc + hand.bid * (index + 1)
        }
        return score
    }

    val testInput1 = readInput("Day07_test")
    check(part1(testInput1) == 6440L)
    check(part2(testInput1) == 5905L)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}

