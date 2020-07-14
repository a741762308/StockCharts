package com.charles.stocks.charts.view.kl.chat

import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.kl.chat.core.BaseKLCore
import com.charles.stocks.charts.view.kl.model.KLNode
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseKLChart(
    stock: Stock,
    list: List<KLNode>?,
    displayRect: Rect,
    topTextRect: Rect,
    fontSize: Int,
    topTextFontSize: Int,
    decimalPlaces: Int
) {
    protected val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mDisplayRect = displayRect
    protected val mTopTextRec = topTextRect
    protected var mStock = stock
    protected val mNodeList = list
    protected val mDecimalPlaces = decimalPlaces
    protected val mFontSize = fontSize

    protected var mTopTextFontSize = topTextFontSize
    protected var mTextMargin: Int
    protected var mIsNeedCalculateDisplaySection = AtomicBoolean(false)
    protected val mCornerPathEffect: CornerPathEffect

    protected var mStartIndex = -1
    protected var mEndIndex = -1
    private var mOffsetX = 0
    private var mItemWidth = 0
    private var mItemGap = 0

    @Volatile
    protected var mMinValue: Double = 0.0

    @Volatile
    protected var mMaxValue: Double = 0.0
    protected var mHeightScale: Double = 0.0

    init {
        mPaint.textSize = mFontSize.toFloat()
        mIsNeedCalculateDisplaySection.set(true)
        mCornerPathEffect = CornerPathEffect(UiUtils.dip2px(2.0f).toFloat())
        mTextMargin = mFontSize / 2
    }

    protected fun getNode(index: Int): KLNode? {
        if (index >= 0 && mNodeList != null && index < mNodeList.size) {
            return mNodeList[index]
        }
        return null
    }

    fun setStock(stock: Stock) {
        mStock = stock
    }

    @CallSuper
    open fun draw(
        canvas: Canvas,
        offsetX: Int,
        startIndex: Int,
        endIndex: Int,
        itemWidth: Int,
        itemGap: Int,
        crossLineIndex: Int
    ) {
        mOffsetX = offsetX
        mItemWidth = itemWidth
        mItemGap = itemGap
        if (mIsNeedCalculateDisplaySection.get() || mStartIndex != startIndex || mEndIndex != endIndex) {
            mIsNeedCalculateDisplaySection.set(false)
            calculateDisplaySection(startIndex, endIndex)
        }
        drawTopTexts(canvas, itemWidth, if (crossLineIndex >= 0) crossLineIndex else endIndex)
    }

    protected fun isCompleteVisible(index: Int): Boolean {
        val x = mDisplayRect.left + mOffsetX
        val l = x + (index - mStartIndex) * (mItemWidth + mItemGap)
        val r = l + mItemWidth
        return l >= mDisplayRect.left && r <= mDisplayRect.right
    }

    protected open fun calculateDisplaySection(startIndex: Int, endIndex: Int) {

    }

    protected open fun drawTopTexts(canvas: Canvas, itemWidth: Int, currentIndex: Int) {
        canvas.save()
        canvas.clipRect(mTopTextRec)
        val items = getTopTextItems(itemWidth, currentIndex)
        if (items != null && items.isNotEmpty()) {
            var textX = mTopTextRec.left
            val textY = mTopTextRec.centerY()
            var textWidth: Int
            mTopTextFontSize = adjustTopTextSize(items, mTopTextFontSize, mTextMargin, mPaint)
            mPaint.style = Paint.Style.FILL
            mPaint.strokeWidth = 1.2f
            mPaint.pathEffect = null
            mPaint.textSize = mTopTextFontSize.toFloat()
            items.forEach {
                it?.let {
                    mPaint.color = it.mColor
                    ChartsUtil.drawText(
                        canvas,
                        mPaint,
                        it.mText,
                        textX,
                        textY,
                        ChartsUtil.TEXT_ALIGN_V_CENTER
                    )
                    textWidth = ChartsUtil.getTextWidth(it.mText, mPaint)
                    textX += textWidth + mTextMargin
                }
            }
        }
        canvas.restore()
    }

    protected abstract fun getTopTextItems(itemWidth: Int, currentIndex: Int): Array<TopTextItem?>?

    protected abstract fun getKLineData(): BaseKLCore?

    class TopTextItem(val mText: String, @ColorInt val mColor: Int) {

    }

    private fun adjustTopTextSize(
        items: Array<TopTextItem?>,
        fontSize: Int,
        textMargin: Int,
        paint: Paint
    ): Int {
        var textSize = fontSize
        paint.textSize = textSize.toFloat()
        var textWidth = 0
        items.forEach {
            it?.let {
                textWidth += ChartsUtil.getTextWidth(it.mText, paint) + textMargin
            }
        }
        textWidth -= textMargin
        if (textWidth > mTopTextRec.width()) {
            textSize = adjustTopTextSize(items, fontSize - 1, textMargin, paint)
        }
        return textSize
    }
}