import ComModule.*
import Pulse.High
import Pulse.Low
import java.util.LinkedList

enum class Pulse { Low, High }

sealed interface ComModule {

    val name: String
    fun inputConnection(connections: Set<String>) = Unit
    fun pulse(sender: String, pulse: Pulse): Pulse?

    data class FlipFlop(
        override val name: String,
    ) : ComModule {
        private var on = false

        override fun inputConnection(connections: Set<String>) {
            on = false
        }

        override fun pulse(sender: String, pulse: Pulse): Pulse? {
            return when (pulse) {
                Low -> {
                    if (on) {
                        on = false
                        Low
                    } else {
                        on = true
                        High
                    }
                }

                High -> null
            }
        }
    }

    data class Conjunction(
        override val name: String,
    ) : ComModule {
        private val latestPulses = mutableMapOf<String, Pulse>()
        override fun inputConnection(connections: Set<String>) {
            latestPulses.clear()
            connections.forEach { latestPulses[it] = Low }
        }

        override fun pulse(sender: String, pulse: Pulse): Pulse {
            latestPulses[sender] = pulse
            val allHigh = latestPulses.values.all { it == High }
            return if (allHigh) Low else High
        }

    }

    data class Broadcast(
        override val name: String,
    ) : ComModule {
        override fun pulse(sender: String, pulse: Pulse): Pulse = pulse
    }
}

fun main() {

    fun greatestCommonDivisor(a: Long, b: Long): Long {
        return if (b == 0L) a else greatestCommonDivisor(b, a % b)
    }

    fun leastCommonMultiple(a: Long, b: Long): Long {
        return if (a == 0L || b == 0L) 0 else (a * b) / greatestCommonDivisor(a, b)
    }

    fun leastCommonMultiple(numbers: List<Int>): Long {
        return numbers.fold(1L) { acc, num ->
            leastCommonMultiple(acc, num.toLong())
        }
    }

    data class Connection(
        val fromModule: ComModule,
        val to: List<String>,
    )

    data class PulseCommand(
        val pulse: Pulse,
        val from: String,
        val to: String,
    )

    data class Machine(
        val connections: List<Connection>,
    ) {
        val connectionsMap = mutableMapOf<String, Connection>()
        val incomingConnectionsMap = mutableMapOf<String, MutableSet<String>>()

        private fun init() {
            incomingConnectionsMap.clear()
            connectionsMap.clear()

            connections.forEach { c ->
                connectionsMap[c.fromModule.name] = c

                c.to.forEach { connection ->
                    incomingConnectionsMap.putIfAbsent(connection, mutableSetOf())
                    incomingConnectionsMap[connection]!!.add(c.fromModule.name)
                }
            }
            connections.forEach {
                it.fromModule.inputConnection(incomingConnectionsMap.getOrDefault(it.fromModule.name, emptySet()))
            }
        }

        fun run(count: Int): Long {
            init()

            val queue = LinkedList<PulseCommand>()
            val countsMap = mutableMapOf<Pulse, Long>()
            countsMap[Low] = 0
            countsMap[High] = 0

            repeat(count) {
                queue.add(PulseCommand(pulse = Low, from = "button", to = "broadcaster"))

                while (queue.isNotEmpty()) {
                    val command = queue.remove()
                    countsMap[command.pulse] = countsMap[command.pulse]!! + 1

                    val connection = connectionsMap[command.to] ?: continue
                    val module = connection.fromModule

                    val output = module.pulse(command.from, command.pulse)
                    if (output != null) {
                        connection.to.forEach {
                            queue.add(PulseCommand(pulse = output, from = module.name, to = it))
                        }
                    }
                }
            }
            return countsMap.values.fold(1L) { acc, c -> acc * c }
        }

        fun buttonPressUntilLowPulseToRx(): Long {
            // mg -> rx
            // jg, rh, jm, hf -> mg
            // 4 conjunctions must emit high, so mg emits low to rx
            val targets = listOf("jg", "rh", "jm", "hf")
            val intervals = targets.map { buttonPressUntilHighPulse(it) }
            return leastCommonMultiple(intervals)
        }

        fun buttonPressUntilHighPulse(target: String): Int {
            init()

            val queue = LinkedList<PulseCommand>()
            var buttonPresses = 0
            while (true) {
                queue.add(PulseCommand(pulse = Low, from = "button", to = "broadcaster"))
                buttonPresses++

                while (queue.isNotEmpty()) {
                    val command = queue.remove()
                    if (command.from == target && command.pulse == High) return buttonPresses

                    val connection = connectionsMap[command.to] ?: continue
                    val module = connection.fromModule

                    val output = module.pulse(command.from, command.pulse)
                    if (output != null) {
                        connection.to.forEach {
                            queue.add(PulseCommand(pulse = output, from = module.name, to = it))
                        }
                    }
                }
            }
        }
    }

    fun String.toConnection(): Connection {
        val split = split("->")
        val module = split[0].trim()
        val cons = split[1].trim().split(",")

        val comModule = when (module.first()) {
            '%' -> FlipFlop(module.drop(1))
            '&' -> Conjunction(module.drop(1))
            else -> if (module == "broadcaster") Broadcast(module) else error("Invalid module type: $module")
        }
        return Connection(comModule, cons.map { it.trim() })
    }

    fun List<String>.toMachine(): Machine {
        val connections = map { it.toConnection() }
        return Machine(connections)
    }

    fun part1(input: List<String>): Long {
        return input.toMachine().run(1000)
    }

    fun part2(input: List<String>): Long {
        return input.toMachine().buttonPressUntilLowPulseToRx()
    }

    val testInput1 = readInput("Day20_test")
    val testInput2 = readInput("Day20_test2")
    check(part1(testInput1) == 32000000L)
    check(part1(testInput2) == 11687500L)

    val input = readInput("Day20")
    part1(input).println()
    part2(input).println()
}
