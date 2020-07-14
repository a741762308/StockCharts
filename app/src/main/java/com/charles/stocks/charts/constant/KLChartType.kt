package com.charles.stocks.charts.constant

import androidx.annotation.StringDef


object KLChartType {
    const val KL_TYPE_VOLUME = "volume"

    @StringDef(KL_TYPE_VOLUME)
    @Retention(AnnotationRetention.SOURCE)
    annotation class KLTechType {

    }

    annotation class KLCoreType {

    }
}