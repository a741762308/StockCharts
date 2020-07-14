package com.charles.stocks.charts.view.kl.chat.tech

import android.graphics.Rect
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.kl.chat.BaseKLChart
import com.charles.stocks.charts.view.kl.model.KLNode

abstract class KLTechChart(
    stock: Stock,
    list: List<KLNode>,
    displayRect: Rect,
    topTextRect: Rect,
    fontSize: Int,
    topTextSize: Int,
    decimalPlaces: Int
) : BaseKLChart(stock, list, displayRect, topTextRect, fontSize, topTextSize, decimalPlaces) {

}