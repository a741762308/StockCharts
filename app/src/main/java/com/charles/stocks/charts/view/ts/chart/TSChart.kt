package com.charles.stocks.charts.view.ts.chart

import android.graphics.*
import androidx.annotation.IntDef
import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.TSView
import com.charles.stocks.charts.view.ts.chart.TSChart.TSType.Companion.HK_TYPE
import com.charles.stocks.charts.view.ts.chart.TSChart.TSType.Companion.HS_TYPE
import com.charles.stocks.charts.view.ts.chart.core.TimeSharing
import com.charles.stocks.charts.view.ts.model.TSNode
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.abs
import kotlin.math.pow

class TSChart(
    tsView: TSView,
    stock: Stock,
    displayRect: Rect,
    pCLose: Double,
    highest: Double,
    lowest: Double,
    decimalPlaces: Int
) {
    private val mTSView = tsView
    private val mContext = tsView.context

    @Volatile
    private var mPClosePrice = pCLose

    @Volatile
    private var mHighestPrice = highest

    @Volatile
    private var mLowestPrice = lowest

    @Volatile
    private var mMaxRisePrice = 0.0

    @Volatile
    private var mMinRisePrice = 0.0

    private val mStock = stock
    private var mDecimalPlaces = decimalPlaces
    private val mCornerPathEffect: CornerPathEffect
    private val mTimeSharing: TimeSharing?
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mFontSize = UiUtils.dip2px(10f)

    private lateinit var mDisplayRect: Rect
    private var mDayWidth = 0f
    private var mNodeWidth = 0f
    private var mPriceHeightScale = 0.0
    private var mIsCalculate = false

    @TSType
    private var mTSType = HK_TYPE


    init {
        mTimeSharing = TimeSharing(mStock, mDecimalPlaces)
        mCornerPathEffect = CornerPathEffect(UiUtils.dip2px(2.0f).toFloat())
        seTSType(mStock)
        mPaint.textSize = mFontSize.toFloat()
        setDisplayRect(displayRect)
    }

    private fun seTSType(stock: Stock?) {
        stock?.let {
            if (stock.isHkStock() || stock.isUSStock()) {
                setTSType(HK_TYPE)
            } else {
                setTSType(HS_TYPE)
            }
        }
    }

    fun setTSType(@TSType type: Int) {
        this.mTSType = type
    }

    fun setDisplayRect(displayRect: Rect) {
        this.mDisplayRect = displayRect
        mDayWidth = mDisplayRect.width().toFloat()
        mNodeWidth = mDayWidth / (mTimeSharing!!.getTimeNodeCunt() - 1)
        calculateMaxPrice()
    }

    fun getDayWidth(): Float {
        return mDayWidth
    }

    fun getNodeWidth(): Float {
        return mNodeWidth
    }

    fun getTimeSharing(): TimeSharing? {
        return mTimeSharing
    }

    fun isHSTimeSharing(): Boolean {
        return mTSType == HS_TYPE
    }


    @Synchronized
    private fun calculateMaxPrice() {
        mMaxRisePrice = 0.0
        mMaxRisePrice = 0.0
        mPriceHeightScale = 0.0
        if (mTimeSharing?.mNodes == null || !mTimeSharing.mISHaveData) {
            return
        }
        var pClose: Double? = null
        var highestPrice = if (mHighestPrice != 0.0) mHighestPrice else null
        var lowestPrice = if (mLowestPrice != 0.0) mLowestPrice else null

        for (nodes in mTimeSharing.mNodes) {
            nodes?.let {
                for (node in nodes) {
                    node?.let {
                        if (pClose == null) {
                            pClose = node.mPClose
                            if (ChartsUtil.doubleEqualsZero(pClose)) {
                                pClose = node.mPrice
                            }
                        }
                        highestPrice = if (highestPrice == null) {
                            node.mPrice
                        } else {
                            (highestPrice!!).coerceAtLeast(node.mPrice)
                        }
                        lowestPrice = if (lowestPrice == null) {
                            node.mPrice
                        } else {
                            (lowestPrice!!).coerceAtMost(node.mPrice)
                        }

                    }
                }
            }
        }
        if (pClose != null) {
            mPClosePrice = pClose!!
        }
        if (highestPrice != null && lowestPrice != null) {
            if (isHSTimeSharing()) {
                mMaxRisePrice = abs(highestPrice!! - mPClosePrice).coerceAtLeast(abs(lowestPrice!! - mPClosePrice))
                mMinRisePrice = -mMaxRisePrice
            } else {
                mMaxRisePrice = highestPrice!! - mPClosePrice
                mMinRisePrice = lowestPrice!! - mPClosePrice
            }
        }
        if (mMaxRisePrice - mMinRisePrice < 0.00001) {
            mMaxRisePrice = 10.0.pow(-mDecimalPlaces.toDouble()) * 2
            mMinRisePrice = -mMaxRisePrice
        }
        mPriceHeightScale = mDisplayRect.height() / (mMaxRisePrice - mMinRisePrice)
        mIsCalculate = true
    }

    fun addData(tsData: TSResponse?): Single<Boolean> {
        return Single.create(SingleOnSubscribe<Boolean> {
            if (tsData == null) {
                it.onSuccess(false)
            } else {
                mDecimalPlaces = tsData.priceBase
                synchronized(this) {
                    mTimeSharing?.addData(tsData)
                    calculateMaxPrice()
                    it.onSuccess(true)
                }
            }

        }).subscribeOn(Schedulers.single())
    }

    fun onDraw(canvas: Canvas?, crossLineDayIndex: Int, crossLineIndex: Int, crossLineX: Float) {
        if (mDisplayRect.right == 0) {
            return
        }
        synchronized(this) {
            if (mTimeSharing == null || !mTimeSharing.mISHaveData) {
                return
            }
            drawTimeSharing(canvas)
        }
    }

    protected fun drawTimeSharing(canvas: Canvas?) {
        if (mTimeSharing?.mNodes == null) {
            return
        }
        drawTimeSharing(canvas, mTimeSharing.mNodes[0], mDisplayRect.left.toFloat(), 2.5f)
    }

    private fun drawTimeSharing(canvas: Canvas?, nodes: Array<TSNode?>?, startX: Float, priceLineWidth: Float) {
        if (canvas == null || nodes == null) {
            return
        }
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = priceLineWidth
        mPaint.shader = null

        var node: TSNode? = null
        var pricePath: Path? = null
        var priceShadowPath: Path? = null
        var avgPath: Path? = null

        var x = startX
        var lastX = startX
        var nodeCount = 0
        for (i in nodes.indices) {
            if (nodes[i] == null) {
                x += mNodeWidth
                continue
            }
            nodeCount++
            node = nodes[i]

            if (pricePath == null) {
                pricePath = Path()
                priceShadowPath = Path()
                pricePath.moveTo(x, getYPositionPrice(node))
                priceShadowPath.moveTo(x, getYPositionPrice(node))
            } else {
                pricePath.lineTo(x, getYPositionPrice(node))
                priceShadowPath!!.lineTo(x, getYPositionPrice(node))
            }
            if (avgPath == null) {
                avgPath = Path()
                avgPath.moveTo(x, getYPositionAvg(node))
            } else {
                avgPath.lineTo(x, getYPositionAvg(node))
            }

            lastX = x
            x += mNodeWidth
        }
        if (nodeCount == 1 && node != null) {
            pricePath?.lineTo(startX + mNodeWidth, getYPositionPrice(node))
            priceShadowPath?.lineTo(startX + mNodeWidth, getYPositionPrice(node))
            avgPath?.lineTo(startX + mNodeWidth, getYPositionAvg(node))

        }

        canvas.save()
        canvas.clipRect(mDisplayRect)

        if (pricePath != null) {
            mPaint.style = Paint.Style.STROKE
            mPaint.color = ChartsUtil.getPriceLineColor()
            canvas.drawPath(pricePath, mPaint)
        }
        if (priceShadowPath != null) {
            priceShadowPath.lineTo(lastX, mDisplayRect.bottom.toFloat())
            priceShadowPath.lineTo(startX, mDisplayRect.bottom.toFloat())
            priceShadowPath.close()
            mPaint.style = Paint.Style.FILL
            mPaint.color = ChartsUtil.getPriceLineShadowColor()
            canvas.drawPath(priceShadowPath, mPaint)
        }
        if (avgPath != null) {
            mPaint.style = Paint.Style.STROKE
            mPaint.color = ChartsUtil.getAvgLineColor()
            canvas.drawPath(avgPath, mPaint)
        }

        canvas.restore()
    }

    private fun getYPositionPrice(node: TSNode?): Float {
        node?.let {
            return getYPositionPrice(node.mPrice)
        }
        return 0f
    }

    private fun getYPositionAvg(node: TSNode?): Float {
        node?.let {
            return getYPositionPrice(node.mAvg)
        }
        return 0f
    }


    fun getYPositionPrice(price: Double): Float {
        return (mDisplayRect.bottom - mPriceHeightScale * (price - mPClosePrice - mMinRisePrice)).toFloat()
    }

    @IntDef(HK_TYPE, HS_TYPE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TSType {
        companion object {
            const val HK_TYPE = 0
            const val HS_TYPE = 1
        }
    }
}