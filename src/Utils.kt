import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.absoluteValue

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

inline fun String.forEachIndexedReversed(action: (index: Int, c: Char) -> Unit) {
    for (index in lastIndex downTo 0) action(index, this[index])
}

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

fun gaussElimination(matrix: Array<DoubleArray>): DoubleArray {
    val n = matrix.size

    for (i in 0 until n) {
        // Find the row with the maximum first element and swap rows
        var maxRow = i
        for (k in i + 1 until n) {
            if (matrix[k][i].absoluteValue > matrix[maxRow][i].absoluteValue) {
                maxRow = k
            }
        }
        val temp = matrix[i]
        matrix[i] = matrix[maxRow]
        matrix[maxRow] = temp

        // Make the diagonal element 1
        val divisor = matrix[i][i]
        for (j in i until n + 1) {
            matrix[i][j] /= divisor
        }

        // Make the other elements in the column 0
        for (k in 0 until n) {
            if (k != i) {
                val factor = matrix[k][i]
                for (j in i until n + 1) {
                    matrix[k][j] -= factor * matrix[i][j]
                }
            }
        }
    }

    // Extract the solution
    val solution = DoubleArray(n)
    for (i in 0 until n) {
        solution[i] = matrix[i][n]
    }

    return solution
}

fun findParabolaEquation(X: List<Double>, Y: List<Double>): Triple<Double, Double, Double> {
    require(X.size == 3)
    require(X.size == Y.size)

    // Extract coordinates of the points
    val (x1, x2, x3) = X
    val (y1, y2, y3) = Y

    // Build the system of equations
    val matrix = arrayOf(
        doubleArrayOf(x1 * x1, x1, 1.0, y1),
        doubleArrayOf(x2 * x2, x2, 1.0, y2),
        doubleArrayOf(x3 * x3, x3, 1.0, y3)
    )
    // Solve the system of equations
    val solution = gaussElimination(matrix)
    // Extract the coefficients
    return Triple(solution[0], solution[1], solution[2])
}