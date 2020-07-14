package com.charles.stocks.charts.view.kl.chat.tech

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.kl.chat.core.BaseKLCore
import com.charles.stocks.charts.view.kl.model.KLNode

class KLTechChartVolume(
    stock: Stock,
    list: List<KLNode>,
    displayRect: Rect,
    topTextRect: Rect,
    fontSize: Int,
    topTextSize: Int,
    decimalPlaces: Int
) : KLTechChart(stock, list, displayRect, topTextRect, fontSize, topTextSize, decimalPlaces) {
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
        var bottomY = mDisplayRect.bottom
        for (i in startIndex..endIndex) {
            drawCandle(canvas, getNode(i), x, bottomY, itemWidth, mHeightScale)
            x += itemWidth + itemGap
        }
        mPaint.style = Paint.Style.STROKE
        mPaint.pathEffect = mCornerPathEffect
        mPaint.strokeWidth = 1.2f

        canvas.restore()
    }

    private fun drawCandle(
        canvas: Canvas,
        node: KLNode?,
        x: Int,
        bottomY: Int,
        itemWidth: Int,
        scale: Double
    ) {
        node?.let {
            mPaint.pathEffect = null
            mPaint.style = Paint.Style.FILL
            var maxY = bottomY - ((node.mVolume - mMinValue) * scale).toInt()
            if (maxY == bottomY) {
                maxY--
            }

            mPaint.color = ChartsUtil.getCandleColor(it)
            if (itemWidth > 1) {
                canvas.drawRect(
                    x.toFloat(),
                    maxY.toFloat(),
                    (x + itemWidth).toFloat(),
                    bottomY.toFloat(),
                    mPaint
                )
            } else {
                canvas.drawLine(x.toFloat(), maxY.toFloat(), x.toFloat(), bottomY.toFloat(), mPaint)
            }
        }
    }


    override fun calculateDisplaySection(startIndex: Int, endIndex: Int) {
        super.calculateDisplaySection(startIndex, endIndex)
        mStartIndex = startIndex
        mEndIndex = endIndex

        mMaxValue = getNode(startIndex)?.mVolume ?: 0.0
        for (i in startIndex..endIndex) {
            getNode(i)?.let {
                mMaxValue = mMaxValue.coerceAtLeast(it.mVolume)
                mMinValue = mMinValue.coerceAtMost(it.mVolume)
            }
        }

        if (mMaxValue != mMinValue) {
            mHeightScale = mDisplayRect.height() / (mMaxValue - mMinValue)
        }
    }

    override fun getTopTextItems(itemWidth: Int, currentIndex: Int): Array<TopTextItem?>? {
        val node: KLNode? = getNode(currentIndex)
        node?.let {
            val items: Array<TopTextItem?> = arrayOfNulls(1)
            items[0] =
                TopTextItem(ChartsUtil.formatNumberUnit(it.mVolume), ChartsUtil.getTextTitleColor())
            return items
        }
        return null
    }

    override fun getKLineData(): BaseKLCore? {
        return null
    }
}