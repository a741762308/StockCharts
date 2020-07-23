package com.charles.stocks.charts.view.kl

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Scroller
import com.charles.stocks.charts.constant.KLChartType
import com.charles.stocks.charts.constant.KLDirection
import com.charles.stocks.charts.constant.KLType
import com.charles.stocks.charts.http.KLResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.BaseChartsView
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.kl.chat.BaseKLChart
import com.charles.stocks.charts.view.kl.chat.core.KLChartCandle
import com.charles.stocks.charts.view.kl.chat.tech.KLTechChartVolume
import com.charles.stocks.charts.view.kl.model.KLNode
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

class KLView(context: Context, attrs: AttributeSet) : BaseChartsView(context, attrs) {

    private val mKLChartOutLineRect = Rect()
    private val mKLChartRect = Rect()
    private val mKLChartTopTextRect = Rect()
    private val mTechChartOutLineRect = Rect()
    private val mTechChartRect = Rect()
    private val mTechChartTopTextRect = Rect()
    private val mTimeTextRect = Rect()

    @KLType
    private var mKLType: Int = KLType.NONE

    @KLDirection
    private var mKLineDirect: Int = KLDirection.NONE

    private var mFontSize: Int
    private var mTopTextFontSize: Int
    private var mTextHeight: Int
    private var mChartMarginX: Int
    private var mChartMarginY: Int
    private var mMaxWidth = 0
    private var mNodeWidth: Int
    private var mNodeGap: Int
    private var mNodeWidthMax: Int

    private var mStartX = 0
    private var mMoveX = 0
    private var mIsMoving = false

    private val mKlScroller: KLScroller


    private var mChartStartIndex = -1
    private var mChartEndIndex = -1
    private var mChartOffsetX = 0

    private var mIsAreaInitiated = false

    private lateinit var mStock: Stock
    private var mDecimalPlaces = 2

    private var mKLChartType = ""
    private var mKLChart: KLChartCandle? = null
    private var mKLTechType = KLChartType.KL_TYPE_VOLUME
    private var mTechChart: BaseKLChart? = null

    private val mNodeMap = TreeMap<Long, KLNode>()
    private var mNodeList: List<KLNode>? = null

    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    init {
        mKlScroller = KLScroller()
        mFontSize = UiUtils.dip2px(10.0f)
        mTopTextFontSize = UiUtils.sp2px(8.0f)
        mTextHeight = UiUtils.dip2px(16f)
        mChartMarginX = UiUtils.dip2px(2f)
        mChartMarginY = UiUtils.dip2px(1f)
        mNodeWidthMax = UiUtils.dip2px(16.0f)
        if (mNodeWidthMax % 2 == 0) {
            mNodeWidthMax += 1
        }
        mNodeWidth = UiUtils.dip2px(4.1f)
        if (mNodeWidth % 2 == 0) {
            mNodeWidth += 1
        }
        mNodeGap = mNodeWidth / 3
    }

    private var mOnKLineListener: OnKLineListener? = null


    fun setOnKLineListener(listener: OnKLineListener?) {
        mOnKLineListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculateDisplayRect(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    @Synchronized
    private fun calculateDisplayRect(width: Int, height: Int) {
        val actualWidth = width - paddingLeft - paddingRight
        val actualHeight = height - paddingTop - paddingBottom
        if (actualWidth <= 0 || actualHeight <= 0) {
            return
        }
        val oldWidth = mKLChartOutLineRect.width()
        val oldHeight = mTimeTextRect.bottom - mKLChartTopTextRect.top
        val cellHeight = (actualHeight - mTextHeight * 3 - mChartMarginY)

        mKLChartTopTextRect.top = paddingTop
        mKLChartTopTextRect.bottom = mKLChartTopTextRect.top + mTextHeight - mChartMarginY
        mKLChartTopTextRect.left = paddingLeft + mChartMarginX
        mKLChartTopTextRect.right = paddingLeft + width - mChartMarginX

        mKLChartOutLineRect.top = mKLChartTopTextRect.bottom
        mKLChartOutLineRect.bottom = mKLChartOutLineRect.top + (cellHeight * 0.77).toInt()
        mKLChartOutLineRect.left = paddingLeft
        mKLChartOutLineRect.right = width - paddingRight

        mKLChartRect.top = mKLChartOutLineRect.top + mChartMarginY
        mKLChartRect.bottom = mKLChartOutLineRect.bottom - mChartMarginY
        mKLChartRect.left = mKLChartOutLineRect.left + mChartMarginX
        mKLChartRect.right = mKLChartOutLineRect.right - mChartMarginX

        mTimeTextRect.top = mKLChartOutLineRect.bottom
        mTimeTextRect.bottom = mTimeTextRect.top + mTextHeight
        mTimeTextRect.left = mKLChartRect.left
        mTimeTextRect.right = mKLChartRect.right

        mTechChartTopTextRect.top = mTimeTextRect.bottom + mChartMarginY
        mTechChartTopTextRect.bottom = mTechChartTopTextRect.top + mTextHeight
        mTechChartTopTextRect.left = mKLChartRect.left
        mTechChartTopTextRect.right = mKLChartRect.right


        mTechChartOutLineRect.top = mTechChartTopTextRect.bottom
        mTechChartOutLineRect.bottom = mTechChartOutLineRect.top + (cellHeight * 0.23).toInt()
        mTechChartOutLineRect.left = mKLChartOutLineRect.left
        mTechChartOutLineRect.right = mKLChartOutLineRect.right

        mTechChartRect.top = mTechChartOutLineRect.top + mChartMarginY
        mTechChartRect.bottom = mTechChartOutLineRect.bottom - 1
        mTechChartRect.left = mKLChartRect.left
        mTechChartRect.right = mKLChartRect.right

        mIsAreaInitiated = true

        if (mNodeList != null && (oldWidth != mKLChartOutLineRect.width() || oldHeight != mTimeTextRect.bottom - mKLChartTopTextRect.top)) {
            mMaxWidth = 0
            mCalculateDisplayPositionRunnable.run()
        }
    }

    private val mCalculateDisplayPositionRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mIsAreaInitiated && Thread.currentThread() === Looper.getMainLooper().thread) {
                calculateDisplayPosition()
                postInvalidate()
            } else {
                this@KLView.postDelayed(this, 100)
            }
        }
    }

    private fun calculateDisplayPosition() {
        val maxWidth: Int =
            (mNodeList?.size ?: 0) * (mNodeWidth + mNodeGap) - mNodeGap
        val chartWidth = mKLChartRect.width()
        if (mMaxWidth == 0) {
            mMaxWidth = maxWidth
            mStartX = if (mMaxWidth <= chartWidth) {
                0
            } else {
                mMaxWidth - chartWidth
            }
        } else {
            if (mMaxWidth < maxWidth) {
                mStartX = mStartX + maxWidth - mMaxWidth
            } else if (mMaxWidth > maxWidth) {
                mStartX = if (mMaxWidth <= chartWidth) {
                    0
                } else {
                    mMaxWidth - chartWidth
                }
            }
            mMaxWidth = maxWidth
        }
        calculateDisplaySection()
    }

    private fun calculateDisplaySection() {
        val listSize = mNodeList?.size ?: 0
        val startX: Int = mStartX + mMoveX
        mChartStartIndex = startX / (mNodeWidth + mNodeGap)
        if (mChartStartIndex < 0) {
            mChartStartIndex = 0
        }
        mChartEndIndex = mChartStartIndex + mKLChartRect.width() / (mNodeWidth + mNodeGap)
        while (mChartEndIndex * (mNodeWidth + mNodeGap) < startX + mKLChartRect.width()) {
            mChartEndIndex++
        }
        if (mChartEndIndex >= listSize) {
            mChartEndIndex = listSize - 1
        }
        mChartOffsetX = mChartStartIndex * (mNodeWidth + mNodeGap) - startX
    }

    fun setStock(stock: Stock) {
        mStock = stock
        mDecimalPlaces = 2
        mKLChart?.setStock(stock)
        mTechChart?.setStock(stock)
    }

    fun setKLType(@KLType klType: Int) {
        mKLType = klType
    }

    fun addKLineData(klResponse: KLResponse?) {
        if (klResponse == null) return
        val klData = klResponse.toKLDataList()
        if (klData == null || klData.isEmpty()) {
            return
        }
        mDecimalPlaces = klResponse.priceBase
        Executors.newSingleThreadExecutor().execute {
            val newList: List<KLNode> = klData
            val nodeList = ArrayList<KLNode>()
            synchronized(mNodeMap) {
                for (node in newList) {
                    mNodeMap[node.mTime] = node
                }
                nodeList.addAll(mNodeMap.values)
            }
            if (nodeList.size == 0) {
                return@execute
            }

            val candle: KLChartCandle? = buildKLCandleChart(
                mKLChartType,
                nodeList,
                mKLChartRect,
                mKLChartTopTextRect
            )
            val tech: BaseKLChart? =
                buildKLTechChart(mKLTechType, nodeList, mTechChartRect, mTechChartTopTextRect)

            //数据重置去UI线程做
            this@KLView.post {
                if (mMaxWidth < mKLChartRect.width()) {
                    mMaxWidth = 0
                }
                mNodeList = nodeList
                mKLChart = candle
                mTechChart = tech
                mCalculateDisplayPositionRunnable.run()
            }
        }
    }

    private fun buildKLCandleChart(
        techType: String,
        list: List<KLNode>?,
        rect: Rect,
        techTopRet: Rect
    ): KLChartCandle? {
        list?.let {
            var chart: KLChartCandle
            val cls = KLChartCandle::class.java
            try {
                val ct = cls.getDeclaredConstructor(Stock::class.java, List::class.java, Rect::class.java, Rect::class.java, Int::class.java, Int::class.java, Int::class.java)
                ct.isAccessible = true
                chart = ct.newInstance(mStock, list, rect, techTopRet, mFontSize, mTopTextFontSize, mDecimalPlaces)
            } catch (e: Exception) {
                e.printStackTrace()
                chart = KLChartCandle(mStock, list, mKLChartRect, mKLChartTopTextRect, mFontSize, mTopTextFontSize, mDecimalPlaces)
            }
            return chart
        }
        return null
    }

    private fun buildKLTechChart(
        techType: String,
        list: List<KLNode>?,
        rect: Rect,
        techTopRet: Rect
    ): BaseKLChart? {
        list?.let {
            var chart: BaseKLChart
            val cls = KLTechChartVolume::class.java
            try {
                val ct = cls.getDeclaredConstructor(Stock::class.java, List::class.java, Rect::class.java, Rect::class.java, Int::class.java, Int::class.java, Int::class.java)
                ct.isAccessible = true
                chart = ct.newInstance(mStock, list, rect, techTopRet, mFontSize, mTopTextFontSize, mDecimalPlaces)
            } catch (e: Exception) {
                e.printStackTrace()
                chart = KLTechChartVolume(mStock, list, mTechChartRect, mTechChartTopTextRect, mFontSize, mTopTextFontSize, mDecimalPlaces)
            }
            return chart
        }
        return null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.save()

            drawBorder(canvas)
            if (mChartStartIndex in 0..mChartEndIndex) {
                mKLChart?.draw(it, mChartOffsetX, mChartStartIndex, mChartEndIndex, mNodeWidth, mNodeGap, -1)
                mTechChart?.draw(it, mChartOffsetX, mChartStartIndex, mChartEndIndex, mNodeWidth, mNodeGap, -1)
            }
            drawingTime(canvas)
            drawCrossLine(canvas)

            it.restore()
        }
    }


    private fun drawBorder(canvas: Canvas) {
        mPaint.pathEffect = null
        mPaint.color = ChartsUtil.getDividerColor()
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 1f
        canvas.drawRect(mKLChartOutLineRect, mPaint)
        canvas.drawRect(mTechChartOutLineRect, mPaint)
    }

    private fun drawingTime(canvas: Canvas) {
        if (mChartStartIndex in 0..mChartEndIndex && mNodeList != null) {
            mPaint.style = Paint.Style.FILL
            mPaint.color = ChartsUtil.getTextDefaultColor()
            mPaint.textSize = mFontSize.toFloat()
            ChartsUtil.drawText(
                canvas, mPaint, ChartsUtil.formatDate(mKLType, mNodeList!![mChartStartIndex].mTime),
                mTimeTextRect, ChartsUtil.TEXT_ALIGN_LEFT or ChartsUtil.TEXT_ALIGN_V_CENTER
            )
            ChartsUtil.drawText(
                canvas, mPaint, ChartsUtil.formatDate(mKLType, mNodeList!![mChartEndIndex].mTime), mTimeTextRect,
                ChartsUtil.TEXT_ALIGN_RIGHT or ChartsUtil.TEXT_ALIGN_V_CENTER
            )
        }
    }

    private fun drawCrossLine(canvas: Canvas) {
        if (mCrossLineX < 0) {
            return
        }
        if (mIsCrossLineEnable) {
            mPaint.pathEffect = null
            mPaint.style = Paint.Style.FILL
            mPaint.textSize = mFontSize.toFloat()
            mPaint.color = ChartsUtil.getCrossLineColor()
            val lineLeft: Int = mKLChartOutLineRect.left
            val lineRight = mKLChartOutLineRect.right
            canvas.drawLine(lineLeft.toFloat(), mCrossLineY, lineRight.toFloat(), mCrossLineY, mPaint)
            canvas.drawLine(mCrossLineX, mKLChartOutLineRect.top.toFloat(), mCrossLineX, mKLChartOutLineRect.bottom.toFloat(), mPaint)
            canvas.drawLine(mCrossLineX, mTechChartOutLineRect.top.toFloat(), mCrossLineX, mTechChartOutLineRect.bottom.toFloat(), mPaint)

            val node = mNodeList?.get(mCrossLineIndex)
            node?.let {
                if (mCrossTipType == CrossTip.Float) {
                    mPaint.color = ChartsUtil.getCrossBgColor()
                    mPaint.style = Paint.Style.FILL
                    mCrossTipRect.top = mKLChartOutLineRect.top
                    mCrossTipRect.bottom = mCrossTipRect.top + 10 * mCrossTipItemHeight + 11 * mCrossTipPadding
                    if (mCrossLineX > mKLChartOutLineRect.centerX()) {
                        mCrossTipRect.left = mKLChartOutLineRect.left + UiUtils.dip2px(10f)
                        mCrossTipRect.right = mCrossTipRect.left + mCrossTipWidth
                    } else {
                        mCrossTipRect.right = mKLChartOutLineRect.right - UiUtils.dip2px(10f)
                        mCrossTipRect.left = mCrossTipRect.right - mCrossTipWidth
                    }
                    canvas.drawRect(mCrossTipRect, mPaint)

                    mPaint.color = ChartsUtil.getTextDefaultColor()
                    mPaint.textSize = UiUtils.sp2px(10f).toFloat()

                    val startX = mCrossTipRect.left + UiUtils.dip2px(4f)
                    val endX = mCrossTipRect.right - UiUtils.dip2px(4f)

                    var y = mCrossTipRect.top + mCrossTipPadding
                    ChartsUtil.drawText(
                        canvas,
                        mPaint,
                        "${ChartsUtil.formatQuoteTimeToYYYYMMDD(node.mTime)} ${ChartsUtil.formatQuoteTimeWeekName(node.mTime)}",
                        mCrossTipRect.left + mCrossTipRect.width() / 2,
                        y,
                        ChartsUtil.TEXT_ALIGN_H_CENTER
                    )

                    y += mCrossTipPadding + mCrossTipItemHeight
                    ChartsUtil.drawText(canvas, mPaint, "开盘", startX, y)
                    ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWithoutZero(node.mOpen, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                    y += mCrossTipPadding + mCrossTipItemHeight
                    ChartsUtil.drawText(canvas, mPaint, "最高", startX, y)
                    ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWithoutZero(node.mHigh, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                    y += mCrossTipPadding + mCrossTipItemHeight
                    ChartsUtil.drawText(canvas, mPaint, "最低", startX, y)
                    ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWithoutZero(node.mLow, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                    y += mCrossTipPadding + mCrossTipItemHeight
                    ChartsUtil.drawText(canvas, mPaint, "收盘", startX, y)
                    ChartsUtil.drawText(canvas, mPaint, ChartsUtil.reBuildNumWithoutZero(node.mPClose, 3), endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

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

                    y += mCrossTipPadding + mCrossTipItemHeight
                    ChartsUtil.drawText(canvas, mPaint, "换手率", startX, y)
                    ChartsUtil.drawText(canvas, mPaint, "${ChartsUtil.rebuildNumber(node.mTurnoverRate, 2)}%", endX, y, ChartsUtil.TEXT_ALIGN_RIGHT)

                }
            }

        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (mIsMoving) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(event)
    }

    private var mCrossRunnable = Runnable {
        if (!mIsPointDown) {
            return@Runnable

        }
        mIsPointDown = false
        mIsCrossLineEnable = true
        refreshCrossLine(mPointDownX, mPointDownY)
    }

    override fun refreshCrossLine(x: Float, y: Float) {
        if (mIsCrossLineEnable) {
            mNodeList?.let {
                mCrossLineIndex = (mChartStartIndex + (x - mKLChartRect.left - mChartOffsetX) / (mNodeWidth + mNodeGap)).toInt()
                if (mCrossLineIndex >= it.size) {
                    mCrossLineIndex = it.size - 1
                } else if (mCrossLineIndex < 0) {
                    mCrossLineIndex = 0
                }

                mCrossLineX = (mKLChartRect.left + mChartOffsetX + (mCrossLineIndex - mChartStartIndex) * (mNodeWidth + mNodeGap) + mNodeWidth / 2).toFloat()

                while (mCrossLineX < mKLChartRect.left && mCrossLineIndex < it.size - 1) {
                    mCrossLineIndex++
                    mCrossLineX = (mKLChartRect.left + mChartOffsetX + (mCrossLineIndex - mChartStartIndex) * (mNodeWidth + mNodeGap) + mNodeWidth / 2).toFloat()
                }

                while (mCrossLineX > mKLChartRect.right && mCrossLineIndex > 0) {
                    mCrossLineIndex--
                    mCrossLineX = (mKLChartRect.left + mChartOffsetX + (mCrossLineIndex - mChartStartIndex) * (mNodeWidth + mNodeGap) + mNodeWidth / 2).toFloat()
                }
                mCrossLineY = y
                if (mCrossLineY < mKLChartRect.top) {
                    mCrossLineY = mKLChartRect.top.toFloat()
                } else if (mCrossLineY > mKLChartRect.bottom) {
                    mCrossLineY = mKLChartRect.bottom.toFloat()
                }
            }
        } else {
            mCrossLineIndex = -1
            mCrossLineX = -1f
            mCrossLineY = -1f
        }
        mOnKLineListener?.let {
            val node = if (mCrossLineIndex >= 0) mNodeList?.get(mCrossLineIndex) else null
            it.onCrossLineChange(node)
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        mKlScroller.onTouchEvent(event)

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
                    } else {
                        mMoveX = 0
                        mIsMoving = false
                    }
                    if (mKLChartRect.contains(x.toInt(), y.toInt()) || mTechChartRect.contains(x.toInt(), y.toInt())) {
                        postDelayed(mCrossRunnable, 500)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mIsCrossLineEnable) {
                        refreshCrossLine(x, y)
                    } else {
                        val moveX = (mPointDownX - x).toInt()
                        if (!mIsMoving) {
                            mIsMoving = abs(moveX) > mTouchSlop
                            if (mIsMoving) {
                                mIsPointDown = false
                                val parent = parent
                                parent?.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                        if (mIsMoving) {
                            mMoveX = moveX
                            moveKLine()
                            invalidate()
                        }
                    }
                }
                else -> {
                    if (mIsMoving) {
                        mIsMoving = false
                        mStartX += mMoveX
                        mMoveX = 0
                        if (mStartX > mMaxWidth - mKLChartRect.width()) {
                            mStartX = mMaxWidth - mKLChartRect.width()
                        }
                        if (mStartX < 0) {
                            mStartX = 0
                        }
                        moveKLine()
                    }

                    removeCallbacks(mCrossRunnable)
                    mIsPointDown = false
                    invalidate()
                }
            }
        }
        return true
    }

    private fun moveKLine() {
        calculateDisplayPosition()
    }

    override fun computeScroll() {
        super.computeScroll()
        mKlScroller.computeScroll()
    }

    inner class KLScroller {
        private val mScroller = Scroller(context)
        private val mDetector: GestureDetector
        private var mIsFling = false

        init {
            mDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                    if (velocityX > 0) {
                        startFling(0, velocityX.toInt(), 0, velocityX.toInt())
                    } else {
                        startFling(0, velocityX.toInt(), velocityX.toInt(), 0)
                    }
                    return false
                }
            })
        }

        internal fun onTouchEvent(event: MotionEvent?) {
            mDetector.onTouchEvent(event)
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mIsFling) {
                        stopFling()
                    }
                }
            }
        }

        private fun startFling(startX: Int, velocityX: Int, minX: Int, maxX: Int) {
            if (mIsFling) {
                return
            }
            mIsFling = true
            mScroller.fling(startX, 0, velocityX, 0, minX, maxX, 0, 0)
            postInvalidate()
        }

        private fun stopFling() {
            if (mScroller.computeScrollOffset() || mIsFling) {
                mScroller.forceFinished(true)
                mIsFling = false
                mStartX += mMoveX
                mMoveX = 0
                if (mStartX > mMaxWidth - mKLChartRect.width()) {
                    mStartX = mMaxWidth - mKLChartRect.width()
                }
                if (mStartX < 0) {
                    mStartX = 0
                }
                moveKLine()
                postInvalidate()
            }
        }

        internal fun computeScroll() {
            if (mScroller.computeScrollOffset()) {
                mMoveX = -mScroller.currX
                val startX = mStartX + mMoveX
                if (startX >= mMaxWidth - mKLChartRect.width() || startX <= 0) {
                    stopFling()
                } else {
                    moveKLine()
                    postInvalidate()
                }
            } else if (mIsFling) {
                stopFling()
            }
        }
    }

    interface OnKLineListener {
        fun onChartClick() {

        }

        fun onCrossLineChange(node: KLNode?)

    }
}

