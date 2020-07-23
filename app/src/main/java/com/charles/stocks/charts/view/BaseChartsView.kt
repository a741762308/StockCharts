package com.charles.stocks.charts.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import com.charles.stocks.charts.utils.UiUtils

abstract class BaseChartsView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    protected val mCrossTipRect = Rect()
    protected val mCrossTipWidth = UiUtils.dip2px(100f)
    protected val mCrossTipPadding = UiUtils.dip2px(2f)
    protected val mCrossTipItemHeight = UiUtils.dip2px(14f)

    protected var mIsPointDown = false
    protected var mPointDownX = 0f
    protected var mPointDownY = 0f

    protected var mCrossLineIndex = -1
    protected var mCrossLineX = 0f
    protected var mCrossLineY = 0f
    protected var mIsCrossLineEnable = false
    protected var mIsSupportCrossLine = true
    protected var mIsAutoHideCrossLine = true

    protected val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    @Retention(AnnotationRetention.SOURCE)
    annotation class CrossTip {
        companion object {
            const val Float = 0
            const val PinTop = 1
        }
    }

    @CrossTip
    protected var mCrossTipType = CrossTip.Float


    fun setCrossTipType(@CrossTip type: Int) {
        mCrossTipType = type
    }

    protected open fun refreshCrossLine(x: Float, y: Float) {

    }
}