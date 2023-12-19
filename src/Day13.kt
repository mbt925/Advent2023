import java.util.BitSet

fun main() {

    fun List<String>.toHorizontalBitSet(): List<BitSet> {
        return map {
            val bitSet = BitSet(it.length)
            it.forEachIndexed { index, c -> bitSet.set(index, c == '#') }
            bitSet
        }
    }

    fun List<String>.toVerticalBitSet(): List<BitSet> {
        val rows = size
        val cols = this[0].length
        return buildList {
            for (c in 0..<cols) {
                val bitSet = BitSet(rows)
                for (r in 0..<rows) {
                    bitSet.set(r, this@toVerticalBitSet[r][c] == '#')
                }
                add(bitSet)
            }
        }
    }

    // Equality: XOR == 0
    // One bit difference: XOR has only one bit set
    fun List<BitSet>.verifyReflection(line: Int, smudge: Boolean): Boolean {
        val rows = size
        var reflection = 2 + 1
        var smudges = 0

        for (r in line - 1 downTo 0) {
            val reflectedRow = r + reflection
            if (reflectedRow >= rows) break

            val xorResult = BitSet()
            xorResult.or(this[r])
            xorResult.xor(this[reflectedRow])

            val diff = xorResult.cardinality()
            if (diff == 1) smudges++
            else if (diff > 1) return false

            if (smudge && smudges > 1) return false
            reflection += 2
        }
        return (smudges == 0 && !smudge) || (smudges == 1 && smudge)
    }

    fun List<BitSet>.findReflection(smudge: Boolean): Int {
        val rows = size
        // find reflection line
        for (r in 0..<rows - 1) {
            val xorResult = BitSet()
            xorResult.or(this[r])
            xorResult.xor(this[r+1])

            val diff = xorResult.cardinality()
            if (diff == 0 && verifyReflection(r, smudge)) return r + 1
            if (smudge && diff == 1 && verifyReflection(r, false)) return r + 1
        }
        return 0
    }

    fun part(input: List<String>, smudge: Boolean): Long {
        var sum = 0L
        var lastStart = 0
        for (i in input.indices) {
            if (input[i].isBlank()) {
                val subList = input.subList(lastStart, i)
                val horizontalBitSet = subList.toHorizontalBitSet()
                val verticalBitSet = subList.toVerticalBitSet()

                val hor = horizontalBitSet.findReflection(smudge) * 100L
                sum += if (hor == 0L) verticalBitSet.findReflection(smudge).toLong() else hor

                lastStart = i + 1
            }
        }
        check(lastStart == input.size)
        return sum
    }

    val testInput1 = readInput("Day13_test")
    check(part(testInput1, false) == 405L)
    check(part(testInput1, true) == 400L)

    val input = readInput("Day13")
    part(input, false).println()
    part(input, true).println()
}
