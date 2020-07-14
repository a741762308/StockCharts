package com.charles.stocks.charts.utils

class DomainUtils {
    companion object {
        private  val PRICE_BASE_VALUES = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000)

        fun priceBaseValue(priceBase: Int): Int {
            return if (priceBase > PRICE_BASE_VALUES.size - 1) {
                PRICE_BASE_VALUES[0]
            } else PRICE_BASE_VALUES[priceBase]
        }
    }

}