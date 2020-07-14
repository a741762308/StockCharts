package com.charles.stocks.charts.utils;

import android.content.Context;
import android.util.DisplayMetrics
import android.util.TypedValue
import com.charles.stocks.charts.AppApplication

object UiUtils {
    private fun getDensity(context: Context): Float {
        return getDisplayMetrics(context).density
    }

    private fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }


    fun dip2px(dip: Float): Int {
        val scale = getDensity(AppApplication.get())
        return (dip * scale + 0.5f).toInt()
    }

    fun sp2px(sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getDisplayMetrics(AppApplication.get())).toInt()
    }

}
