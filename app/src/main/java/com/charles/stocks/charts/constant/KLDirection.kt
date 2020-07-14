package com.charles.stocks.charts.constant

import androidx.annotation.IntDef
import com.charles.stocks.charts.constant.KLDirection.Companion.BACKWARD
import com.charles.stocks.charts.constant.KLDirection.Companion.FORWARD
import com.charles.stocks.charts.constant.KLDirection.Companion.NONE


@IntDef(NONE, FORWARD, BACKWARD)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class KLDirection {
    companion object {
        const val NONE = 0

        const val FORWARD = 1

        const val BACKWARD = 2
    }

}