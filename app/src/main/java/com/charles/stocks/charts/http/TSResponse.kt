package com.charles.stocks.charts.http

import com.charles.stocks.charts.utils.DomainUtils
import com.charles.stocks.charts.view.ts.model.TSNode
import com.google.gson.annotations.SerializedName

class TSResponse {

    @SerializedName("market")
    private var market: String? = null

    @SerializedName("symbol")
    private var code: String? = null

    @SerializedName("priceBase")
    var priceBase: Int = 0

    @SerializedName("days")
    var days: Int = 0

    @SerializedName("type")
    private var type: String? = null

    @SerializedName("list")
    private var list: List<ListBean>? = null

    fun toTSDataList(): List<TSNode>? {
        val nodeList = arrayListOf<TSNode>()
        val priceBaseValue: Double = DomainUtils.priceBaseValue(priceBase).toDouble()
        list?.forEach() {
            val avg = it.avg / priceBaseValue
            val pClose = it.pClose / priceBaseValue
            val price = it.price / priceBaseValue
            val change = it.change / priceBaseValue
            val roc = it.roc / 100.0
            val amount = it.amount / priceBaseValue
            val tsNode = TSNode(it.time / 100000, pClose, price, avg, it.volume.toDouble(), amount, change, roc, pClose)
            nodeList.add(tsNode)
        }
        return nodeList
    }

    internal inner class ListBean {

        @SerializedName("preClose")
        var pClose: Long = 0

        @SerializedName("avg")
        var avg: Long = 0

        @SerializedName("price")
        var price: Long = 0

        @SerializedName("volume")
        var volume: Long = 0

        @SerializedName("amount")
        var amount: Long = 0

        @SerializedName("netchng")
        var change: Int = 0

        @SerializedName("pctchng")
        var roc: Int = 0

        @SerializedName("latestTime")
        var time: Long = 0

    }
}