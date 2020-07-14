package com.charles.stocks.charts.http

import com.charles.stocks.charts.view.kl.model.KLNode
import com.charles.stocks.charts.utils.DomainUtils
import com.google.gson.annotations.SerializedName

class KLResponse {

    @SerializedName("market")
    private var market: String? = null

    @SerializedName("symbol")
    private var code: String? = null

    @SerializedName("priceBase")
    var priceBase: Int = 0

    @SerializedName("type")
    private var type: String? = null

    @SerializedName("start")
    private var start: String? = null

    @SerializedName("direction")
    private var direction: String? = null

    @SerializedName("list")
    private var list: List<ListBean>? = null

    fun toKLDataList(): List<KLNode>? {
        val nodeList = arrayListOf<KLNode>()
        val priceBaseValue: Double = DomainUtils.priceBaseValue(priceBase).toDouble()
        list?.forEach {
            val open = it.open / priceBaseValue
            val close = it.close / priceBaseValue
            val high = it.high / priceBaseValue
            val low = it.low / priceBaseValue
            val avg = it.avg / priceBaseValue
            val amount = it.amount / priceBaseValue
            val turnover = it.turnover_rate / 100.0
            val postAmount = it.postAmount / priceBaseValue
            val pClose = it.pClose / priceBaseValue
            val change = it.change / priceBaseValue
            val roc = it.roc / 100.0
            val klNode = KLNode(
                it.time,
                open,
                close,
                high,
                low,
                avg,
                it.volume.toDouble(),
                amount,
                turnover,
                postAmount,
                it.postSize.toDouble(),
                pClose,
                change,
                roc
            )
            nodeList.add(klNode)
        }
        return nodeList
    }

   internal inner class ListBean {

        @SerializedName("close")
        var close: Long = 0

        @SerializedName("preClose")
        var pClose: Long = 0

        @SerializedName("open")
        var open: Long = 0

        @SerializedName("high")
        var high: Long = 0

        @SerializedName("low")
        var low: Long = 0

        @SerializedName("avg")
        var avg: Long = 0

        @SerializedName("volume")
        var volume: Long = 0

        @SerializedName("amount")
        var amount: Long = 0

        @SerializedName("netchng")
        var change: Long = 0

        @SerializedName("pctchng")
        var roc = 0

        @SerializedName("turnoverRate")
        var turnover_rate: Long = 0

        @SerializedName("latestTime")
        var time: Long = 0

        @SerializedName("postVolume")
        var postSize: Long = 0

        @SerializedName("postAmount")
        var postAmount: Long = 0
    }
}