package core.engine.strategy

import core.engine.Signal

interface TradingStrategy {
    fun onTick(ticks: List<Double>): Signal
}