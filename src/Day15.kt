import Operation.Add
import Operation.Remove

enum class Operation { Remove, Add }

fun main() {

    fun String.hash(): Int {
        return fold(0) { acc, c ->
            (acc + c.code) * 17 % 256
        }
    }

    data class Lens(
        val label: String,
        val focalLength: Int,
    )

    data class Instruction(
        val lens: Lens,
        val operation: Operation,
    ) {
        val boxNumber by lazy { lens.label.hash() }
    }

    fun MutableMap<String, Lens>.focusingPower(box: Int): Long {
        return values.foldIndexed(0L) { index, acc, lens ->
            acc + (box + 1) * (index + 1) * lens.focalLength
        }
    }

    fun List<Instruction>.apply(): Long {
        val boxes = List<MutableMap<String, Lens>>(256) { LinkedHashMap() }
        forEach { instruction ->
            val box = boxes[instruction.boxNumber]
            when (instruction.operation) {
                Remove -> box.remove(instruction.lens.label)
                Add -> box[instruction.lens.label] = instruction.lens
            }
        }
        return boxes.foldIndexed(0L) { index, acc, box ->
            acc + box.focusingPower(index)
        }
    }

    fun List<String>.parseToParts() = joinToString(",")
        .split(",")
        .filterNot { it.isBlank() }

    fun List<String>.parseToInstructions(): List<Instruction> {
        val regex = Regex("[=-]")
        return parseToParts().map {
            val split = it.split(regex)
            val operation = if (it[split[0].length] == '=') Add else Remove
            Instruction(
                lens = Lens(
                    label = split[0],
                    focalLength = if (operation == Add) split[1].toInt() else -1,
                ),
                operation = operation,
            )
        }
    }

    fun part1(input: List<String>): Long {
        val list = input.parseToParts()
        return list.fold(0L) { acc, word ->
            acc + word.hash()
        }
    }

    fun part2(input: List<String>): Long {
        val list = input.parseToInstructions()
        return list.apply()
    }

    val testInput1 = readInput("Day15_test")
    check(part1(testInput1) == 1320L)
    check(part2(testInput1) == 145L)

    val input = readInput("Day15")
    part1(input).println()
    part2(input).println()
}
