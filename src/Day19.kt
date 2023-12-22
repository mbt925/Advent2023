import FilterOperator.Greater
import FilterOperator.Smaller
import WorkflowOutput.*
import java.util.*

enum class FilterOperator {
    Smaller, Greater;

    fun inverse() = when (this) {
        Smaller -> Greater
        Greater -> Smaller
    }

    companion object {
        fun from(o: Char) = when (o) {
            '<' -> Smaller
            '>' -> Greater
            else -> error("Invalid input: $o")
        }
    }
}

sealed interface WorkflowOutput {
    data object Accept : WorkflowOutput
    data object Reject : WorkflowOutput
    data class AnotherWorkflow(val name: String) : WorkflowOutput
}

fun main() {

    data class Attr(
        val name: String,
        val value: Int,
    )

    data class Range(
        val attr: Attr,
        val operator: FilterOperator,
    ) {
        fun inverse() = Range(
            attr = attr.copy(value = if (operator == Smaller) attr.value - 1 else attr.value + 1),
            operator = operator.inverse(),
        )
    }

    data class Part(
        val attrs: Map<String, Attr>,
    ) {
        fun getAttr(name: String) = attrs[name]!!
        val rating: Int by lazy { attrs.values.fold(0) { acc, a -> acc + a.value } }
    }

    data class Filter(
        val attr: String,
        val operator: FilterOperator,
        val value: Int,
        val destination: String,
    ) {
        fun pass(part: Part): Boolean {
            return when (operator) {
                Smaller -> part.getAttr(attr).value < value
                Greater -> part.getAttr(attr).value > value
            }
        }

        fun range() = Range(Attr(attr, value), operator)
    }

    fun String.toOutput() = when (this) {
        "A" -> Accept
        "R" -> Reject
        else -> AnotherWorkflow(this)
    }

    data class Workflow(
        val name: String,
        val filters: List<Filter>,
        val elseDestination: String,
    ) {
        fun apply(part: Part): WorkflowOutput {
            filters.forEach { filter ->
                if (filter.pass(part)) return filter.destination.toOutput()
            }
            return elseDestination.toOutput()
        }
    }

    data class RangeSet(
        val workflow: Workflow,
        val ranges: List<Range>, // a sample range set: (>10), (>20), (<50)
    ) {
        // compute intersection of all ranges, and multiply
        fun combinations(): Long {
            val byAttr = ranges.groupBy { it.attr.name }
            val ranges = byAttr.map { attr ->
                val byOperator = attr.value.groupBy { it.operator }
                val start = byOperator[Greater]!!.maxBy { it.attr.value }.attr.value
                val end = byOperator[Smaller]!!.minBy { it.attr.value }.attr.value

                end - start - 1
            }
            return ranges.fold(1L) { acc, n -> acc * n }
        }
    }

    data class System(
        val workflows: Map<String, Workflow>,
        val parts: List<Part>,
    ) {
        fun run(): Int {
            val accepted = mutableListOf<Part>()
            parts.forEach { part ->
                var currWorkflow = workflows["in"]!!
                while (true) {
                    when (val output = currWorkflow.apply(part)) {
                        Accept -> {
                            accepted.add(part)
                            break
                        }
                        Reject -> break
                        is AnotherWorkflow -> currWorkflow = workflows[output.name]!!
                    }
                }
            }
            return accepted.fold(0) { acc, p -> acc + p.rating }
        }

        private fun getInitialRanges(): RangeSet {
            return RangeSet(
                workflow = workflows["in"]!!,
                ranges = parts[0].attrs.values.flatMap {
                    listOf(
                        Range(
                            attr = Attr(name = it.name, value = 0),
                            operator = Greater,
                        ),
                        Range(
                            attr = Attr(name = it.name, value = 4001),
                            operator = Smaller,
                        )
                    )
                }
            )
        }

        fun possibleCombinations(): Long {
            val accepted = mutableListOf<RangeSet>()
            val queue = LinkedList<RangeSet>()
            queue.add(getInitialRanges())

            while (queue.isNotEmpty()) {
                val curr = queue.remove()
                var newRange = curr.ranges
                curr.workflow.filters.forEach { filter ->
                    val range = filter.range()
                    newRange = when (val output = filter.destination.toOutput()) {
                        Accept -> {
                            accepted.add(curr.copy(ranges = newRange + range))
                            newRange + range.inverse()
                        }

                        is AnotherWorkflow -> {
                            queue.add(RangeSet(workflows[output.name]!!, newRange + range))
                            newRange + range.inverse()
                        }

                        Reject -> newRange + range.inverse()
                    }
                }
                when (val output = curr.workflow.elseDestination.toOutput()) {
                    Accept -> accepted.add(curr.copy(ranges = newRange))
                    is AnotherWorkflow -> queue.add(RangeSet(workflows[output.name]!!, newRange))
                    Reject -> Unit
                }
            }
            return accepted.fold(0L) { acc, rangeSet ->
                acc + rangeSet.combinations()
            }
        }
    }

    val workFlowNameRegex = Regex("""^(\w+)\{""")
    val filtersRegex = Regex("""([a-z])([<>])(\d+):(\w+),""")
    val elseRegex = Regex(""",(\w+)}""")

    fun String.toWorkflow(): Workflow {
        val (name) = workFlowNameRegex.find(this)!!.destructured
        val (elseDestination) = elseRegex.find(this)!!.destructured
        val filtersResult = filtersRegex.findAll(this)
        val filters = buildList {
            filtersResult.forEach {
                val (attr, op, value, dest) = it.destructured
                add(
                    Filter(
                        attr = attr,
                        operator = FilterOperator.from(op.first()),
                        value = value.toInt(),
                        destination = dest,
                    )
                )
            }
        }
        return Workflow(
            name = name,
            filters = filters,
            elseDestination = elseDestination,
        )
    }

    val partRegex = Regex("""([a-z])=(\d+)[,}]""")

    fun String.toPart(): Part {
        val partsResult = partRegex.findAll(this)
        val attrs = buildList {
            partsResult.forEach {
                val (n, v) = it.destructured
                add(
                    Attr(
                        name = n,
                        value = v.toInt(),
                    )
                )
            }
        }
        return Part(attrs.associateBy { it.name })
    }

    fun part1(input: List<String>): Int {
        val workflows = mutableListOf<Workflow>()
        val parts = mutableListOf<Part>()

        var isPart = false
        input.forEach { line ->
            if (line.isBlank()) isPart = true
            else {
                if (isPart) parts.add(line.toPart())
                else workflows.add(line.toWorkflow())
            }
        }

        return System(workflows.associateBy { it.name }, parts).run()
    }

    fun part2(input: List<String>): Long {
        val workflows = mutableListOf<Workflow>()
        val parts = mutableListOf<Part>()

        var isPart = false
        input.forEach { line ->
            if (line.isBlank()) isPart = true
            else {
                if (isPart) parts.add(line.toPart())
                else workflows.add(line.toWorkflow())
            }
        }

        return System(workflows.associateBy { it.name }, parts).possibleCombinations()
    }

    val testInput1 = readInput("Day19_test")
    check(part1(testInput1) == 19114)
    check(part2(testInput1) == 167409079868000L)

    val input = readInput("Day19")
    part1(input).println()
    part2(input).println()
}
