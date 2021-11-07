import java.io.*
import java.util.*
import kotlin.properties.Delegates

private const val INPUT_FILE = "in.txt"
private const val OUTPUT_FILE = "out.txt"

// найти вторые производные
// граничные условия s'(x0) = A, s'(xN) = B

/*
 * test1: y = x ^ 2, y' = 2x, y'' = 2
 * test2: y = x ^ 3, y' = 3x^2, y'' = 6x
 */
class Splines {
    /**
     * количество отрезков
     */
    private var n by Delegates.notNull<Int>()

    /**
     * краевые условия
     */
    private var A by Delegates.notNull<Float>()
    private var B by Delegates.notNull<Float>()

    /**
     * вектор аргументов
     */
    private val x = arrayListOf<Float>()

    /**
     * вектор значений
     */
    private val y = arrayListOf<Float>()

    /**
     * вектор шагов
     */
    private val h = arrayListOf<Float>()

    /**
     * диагональ, лежащая над главной
     */
    private val top = arrayListOf<Float>()

    /**
     * главная диагональ матрицы A
     */
    private val main = arrayListOf<Float>()

    /**
     * диагональ, лежащая под главной
     */
    private val bottom = arrayListOf<Float>()

    /**
     * файл для вывода
     */
    private val out = PrintWriter(FileWriter(OUTPUT_FILE))

    fun printHeaders() {
        println("Назначение программы")
        println(
            "Назначение: Вычисление значения первой" +
                    " производной таблично заданной функции в заданной точке с помощью кубического сплайна. "
        )
        println("Входные пара  метры: ")
        println("X – вектор значений аргументов в порядке возрастания (вектор узлов интерполяции); ")
        println("Y – вектор значений функции в узлах интерполяции; ")
        println("N – количество узлов интерполяции, в которых заданы значения функций;")
        println("А, В – константы краевых условий.")
        println("Выходные параметры: ")
        println("IER – индикатор ошибки:")
        println("IER = 0 – нет ошибки;")
        println("IER = 1 – кубический сплайн не может быть построен (N < 3);")
        println("IER = 2 – нарушен порядок возрастания аргумента в входном векторе X;")
        println("IER = 3 – файл не найден.")
        println("Метод: ")
        println(
            "Построение таблицы значений второй производной для таблично заданной функции f(x)" +
                    " с помощью кубического сплайна. "
        )
        println("Работу выполнил студент 7 группы: ")
        println("Добродеев Даниил Вадимович")
        println("Проверила: Шабунина Зоя Александровна")
    }

    /**
     * Главная функция
     */
    fun findSecondDerivatives() {
        val code: Int
        if (fileExists()) {
            getData()
            if (n >= 3) {
                var increase = true
                var i = 1
                h.add(0f)
                while (i <= n && increase) {
                    increase = x[i] > x[i - 1]
                    h.add(x[i] - x[i - 1])
                    i++
                }
                // проверяем, что аргументы расположены в порядке возрастания
                code = if (increase) {
                    calcSpline()
                    0
                } else
                    2
            } else
                code = 1
        } else {
            println("Файл не найден")
            code = 3
        }
        out.println("IER = $code")
        println("IER = $code")
        out.close()
    }


    private fun fileExists(): Boolean {
        try {
            Scanner(File(INPUT_FILE))
        } catch (ex: FileNotFoundException) {
            return false
        } catch (ex: IOException) {
            return false
        }
        return true
    }

    private fun getData() {
        val scanner = Scanner(File(INPUT_FILE))
        n = scanner.nextInt()
        println("Исходные данные:") // выводим на экран
        println("n = $n")
        for (i in 0..n) // вводим n+1 значений функции
        {
            x.add(scanner.next().toFloat())
            println("x[$i] = ${x[i]}")
        }
        for (i in 0..n) // вводим n+1 значений функции
        {
            y.add(scanner.next().toFloat())
            println("y[$i] = ${y[i]}")
        }
        println()
        A = scanner.next().toFloat()
        B = scanner.next().toFloat()

        scanner.close() // закрываем файл
    }

    /**
     * Функция для вычисления кубического сплайна
     */
    private fun calcSpline() {
        createMatrix() // создаем матрицу для ее дальнейшего решения и проверки точности решения
        val functions = mutableListOf<Float>()
        functions.add(3 / h[1] * ((y[1] - y[0]) / h[1] - A)) // задаем правые части
        for (i in 1 until n)
            functions.add(6 * ((y[i + 1] - y[i]) / h[i + 1] - (y[i] - y[i - 1]) / h[i]))
        functions.add(3 / h[n] * (B - (y[n] - y[n - 1]) / h[n]))
        val c = findSolution(functions) // ищем решение
        for (i in 0..n)
            out.print(x[i]).also { out.print("\t") }
        out.println()
        for (i in 0..n)
            out.print(y[i]).also { out.print("\t") }
        out.println()
        for (i in 0..n)
            out.print(c[i]).also { out.print("\t") }
        out.println()
    }

    /**
     * Функция создания матрицы уравнения граничных условий
     */
    private fun createMatrix() {
        main.add(1f)
        top.add(0.5f)
        bottom.add(h[0])
        for (i in 1 until n) {
            bottom.add(h[i])
            main.add(2f * (h[i] + h[i + 1]))
            top.add(h[i + 1])
        }
        main.add(1f)
        bottom.add(0.5f)
    }

    /**
     * функция нахождения решения методом прогонки
     */
    private fun findSolution(functions: List<Float>): List<Float> {
        val result = mutableListOf<Float>()
        val alpha = mutableListOf<Float>()
        val beta = mutableListOf<Float>()
        alpha.add(-top[0] / main[0])
        beta.add(functions[0] / main[0])
        for (i in 1 until n) {
            val bottom = main[i] + bottom[i] * alpha[i - 1]
            alpha.add(-top[i] / bottom)
            beta.add((functions[i] - this.bottom[i] * beta[i - 1]) / bottom)
        }
        beta.add((functions[n] - bottom[n] * beta[n - 1]) / (main[n] + bottom[n] * alpha[n - 1]))
        result.add(beta[n])
        for (i in (n - 1) downTo 0) {
            result.add(alpha[i] * result[n - i - 1] + beta[i])
        }
        return result.reversed()
    }
}