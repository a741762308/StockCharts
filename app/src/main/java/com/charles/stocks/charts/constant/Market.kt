package com.charles.stocks.charts.constant

import androidx.annotation.StringDef
import com.charles.stocks.charts.constant.Market.Companion.HK
import com.charles.stocks.charts.constant.Market.Companion.HS
import com.charles.stocks.charts.constant.Market.Companion.SH
import com.charles.stocks.charts.constant.Market.Companion.SZ
import com.charles.stocks.charts.constant.Market.Companion.US

@StringDef(HS, SH, SZ, HK, US)
@Retention(AnnotationRetention.SOURCE)
annotation class Market {
    companion object {
        const val HS = "hs"
        const val SH = "sh"
        const val SZ = "sz"
        const val HK = "hk"
        const val US = "us"
    }
}