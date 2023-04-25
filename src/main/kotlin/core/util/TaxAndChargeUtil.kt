package core.util

object TaxAndChargeUtil {

    private fun calculateCharges(quantity: Int, price: Double, lotSize: Int): Double {
        val brokerage = 20
        val transactionCharges = 0.0002 * price * quantity
        val clearingCharges = 0.00005 * price * quantity
        val gst = 0.18 * (brokerage + transactionCharges + clearingCharges)
        val sebiFees = 0.000001 * price * quantity
        val stampDuty = 0.002 * price * quantity / lotSize
        return brokerage + transactionCharges + clearingCharges + gst + sebiFees + stampDuty
    }

    fun breakEvenPrice(quantity: Int, price: Double, lotSize: Int): Double {
        val charges = calculateCharges(quantity, price, lotSize)
        return (price * quantity + charges) / quantity
    }


}