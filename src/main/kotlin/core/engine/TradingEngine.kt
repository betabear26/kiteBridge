package core.engine

import core.engine.strategy.TradingStrategy

class TradingEngine(
    private val strategy: TradingStrategy,
    private val communicator: TradeCommunicator
) {

    private var ticks: MutableList<Double> = mutableListOf()

    fun addTick(tick: Double) {
        ticks.add(tick)
        when(strategy.onTick(ticks)){
            Signal.Buy -> buy()
            Signal.Sell -> sell()
            Signal.Hold -> hold()
        }
    }

    private fun buy() {
        communicator.buy()
    }

    private fun sell() {
        communicator.sell()
    }

    private fun hold() {
        communicator.hold()
    }

}