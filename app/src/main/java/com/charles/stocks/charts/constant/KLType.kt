package com.charles.stocks.charts.constant

import androidx.annotation.IntDef
import com.charles.stocks.charts.constant.KLType.Companion.DAY
import com.charles.stocks.charts.constant.KLType.Companion.MIN1
import com.charles.stocks.charts.constant.KLType.Companion.MIN10
import com.charles.stocks.charts.constant.KLType.Companion.MIN15
import com.charles.stocks.charts.constant.KLType.Companion.MIN30
import com.charles.stocks.charts.constant.KLType.Companion.MIN5
import com.charles.stocks.charts.constant.KLType.Companion.MIN60
import com.charles.stocks.charts.constant.KLType.Companion.MNT12
import com.charles.stocks.charts.constant.KLType.Companion.MNT3
import com.charles.stocks.charts.constant.KLType.Companion.MNT6
import com.charles.stocks.charts.constant.KLType.Companion.MONTH
import com.charles.stocks.charts.constant.KLType.Companion.NONE
import com.charles.stocks.charts.constant.KLType.Companion.WEEK

@IntDef(NONE, MIN1, MIN5, MIN10, MIN15, MIN30, MIN60, DAY, WEEK, MONTH, MNT3, MNT6, MNT12)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.SOURCE)
annotation class KLType {
    companion object {
        const val NONE = 0

        const val MIN1 = 1

        const val MIN5 = 2

        const val MIN10 = 3

        const val MIN15 = 4

        const val MIN30 = 5

        const val MIN60 = 6

        const val DAY = 7

        const val WEEK = 8

        const val MONTH = 9

        const val MNT3 = 10

        const val MNT6 = 11

        const val MNT12 = 12
    }
}