class LinearFunction(val a: Double, val b: Double) {

    fun getY(x: Double) = a * x + b

    override fun toString(): String {
        return "y = ${a}x + $b"
    }
}