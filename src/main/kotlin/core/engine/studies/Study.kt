package core.engine.studies

interface Study {

    fun update(price: Double)

    fun getValue(): Double?
}