import org.jfree.chart.ChartColor
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BorderLayout
import java.awt.Color
import java.io.File
import javax.swing.JFrame


fun main(args: Array<String>) {
    // 教師データcsvファイルからIrisインスタンスを生成
    var file = File("src/main/resources/iris_teacher.csv")
    val teacherIrisList = mutableListOf<Iris>()
    file.readLines().forEach {
        teacherIrisList.add(Iris.Builder.parse(it))
    }
    println("教師データ $teacherIrisList")

    // 判定直線を求める
    val judgeLine = getIrisJudgeLine(
        teacherIrisList,
        1000,
        1000,
        0,
        0,
        5000,
        5000,
    )


    // テストデータcsvファイルからIrisインスタンスを生成
    file = File("src/main/resources/iris_test.csv")
    val testIrisList = mutableListOf<Iris>()
    file.readLines().forEach {
        testIrisList.add(Iris.Builder.parse(it))
    }
    println()
    println("テストデータ $testIrisList")

    // 上で求めた判定直線を用いてテストデータを判別し、誤判定の回数を表示する
    print("テストデータの誤判定数 : ${countMissJudge(testIrisList, judgeLine)}")

}

fun getIrisJudgeLine(
    irisList: List<Iris>,

// 傾きaと切片bを増やす段階。(1/step)ずつ傾きと切片が増加する
    aIncreaseStep: Int,
    bIncreaseStep: Int,

// 傾きと切片をどの範囲で動かすか
    aStart: Int,
    bStart: Int,
    aEnd: Int,
    bEnd: Int,
): LinearFunction {

    // グラフ初期化
    val chartWindow = ChartPane(irisList)
    chartWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    chartWindow.setBounds(10, 10, 500, 500)
    chartWindow.title = "iris classification"
    chartWindow.isVisible = true


    // 誤判定数の最小値
    var minMistake: Int? = null
    // 誤判定数が最小の時の判定直線
    var judgeLineInMinMistake: LinearFunction? = null

    // sepalLengthをx軸,petalLengthをy軸とする
    // 傾きa,切片bを動かして、誤判定数が最小となるようなaとbを求める
    for (a in aStart until aEnd) {
        for (b in bStart until bEnd) {

            val judgeLine = LinearFunction(a.toDouble() / aIncreaseStep, b.toDouble() / bIncreaseStep)

            val mistakeCount = countMissJudge(irisList, judgeLine)

            // 誤判定数最小値を算出
            if (minMistake == null || minMistake > mistakeCount) {
                minMistake = mistakeCount
                judgeLineInMinMistake = judgeLine

                println("$judgeLineInMinMistake   (誤判定数 : $minMistake)")
                chartWindow.drawLine(4.0, judgeLineInMinMistake.getY(4.0), 8.0, judgeLineInMinMistake.getY(8.0))
            }
        }
    }

    println("最適判定直線")
    println("$judgeLineInMinMistake   (誤判定数 : $minMistake)")

    return judgeLineInMinMistake!!
}

fun countMissJudge(irisList: List<Iris>, judgeLine: LinearFunction): Int {

    var mistakeCount = 0
    for (iris in irisList) {
        // 直線 a * sepal + b よりも上にある場合virginica、下にある場合versicolor
        val prediction =
            if (judgeLine.getY(iris.sepalLength) <= iris.petalLength) {
                IrisType.Virginica
            } else {
                IrisType.Versicolor
            }

//        println("${if(iris.type != prediction){"*"}else{" "}}  y = ${a.toDouble()/aIncreaseStep}x + ${b.toDouble()/bIncreaseStep}   (x = ${iris.sepalLength}, y = ${iris.petalLength})")

        // 予想と正しい答えが違った場合カウントを増やす
        if (iris.type != prediction) {
            mistakeCount++
        }
    }
    return mistakeCount
}

class ChartPane(irisList: List<Iris>) : JFrame() {
    private var chart: JFreeChart
    private var chartPanel: ChartPanel
    private val chartData = XYSeriesCollection()
    private val line = XYSeries("判定直線")

    init {
        // show graph
        val chartData = XYSeriesCollection()
        val versicolor = XYSeries("versicolor")
        val virginica = XYSeries("virginica")
        irisList.forEach {
            if (it.type == IrisType.Versicolor) {
                versicolor.add(it.sepalLength, it.petalLength)
            } else {
                virginica.add(it.sepalLength, it.petalLength)
            }
        }
        chartData.addSeries(versicolor)
        chartData.addSeries(virginica)
        chart = ChartFactory.createScatterPlot("iris classification", "sepal length", "petal length", chartData)
        chartPanel = ChartPanel(chart)
        contentPane.add(chartPanel, BorderLayout.CENTER)

    }

    fun drawLine(startX: Double, startY: Double, endX: Double, endY: Double) {

        chartData.removeSeries(line)
        line.clear()
//        line.add(startX, startY)
//        line.add(endX, endY)

        chartData.addSeries(line)
        val linePlot: XYPlot = chart.xyPlot
        linePlot.backgroundPaint = Color.white

        linePlot.setDataset(1, chartData)
        linePlot.mapDatasetToRangeAxis(1, 0)

        val render = XYLineAndShapeRenderer()
        linePlot.setRenderer(1, render)
        render.setSeriesPaint(0, ChartColor.BLACK)
        linePlot.datasetRenderingOrder = DatasetRenderingOrder.REVERSE
    }
}