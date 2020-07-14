package com.charles.stocks.charts.view.ts.chart.tech

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.charles.stocks.charts.utils.UiUtils
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.chart.core.TimeSharing
import java.lang.String

class TSTechChartVolume(timeSharing: TimeSharing?, displayRect: Rect, dayWidth: Float, nodeWidth: Float) :
    TSTechChart(timeSharing, displayRect, dayWidth, nodeWidth) {
    companion object {
        const val VOLUME = "成交量"
    }

    override fun calculateMaxPrice() {
        super.calculateMaxPrice()
        mMaxValue = 0.0
        mTimeSharing?.mNodes?.forEach { nodes ->
            nodes?.forEach { node ->
                node?.let {
                    mMaxValue = node.mVolume.coerceAtLeast(mMaxValue)
                }
            }
        }
        mHeightScale = if (mMaxValue < 0.1) {
            0.0
        } else {
            mDisplayRect.height() / mMaxValue
        }
    }

    override fun onDraw(canvas: Canvas?, crossLineDayIndex: Int, crossLineIndex: Int, corssLineX: Float) {
        super.onDraw(canvas, crossLineDayIndex, crossLineIndex, corssLineX)
        if (mTimeSharing?.mISHaveData == false) {
            return
        }
        canvas?.let {
            canvas.save()
            canvas.clipRect(mDisplayRect)

            mPaint.style = Paint.Style.STROKE
            if (mTimeSharing?.mNodes?.size == 1) {
                mPaint.strokeWidth = 3f
            } else {
                mPaint.strokeWidth = 1.5f
            }
            mTimeSharing?.mNodes?.forEachIndexed { index, arrayOfTSNodes ->
                var x = mDisplayRect.left + index * mDayWidth
                arrayOfTSNodes?.let {
                    for (node in it) {
                        node?.let {
                            when {
                                node.mVolumeDirect > 0 -> {
                                    mPaint.color = ChartsUtil.getRiseColor()
                                }
                                node.mVolumeDirect < 0 -> {
                                    mPaint.color = ChartsUtil.getFallColor()
                                }
                                else -> {
                                    mPaint.color = ChartsUtil.getPlatColor()
                                }
                            }
                            canvas.drawLine(x, (mDisplayRect.bottom - node.mVolume * mHeightScale).toFloat(), x, mDisplayRect.bottom.toFloat(), mPaint)

                            x += mNodeWidth
                        }
                    }

                }
            }

            mPaint.style = Paint.Style.FILL
            mPaint.strokeWidth = 1f
            mPaint.textSize = mVolumeFontSize.toFloat()
            mPaint.color = ChartsUtil.getTextDefaultColor()
            ChartsUtil.drawText(canvas, mPaint, VOLUME, mDisplayRect.left, mDisplayRect.top - 2, ChartsUtil.TEXT_ALIGN_LEFT or ChartsUtil.TEXT_ALIGN_TOP)

            val textWidth = ChartsUtil.getTextWidth(VOLUME, mPaint)
            val textHeight1 = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
            mPaint.textSize = mFontSize.toFloat()
            val textHeight2 = mPaint.fontMetrics.bottom - mPaint.fontMetrics.top
            val marginTop = ((textHeight1 - textHeight2) / 2).toInt()
            ChartsUtil.drawText(
                canvas,
                mPaint,
                String.format(
                    "%s股",
                    ChartsUtil.formatNumberUnit(mMaxValue)
                ),
                mDisplayRect.left + textWidth + UiUtils.dip2px(5f),
                mDisplayRect.top - 2 + marginTop,
                ChartsUtil.TEXT_ALIGN_LEFT or ChartsUtil.TEXT_ALIGN_TOP
            )

            canvas.restore()
        }
    }
}