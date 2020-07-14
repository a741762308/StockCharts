package com.charles.stocks.charts.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.kl.model.KLNode
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.ceil

object ChartsUtil {
    const val TEXT_ALIGN_LEFT = 0x0001
    const val TEXT_ALIGN_RIGHT = 0x0002
    const val TEXT_ALIGN_TOP = 0x0004
    const val TEXT_ALIGN_BOTTOM = 0x0008
    const val TEXT_ALIGN_V_CENTER = 0x0010
    const val TEXT_ALIGN_H_CENTER = 0x0020
    const val TEXT_ALIGN_CENTER = TEXT_ALIGN_V_CENTER or TEXT_ALIGN_H_CENTER

    private val sHSTradeTimeSectors = arrayOf(intArrayOf(930, 1130), intArrayOf(1300, 1500))
    private val mStmTradeTimeSectors = arrayOf(intArrayOf(930, 1130), intArrayOf(1300, 1530))
    private val sHKTradeTimeSectors = arrayOf(intArrayOf(930, 1200), intArrayOf(1300, 1600))
    private val sUSTradeTimeSectors = arrayOf(intArrayOf(930, 1245), intArrayOf(1245, 1600))

    const val DOUBLE_NEG_OFFSET = -0.00000001
    const val DOUBLE_POS_OFFSET = 0.00000001

    @IntDef(
        TEXT_ALIGN_LEFT,
        TEXT_ALIGN_RIGHT,
        TEXT_ALIGN_TOP,
        TEXT_ALIGN_BOTTOM,
        TEXT_ALIGN_V_CENTER,
        TEXT_ALIGN_H_CENTER,
        TEXT_ALIGN_CENTER
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class TextAlign {}

    private val sDividerColor = Color.parseColor("#1C191919")

    private val sCandleFallColor = Color.parseColor("#FF1AB675")
    private val sCandleRiseColor = Color.parseColor("#FFFF5000")

    private val sLineRiseColor = Color.parseColor("#FFFF5000")
    private val sLineFallColor = Color.parseColor("#FF1AB675")
    private val sLinePlatColor = Color.parseColor("#FF7E9DCB")


    private val sTextColorDefault = Color.parseColor("#80191919")
    private val sTextColorTitle = Color.parseColor("#A6191919")

    private val sPriceLineColor = Color.parseColor("#FF47BBFF")
    private val sAvgLineColor = Color.parseColor("#FFA9880A")
    private val sPriceLineShadowColor = Color.parseColor("#400484CE")


    fun getTextWidth(text: String, paint: Paint): Int {
        return paint.measureText(text).toInt() + 1
    }

    fun getTextHeight(fm: Paint.FontMetrics): Int {
        return ceil(fm.descent - fm.top).toInt() + 2
    }

    fun drawText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        x: Int,
        y: Int,
        @TextAlign align: Int
    ) {
        val fm = paint.fontMetrics
        val left = when {
            (align and TEXT_ALIGN_H_CENTER) == TEXT_ALIGN_H_CENTER -> {
                x - getTextWidth(text, paint) / 2
            }
            (align and TEXT_ALIGN_RIGHT) == TEXT_ALIGN_RIGHT -> {
                x - getTextWidth(text, paint) / 2
            }
            else -> {
                x
            }
        }
        val baseY = when {
            (align and TEXT_ALIGN_V_CENTER) == TEXT_ALIGN_V_CENTER -> {
                y + getTextHeight(fm) / 2 - fm.bottom.toInt()
            }
            (align and TEXT_ALIGN_BOTTOM) == TEXT_ALIGN_BOTTOM -> {
                y - fm.bottom.toInt()
            }
            else -> {
                y + getTextHeight(fm) - fm.bottom.toInt()
            }
        }
        canvas.drawText(text, left.toFloat(), baseY.toFloat(), paint)
    }

    fun drawText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        rect: Rect,
        @TextAlign align: Int
    ) {
        val fm = paint.fontMetrics
        val left = when {
            (align and TEXT_ALIGN_H_CENTER) == TEXT_ALIGN_H_CENTER -> {
                rect.left + rect.width() / 2 - getTextWidth(text, paint) / 2
            }
            (align and TEXT_ALIGN_RIGHT) == TEXT_ALIGN_RIGHT -> {
                rect.right - getTextWidth(text, paint)
            }
            else -> {
                rect.left
            }
        }
        val baseY = when {
            (align and TEXT_ALIGN_V_CENTER) == TEXT_ALIGN_V_CENTER -> {
                rect.top + rect.height() / 2 + getTextHeight(fm) / 2 - fm.bottom.toInt()
            }
            (align and TEXT_ALIGN_BOTTOM) == TEXT_ALIGN_BOTTOM -> {
                rect.bottom - fm.bottom.toInt()
            }
            else -> {
                rect.top + getTextHeight(fm) - fm.bottom.toInt()
            }
        }
        canvas.drawText(text, left.toFloat(), baseY.toFloat(), paint)
    }

    fun formatNumberUnit(number: Double, sign: Boolean = false): String {
        var value = abs(number)
        var res = when {
            value / 1E12 > 1 -> {
                String.format("%s万亿", String.format("%1$.2f", value / 1E12))
            }
            value / 1E8 > 1 -> {
                String.format("%s亿", String.format("%1$.2f", value / 1E8))
            }
            value / 1E4 > 1 -> {
                String.format("%s万", String.format("%1$.2f", value / 1E4))
            }
            else -> {
                String.format("%1$.0f", value)
            }
        }
        if (number < 0) {
            res = "-$res"
        }
        if (sign && number > 0) {
            res = "+$res"
        }
        return res
    }

    fun rebuildNumber(number: Double, decimalPlaces: Int): String {
        return BigDecimal(number).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).toPlainString()
    }

    @ColorInt
    fun getCandleColor(value: Double): Int {
        return if (value < 0) sCandleFallColor else sCandleRiseColor
    }

    @ColorInt
    fun getCandleColor(node: KLNode): Int {
        var compare = node.mClose.compareTo(node.mOpen)
        if (compare == 0) {
            compare = node.mOpen.compareTo(node.mPClose)
        }
        return getCandleColor(compare.toDouble())
    }


    @ColorInt
    fun getTextTitleColor(): Int {
        return sTextColorTitle
    }

    @ColorInt
    fun getTextDefaultColor(): Int {
        return sTextColorDefault
    }

    @ColorInt
    fun getPriceLineColor(): Int {
        return sPriceLineColor
    }

    @ColorInt
    fun getAvgLineColor(): Int {
        return sAvgLineColor
    }

    @ColorInt
    fun getPriceLineShadowColor(): Int {
        return sPriceLineShadowColor
    }

    @ColorInt
    fun getRiseColor(): Int {
        return sLineRiseColor
    }

    @ColorInt
    fun getFallColor(): Int {
        return sLineFallColor
    }

    @ColorInt
    fun getPlatColor(): Int {
        return sLinePlatColor
    }

    @ColorInt
    fun getDividerColor(): Int {
        return sDividerColor
    }

    fun buildTimeNodes(stock: Stock): IntArray {
        val times = getTimeSections(stock)
        val nodes = IntArray(getTimeNodeCount(stock))

        var index = 0
        for (i in times.indices) {
            var startMin = times[i][0] / 100 * 60 + times[i][0] % 100
            val endMin = times[i][1] / 100 * 60 + times[i][1] % 100
            if (i > 0 && times[i][0] == times[i - 1][1]) {
                startMin += 1
            }
            for (j in startMin..endMin) {
                nodes[index] = j / 60 * 100 + j % 60
                index++
            }
        }
        return nodes
    }

    fun getTimeSections(stock: Stock?): Array<IntArray> {
        return stock?.let {
            when {
                stock.isHkStock() -> {
                    sHKTradeTimeSectors
                }
                stock.isUSStock() -> {
                    sUSTradeTimeSectors
                }
                stock.isStmStock() -> {
                    mStmTradeTimeSectors
                }
                else -> {
                    sHSTradeTimeSectors
                }
            }
        } ?: sHSTradeTimeSectors
    }

    fun getTimeNodeCount(stock: Stock): Int {
        val times = getTimeSections(stock)
        return getTimeNodeCount(times)
    }

    fun getTimeNodeCount(times: Array<IntArray>): Int {
        var count = 0
        for (i in times.indices) {
            var startMin = times[i][0] / 100 * 60 + times[i][0] % 100
            val endMin = times[i][1] / 100 * 60 + times[i][1] % 100
            if (i > 0 && times[i][0] == times[i - 1][1]) {
                startMin += 1
            }
            count += (endMin - startMin) + 1
        }
        return count
    }

    fun doubleEqualsZero(value: Double?): Boolean {
        value?.let {
            return it > DOUBLE_NEG_OFFSET && it < DOUBLE_POS_OFFSET
        }
        return true
    }

    fun getTimeHHmm(time: Long): String {
        val timeHHmm = time % 10000
        return String.format("%d:%02d", timeHHmm / 100, timeHHmm % 100)
    }
}