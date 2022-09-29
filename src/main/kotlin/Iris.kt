class Iris(val sepalLength: Double, val petalLength: Double, val type: IrisType) {
    object Builder{
        fun parse(data: String): Iris{
            val row = data.split("\t")
            val type = when(row[2]){
                "versicolor" -> IrisType.Versicolor
                "virginica" -> IrisType.Virginica
                else -> throw Exception()
            }
            return Iris(row[0].toDouble(), row[1].toDouble(), type)
        }
    }

    override fun toString(): String {
        return "Iris{$sepalLength, $petalLength, $type}"
    }
}

enum class IrisType{
    Versicolor, Virginica
}
