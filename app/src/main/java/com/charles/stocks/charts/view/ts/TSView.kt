package com.charles.stocks.charts.view.ts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.chart.TSChart
import com.charles.stocks.charts.view.ts.chart.core.TimeSharing
import com.charles.stocks.charts.view.ts.chart.tech.TSTechChart
import com.charles.stocks.charts.view.ts.chart.tech.TSTechChartVolume

class TSView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDisplayRect = Rect()
    private val mTsChartOutRect = Rect()
    private val mTsChartRect = Rect()
    private val mTimeTextRect = Rect()
    private val mTechChartOutRect = Rect()
    private val mTechChartRect = Rect()

    private var mStock: Stock? = null
    private var mDecimalPlaces = 2
    private var mPClosePrice = 0.0
    private var mHighPrice = 0.0
    private var mLowPrice = 0.0
    private var mTsChart: TSChart? = null
    private var mTechChart: TSTechChart? = null

    private val mChartMarginY = UiUtils.dip2px(2f)
    private val mFontSize = UiUtils.dip2px(11f)

    private var mCrossLineIndex = -1
    private var mIsCrossLineEnable = false

    init {
        //不支持长按
        isLongClickable = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculateDisplayRect(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    @Synchronized
    fun calculateDisplayRect(width: Int, height: Int) {
        val actualWidth = width - paddingLeft - paddingRight
        val actualHeight = height - paddingTop - paddingBottom

        mDisplayRect.left = paddingLeft
        mDisplayRect.right = paddingLeft + actualWidth - UiUtils.dip2px(0.5f)
        mDisplayRect.top = paddingTop + UiUtils.dip2px(0.5f)
        mDisplayRect.bottom = paddingTop + actualHeight

        val timeTextHeight = UiUtils.dip2px(18f)
        val disPlayHeight = mDisplayRect.height() - timeTextHeight - mChartMarginY * 2

        val cellHeight = disPlayHeight / 4

        mTsChartOutRect.left = mDisplayRect.left
        mTsChartOutRect.top = mDisplayRect.top
        mTsChartOutRect.right = mDisplayRect.right
        mTsChartOutRect.bottom = mDisplayRect.top + cellHeight * 3 + mChartMarginY

        mTsChartRect.left = mTsChartOutRect.left + mChartMarginY
        mTsChartRect.top = mTsChartOutRect.top + mChartMarginY
        mTsChartRect.right = mTsChartOutRect.right - mChartMarginY
        mTsChartRect.bottom = mTsChartOutRect.bottom

        mTimeTextRect.left = mTsChartRect.left
        mTimeTextRect.top = mTsChartOutRect.bottom
        mTimeTextRect.right = mTsChartRect.right
        mTimeTextRect.bottom = mTsChartOutRect.bottom + timeTextHeight

        mTechChartOutRect.left = mTsChartOutRect.left
        mTechChartOutRect.top = mTimeTextRect.bottom
        mTechChartOutRect.right = mTsChartOutRect.right
        mTechChartOutRect.bottom = mDisplayRect.bottom

        mTechChartRect.left = mTsChartRect.left
        mTechChartRect.top = mTechChartOutRect.top + mChartMarginY
        mTechChartRect.right = mTsChartRect.right
        mTechChartRect.bottom = mTechChartOutRect.bottom - 1

        mTsChart?.let { chart ->
            chart.setDisplayRect(mTsChartRect)
            mTechChart?.setDisplayRect(mTechChartRect, chart.getDayWidth(), chart.getNodeWidth())
        }
    }

    fun setStock(stock: Stock) {
        if (mStock == stock) {
            return
        }
        mStock = stock
        if (mStock?.equalsId(stock) == false) {
            buildChart()
        }
    }

    fun updateQuote(pClosePrice: Double, high: Double, low: Double) {
        this.mPClosePrice = pClosePrice
        this.mHighPrice = high
        this.mLowPrice = low
    }

    private fun buildChart() {
        mStock?.let { stock ->
            mTsChart = TSChart(this, stock, mTsChartRect, mPClosePrice, mHighPrice, mLowPrice, mDecimalPlaces)
            mTsChart?.let { ts ->
                mTechChart = TSTechChartVolume(ts.getTimeSharing(), mTechChartRect, ts.getDayWidth(), ts.getNodeWidth())
            }
        }
    }

    fun addTSData(tsResponse: TSResponse) {
        mDecimalPlaces = tsResponse.priceBase
        if (mTsChart == null) {
            buildChart()
        }
        mTsChart?.let { ts ->
            ts.addData(tsResponse).subscribe({
                mTechChart?.updateData(ts.getTimeSharing())
                postInvalidate()
            }, {

            })
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawBorder(canvas)

        mTsChart?.onDraw(canvas, -1, -1, 0f)
        mTechChart?.onDraw(canvas, -1, -1, 0f)

        drawTimeSections(canvas)
        drawCrossLine(canvas)
    }


    private fun drawBorder(canvas: Canvas?) {
        canvas?.let {
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 1f
            mPaint.pathEffect = null
            mPaint.color = ChartsUtil.getDividerColor()
            canvas.drawRect(mTsChartOutRect, mPaint)
            canvas.drawRect(mTechChartOutRect, mPaint)
        }
    }

    private fun drawTimeSections(canvas: Canvas?) {

        canvas?.let {

            val timeSections = ChartsUtil.getTimeSections(mStock)
            mPaint.style = Paint.Style.FILL
            mPaint.textSize = mFontSize.toFloat()
            mPaint.color = ChartsUtil.getTextDefaultColor()
            ChartsUtil.drawText(canvas, mPaint, ChartsUtil.getTimeHHmm(timeSections[0][0].toLong()), mTimeTextRect, ChartsUtil.TEXT_ALIGN_LEFT or ChartsUtil.TEXT_ALIGN_V_CENTER)
            ChartsUtil.drawText(canvas, mPaint, ChartsUtil.getTimeHHmm(timeSections[timeSections.size - 1][1].toLong()), mTimeTextRect, ChartsUtil.TEXT_ALIGN_RIGHT or ChartsUtil.TEXT_ALIGN_V_CENTER)
            if (timeSections.size != 2) {
                return
            }
            val timeSharing = mTsChart?.getTimeSharing() ?: return
            val startM = timeSections[0][0] / 100 * 60 + timeSections[0][0] % 100
            val endM = timeSections[0][1] / 100 * 60 + timeSections[0][1] % 100


            val amCount = endM - startM + 1
            val x: Int

            x = mTimeTextRect.left + amCount * mTimeTextRect.width() / timeSharing.getTimeNodeCunt()
            if (timeSections[0][1] == timeSections[1][0]) {
                ChartsUtil.drawText(
                    canvas, mPaint, ChartsUtil.getTimeHHmm(timeSections[0][1].toLong()),
                    x, mTimeTextRect.centerY(), ChartsUtil.TEXT_ALIGN_CENTER
                )
            } else {
                ChartsUtil.drawText(
                    canvas, mPaint, ChartsUtil.getTimeHHmm(timeSections[0][1].toLong()) + "/" + ChartsUtil.getTimeHHmm(timeSections[1][0].toLong()),
                    x, mTimeTextRect.centerY(), ChartsUtil.TEXT_ALIGN_CENTER
                )
            }

        }
    }

    private fun drawCrossLine(canvas: Canvas?) {
        val timeSharing = mTsChart?.getTimeSharing() ?: return
        if (mCrossLineIndex < 0) {
            return
        }
        val node = timeSharing.mNodes?.get(0)?.get(mCrossLineIndex) ?: return
        mPaint.pathEffect = null

    }
}