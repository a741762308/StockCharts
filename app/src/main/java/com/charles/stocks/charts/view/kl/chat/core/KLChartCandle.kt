package com.charles.stocks.charts.view.kl.chat.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.kl.chat.BaseKLChart
import com.charles.stocks.charts.view.kl.model.KLNode
import kotlin.math.pow

class KLChartCandle(
    stock: Stock,
    list: List<KLNode>,
    displayRect: Rect,
    topTextRect: Rect,
    fontSize: Int,
    topTextSize: Int,
    decimalPlaces: Int
) : BaseKLChart(stock, list, displayRect, topTextRect, fontSize, topTextSize, decimalPlaces) {
    private var mMaxIndex = 0
    private var mMinIndex = 0

    private var mVisibleMaxIndex = 0
    private var mVisibleMinIndex = 0

    override fun calculateDisplaySection(startIndex: Int, endIndex: Int) {
        super.calculateDisplaySection(startIndex, endIndex)
        mStartIndex = startIndex
        mEndIndex = endIndex
        var node = getNode(startIndex)
        var maxNode = node
        var minNode = node

        mMaxValue = 0.0
        mMinValue = node?.mLow ?: 0.0
        mMaxIndex = startIndex
        mMaxIndex = startIndex

        var visibleMaxNode: KLNode? = null
        var visibleMinNode: KLNode? = null
        mVisibleMaxIndex = -1
        mVisibleMinIndex = -1

        for (i in startIndex..endIndex) {
            node = getNode(i)
            node?.let {
                mMaxValue = mMaxValue.coerceAtLeast(it.mHigh)
                if (node.mHigh.compareTo(maxNode?.mHigh ?: 0.0) >= 0) {
                    maxNode = node
                    mMaxIndex = i
                }
                mMinValue = mMinValue.coerceAtMost(it.mLow)
                if (minNode == null || (node.mLow).compareTo(minNode!!.mLow) <= 0) {
                    minNode = node
                    mMinIndex = i
                }
            }
            if (!isCompleteVisible(i)) {
                continue
            }
            if (visibleMaxNode == null || (node?.mHigh ?: 0.0).compareTo(visibleMaxNode.mHigh) >= 0) {
                visibleMaxNode = node
                mVisibleMaxIndex = i
            }
            if (visibleMinNode == null || (node != null && (node.mLow).compareTo(visibleMinNode.mLow) <= 0)) {
                visibleMinNode = node
                mVisibleMinIndex = i
            }
        }
        setMaxAndMinValue(mMaxValue, mMinValue)
    }

    protected fun setMaxAndMinValue(max: Double, min: Double) {
        mMaxValue = ChartsUtil.rebuildNumber(max, mDecimalPlaces).toDouble()
        mMinValue = ChartsUtil.rebuildNumber(min, mDecimalPlaces).toDouble()
        mMaxValue += 0.1 * (mMaxValue - mMinValue)
        mMinValue -= 0.1 * (mMaxValue - mMinValue)
        if (mMaxValue.compareTo(mMinValue) == 0 && mMinValue > 0) {
            mMinValue -= 10.0.pow(-mDecimalPlaces)
            mMaxValue += 10.0.pow(-mDecimalPlaces)
        }
        mHeightScale = mDisplayRect.height() / (mMaxValue - mMinValue)
    }


    override fun draw(
        canvas: Canvas,
        offsetX: Int,
        startIndex: Int,
        endIndex: Int,
        itemWidth: Int,
        itemGap: Int,
        crossLineIndex: Int
    ) {
        super.draw(canvas, offsetX, startIndex, endIndex, itemWidth, itemGap, crossLineIndex)
        canvas.save()
        canvas.clipRect(mDisplayRect)

        var x = mDisplayRect.left + offsetX
        val bottomY = mDisplayRect.bottom
        for (i in startIndex until endIndex + 1) {
            drawCandle(canvas, getNode(i), x, bottomY, itemWidth, mHeightScale)
            x += itemWidth + itemGap

        }
        canvas.restore()
    }

    private fun drawCandle(canvas: Canvas, node: KLNode?, x: Int, bottomY: Int, itemWidth: Int, scale: Double) {
        node?.let {
            mPaint.pathEffect = null
            mPaint.style = Paint.Style.FILL
            val maxY = (bottomY - (it.mHigh - mMinValue) * scale).toFloat()
            val minY = (bottomY - (it.mLow - mMinValue) * scale).toFloat()
            val openY = (bottomY - (it.mOpen - mMinValue) * scale).toFloat()
            val closeY = (bottomY - (it.mClose - mMinValue) * scale).toFloat()

            mPaint.color = ChartsUtil.getCandleColor(it)
            canvas.drawLine((x + itemWidth / 2).toFloat(), maxY, (x + itemWidth / 2).toFloat(), minY, mPaint)
            if (itemWidth > 1) {
                if (openY == closeY) {
                    canvas.drawLine(x.toFloat(), openY, (x + itemWidth).toFloat(), closeY, mPaint)
                } else {
                    canvas.drawRect(x.toFloat(), openY.coerceAtMost(closeY), (x + itemWidth).toFloat(), openY.coerceAtLeast(closeY), mPaint)
                }
            }
        }
    }


    override fun getTopTextItems(itemWidth: Int, currentIndex: Int): Array<TopTextItem?>? {
        return null
    }

    override fun getKLineData(): BaseKLCore? {
        return null
    }
}