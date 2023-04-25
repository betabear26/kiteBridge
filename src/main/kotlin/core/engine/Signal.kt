package core.engine

sealed class Signal {
    object Buy : Signal()
    object Sell : Signal()
    object Hold : Signal()
}
