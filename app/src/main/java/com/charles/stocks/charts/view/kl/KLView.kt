package com.charles.stocks.charts.view.kl

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.charles.stocks.charts.constant.KLChartType
import com.charles.stocks.charts.http.KLResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.kl.chat.BaseKLChart
import com.charles.stocks.charts.view.kl.chat.tech.KLTechChartVolume
import com.charles.stocks.charts.view.kl.chat.core.KLChartCandle
import com.charles.stocks.charts.view.kl.model.KLNode
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

class KLView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val mKLChartOutLineRect = Rect()
    private val mKLChartRect = Rect()
    private val mKLChartTopTextRect = Rect()
    private val mTechChartOutLineRect = Rect()
    private val mTechChartRect = Rect()
    private val mTechChartTopTextRect = Rect()
    private val mTimeTextRect = Rect()

    private var mFontSize: Int
    private var mTopTextFontSize: Int
    private var mTextHeight: Int
    private var mChartMarginX: Int
    private var mChartMarginY: Int
    private var mMaxWidth = 0
    private var mNodeWidth: Int
    private var mNodeGap: Int
    private var mNodeWidthMax: Int

    private var mPointDownX = 0f
    private var mPointDownY = 0f
    private var mPointDownRawX = 0f
    private var mPointDownRawY = 0f
    private var mIsPointDown = false
    private val mTouchSlop: Int
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

    private val mNodeMap: TreeMap<Long, KLNode> =
        TreeMap<Long, KLNode>()
    private var mNodeList: List<KLNode>? = null

    init {
        mKlScroller = KLScroller()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
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

        mTechChartTopTextRect.top = mTimeTextRect.bottom + mChartMarginY;
        mTechChartTopTextRect.bottom = mTechChartTopTextRect.top + mTextHeight;
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
        var listSize = mNodeList?.size ?: 0
        var startX: Int = mStartX + mMoveX
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

    fun addKLineData(klResponse: KLResponse?) {
        if (klResponse == null) return
        val klData = klResponse.toKLDataList()
        if (klData == null || klData.isEmpty()) {
            return
        }
        mDecimalPlaces = klResponse.priceBase
        Executors.newSingleThreadExecutor().execute {
            val newList: List<KLNode> = klData
            val nodeList: MutableList<KLNode> =
                ArrayList<KLNode>()
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

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            it.save()
            if (mChartStartIndex in 0..mChartEndIndex) {
                mKLChart?.draw(it, mChartOffsetX, mChartStartIndex, mChartEndIndex, mNodeWidth, mNodeGap, -1)
                mTechChart?.draw(it, mChartOffsetX, mChartStartIndex, mChartEndIndex, mNodeWidth, mNodeGap, -1)
            }
            it.restore()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (mIsMoving) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(event)
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
                }
                MotionEvent.ACTION_MOVE -> {
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

    inner class KLScroller() {
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
}