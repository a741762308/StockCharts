package com.charles.stocks.charts.view.ts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.BaseChartsView
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.chart.TSChart
import com.charles.stocks.charts.view.ts.chart.tech.TSTechChart
import com.charles.stocks.charts.view.ts.chart.tech.TSTechChartVolume
import com.charles.stocks.charts.view.ts.model.TSNode
import kotlin.math.abs

class TSView(context: Context, attrs: AttributeSet) : BaseChartsView(context, attrs) {

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

    private val mCrossDotRadius = UiUtils.dip2px(3f).toFloat()

    init {
        isLongClickable = false
    }

    private var mOnTSListener: OnTSListener? = null


    fun setOnTSListener(listener: OnTSListener?) {
        mOnTSListener = listener
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


    fun setSupportCrossLine(boolean: Boolean) {
        mIsSupportCrossLine = boolean
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPointDownX = x
                    mPointDownY = y
                    mIsPointDown = true
                    if (mIsCrossLineEnable) {
                        mIsCrossLineEnable = false
                        mIsPointDown = false
                        refreshCrossLine(x, y)
                    }
                    if (mIsSupportCrossLine) {
                        postDelayed(mCrossRunnable, 225)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mIsCrossLineEnable) {
                        refreshCrossLine(x, y)
                    } else if (mIsPointDown && (abs(x - mPointDownX) > mTouchSlop || abs(y - mPointDownY) > mTouchSlop)) {
                        mIsPointDown = false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    removeCallbacks(mCrossRunnable)
                    mIsPointDown = false
                    invalidate()
                }
            }
        }
        super.onTouchEvent(event)

        return true
    }

    private var mCrossRunnable = Runnable {
        if (mIsPointDown) {
            mIsPointDown = false
            mIsCrossLineEnable = true
            refreshCrossLine(mPointDownX, mPointDownY)
        }
    }

    override fun refreshCrossLine(x: Float, y: Float) {
        mTsChart?.let { chart ->
            val timeSharing = chart.getTimeSharing()
            val nodes = timeSharing?.mNodes?.get(0)
            if (mIsCrossLineEnable) {
                nodes?.let {
                    val left = mTsChartRect.left.toFloat()
                    val right = left + chart.getDayWidth()
                    mCrossLineIndex = ((x - left) / chart.getNodeWidth()).toInt()
                    if (mCrossLineIndex >= nodes.size) {
                        mCrossLineIndex = nodes.size - 1
                    } else if (mCrossLineIndex < 0) {
                        mCrossLineIndex = 0
                    }
                    while (mCrossLineIndex >= 0 && nodes[mCrossLineIndex] == null) {
                        mCrossLineIndex--
                    }
                    mCrossLineX = left + mCrossLineIndex * chart.getNodeWidth()
                    if (mCrossLineX < left) {
                        mCrossLineX = left
                    } else if (mCrossLineX > right) {
                        mCrossLineX = right
                    }
                    mCrossLineY = y
                    if (mCrossLineY < mTsChartRect.top) {
                        mCrossLineY = mTsChartRect.top.toFloat()
                    } else if (mCrossLineY > mTsChartRect.bottom) {
                        mCrossLineY = mTsChartRect.bottom.toFloat()
                    }
                }
            } else {
                mCrossLineIndex = -1
                mCrossLineX = -1f
                mCrossLineY = -1f

            }
            mOnTSListener?.let {
                val node = if (mCrossLineIndex >= 0) nodes?.get(mCrossLineIndex) else null
                it.onCrossLineChange(node)
            }
            invalidate()
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
        canvas?.let {
            mPaint.pathEffect = null
            mPaint.style = Paint.Style.FILL
            mPaint.textSize = mFontSize.toFloat()
            mPaint.color = ChartsUtil.getCrossLineColor()
            val lineLeft: Int = mTsChartOutRect.left
            val lineRight = mTsChartOutRect.right
            canvas.drawLine(lineLeft.toFloat(), mCrossLineY, lineRight.toFloat(), mCrossLineY, mPaint)
            canvas.drawLine(mCrossLineX, mTsChartOutRect.top.toFloat(), mCrossLineX, mTsChartOutRect.bottom.toFloat(), mPaint)
            canvas.drawLine(mCrossLineX, mTimeTextRect.bottom.toFloat(), mCrossLineX, mTechChartOutRect.bottom.toFloat(), mPaint)

            val priceY: Float = mTsChart?.getYPositionPrice(node.mPrice) ?: 0f
            mPaint.color = ChartsUtil.getPriceLineColor()
            canvas.drawCircle(mCrossLineX, priceY, mCrossDotRadius, mPaint)

            mPaint.color = ChartsUtil.getCrossCircleBgColor()
            canvas.drawCircle(mCrossLineX, priceY, mCrossDotRadius - 2, mPaint)

            val avgY: Float = mTsChart?.getYPositionPrice(node.mAvg) ?: 0f
            mPaint.color = ChartsUtil.getAvgLineColor()
            canvas.drawCircle(mCrossLineX, avgY, mCrossDotRadius, mPaint)


            mPaint.color = ChartsUtil.getCrossCircleBgColor()
            canvas.drawCircle(mCrossLineX, avgY, mCrossDotRadius - 2, mPaint)

            if (mCrossTipType == CrossTip.Float) {
                mPaint.color = ChartsUtil.getCrossBgColor()
                mPaint.style = Paint.Style.FILL
                mCrossTipRect.top = mTsChartOutRect.top
                mCrossTipRect.bottom = mCrossTipRect.top + 7 * mCrossTipItemHeight + 8 * mCrossTipPadding
                if (mCrossLineX > mTsChartOutRect.centerX()) {
                    mCrossTipRect.left = mTsChartOutRect.left + UiUtils.dip2px(10f)
                    mCrossTipRect.right = mCrossTipRect.left + mCrossTipWidth
                } else {
                    mCrossTipRect.right = mTsChartOutRect.right - UiUtils.dip2px(10f)
                    mCrossTipRect.left = mCrossTipRect.right - mCrossTipWidth
                }
                canvas.drawRect(mCrossTipRect, mPaint)

                mPaint.color = ChartsUtil.getTextDefaultColor()
                mPaint.textSize = UiUtils.sp2px(10f).toFloat()

                val startX = mCrossTipRect.left + UiUtils.dip2px(4f)
                val endX = mCrossTipRect.right - UiUtils.dip2px(4f)

                var y = mCrossTipRect.top + mCrossTipPadding
                ChartsUtil.drawText(canvas, mPaint, "时间", startX, y)
                ChartsUtil.drawText(canvas, mPaint, ChartsUtil.formatTSNodeTimeToMMDDHHmm(node.mTime), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "价格", startX, y)
                ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWithoutZero(node.mPrice, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "均价", startX, y)
                ChartsUtil.drawText(canvas, mPaint, ChartsUtil.rebuildNumber(node.mAvg, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "涨跌额", startX, y)
                ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWidthSign(node.mChange, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "涨跌幅", startX, y)
                ChartsUtil.drawText(canvas, mPaint, "${ChartsUtil.reBuildNumWidthSign(node.mRoc, 2)}%", endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "成交量", startX, y)
                ChartsUtil.drawText(canvas, mPaint, "${ChartsUtil.formatNumWithUnitKeep2Decimal(node.mVolume)}股", endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                y += mCrossTipPadding + mCrossTipItemHeight
                ChartsUtil.drawText(canvas, mPaint, "成交额", startX, y)
                ChartsUtil.drawText(canvas, mPaint, ChartsUtil.formatNumWithUnitKeep2Decimal(node.mAmount), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)
            }
        }
    }
}

interface OnTSListener {
    fun onChartClick() {

    }

    fun onCrossLineChange(node: TSNode?)
}



