package com.charles.stocks.charts.view.ts.chart.tech

import android.graphics.*
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.chart.core.TimeSharing

abstract class TSTechChart(timeSharing: TimeSharing?, displayRect: Rect, dayWidth: Float, nodeWidth: Float) {
    protected var mTimeSharing = timeSharing
    protected var mDecimalPlaces = timeSharing?.mDecimalPlaces
    protected var mDisplayRect = displayRect
    protected val mStock = timeSharing?.mStock
    protected var mDayWidth = dayWidth
    protected var mNodeWidth = nodeWidth
    protected val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mFontSize = UiUtils.dip2px(10f)
    protected val mVolumeFontSize = UiUtils.dip2px(12f)
    protected val mFontMetrics: Paint.FontMetrics
    protected val mFontHeight: Int

    @Volatile
    protected var mMinValue = 0.0

    @Volatile
    protected var mMaxValue = 0.0
    protected var mHeightScale = 0.0


    init {
        mPaint.textSize = mFontSize.toFloat()
        mFontMetrics = mPaint.fontMetrics
        mFontHeight = ChartsUtil.getTextHeight(mFontMetrics)
        setDisplayRect(displayRect, dayWidth, nodeWidth)
    }

    fun setDisplayRect(displayRect: Rect, dayWidth: Float, nodeWidth: Float) {
        mDisplayRect = displayRect
        mDayWidth = dayWidth
        mNodeWidth = nodeWidth
        if (mTimeSharing?.mISHaveData == true && mDisplayRect.right != 0) {
            calculateMaxPrice()
        }
    }

    fun updateData(timeSharing: TimeSharing?) {
        mTimeSharing = timeSharing
        mDecimalPlaces = timeSharing?.mDecimalPlaces
        if (mTimeSharing?.mISHaveData == true && mDisplayRect.right != 0) {
            calculateMaxPrice()
        }
    }

    protected open fun calculateMaxPrice() {

    }

    @Synchronized
    open fun onDraw(canvas: Canvas?, crossLineDayIndex: Int, crossLineIndex: Int, corssLineX: Float) {

    }

    protected fun addPintToPath(paths: Array<Path?>, pathIndex: Int, value: Double, x: Float, baseY: Float) {
        addPintToPath(paths, pathIndex, x, (baseY - value * mHeightScale).toFloat())
    }

    private fun addPintToPath(paths: Array<Path?>, pathIndex: Int, x: Float, y: Float) {
        if (paths[pathIndex] == null) {
            paths[pathIndex] = Path()
            paths[pathIndex]?.moveTo(x, y)
        } else {
            paths[pathIndex]?.lineTo(x, y)
        }
    }
}
