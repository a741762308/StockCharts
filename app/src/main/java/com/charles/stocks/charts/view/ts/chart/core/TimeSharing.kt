package com.charles.stocks.charts.view.ts.chart.core

import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.model.TSNode

class TimeSharing(
    stock: Stock,
    decimalPlaces: Int
) {
    val mStock = stock
    var mDecimalPlaces = decimalPlaces
    val mNodes: Array<Array<TSNode?>?>?
    private var mPreDayNode: Array<TSNode?>? = null
    private var mNodeTimes: IntArray?
    var mISHaveData = false

    init {
        mNodeTimes = ChartsUtil.buildTimeNodes(mStock)
        mNodes = buildTsNodeArray()
    }

    private fun buildTsNodeArray(): Array<Array<TSNode?>?> {
        return Array(1) { Array<TSNode?>(getTimeNodeCunt()) { null } }
    }

    fun getTimeNodeCunt(): Int {
        if (mNodeTimes == null) {
            mNodeTimes = ChartsUtil.buildTimeNodes(mStock)
        }
        return mNodeTimes!!.size
    }

    fun addData(tsResponse: TSResponse?) {
        if (tsResponse == null) {
            return
        }
        mDecimalPlaces = tsResponse.priceBase
        val tsData = tsResponse.toTSDataList()
        if (tsData == null || tsData.isEmpty()) {
            return
        }

        mISHaveData = true

        val today = tsData[tsData.size - 1].mTime / 10000 * 10000
        mNodes!![0] = Array(mNodeTimes!!.size) { null }
        mPreDayNode = null

        var tsNode: TSNode
        var tsPreNode: TSNode?
        var tempNodes: Array<TSNode?>?
        var nodeTime: Long
        var nodeIndex: Int
        for (i in tsData.indices) {
            tsNode = tsData[i]
            if (tsResponse.days == 1 || tsNode.mTime >= today) {
                tempNodes = mNodes[0]
            } else if (tsResponse.days == 2) {
                if (mPreDayNode == null) {
                    mPreDayNode = Array(getTimeNodeCunt()) { null }
                }
                tempNodes = mPreDayNode
            } else {
                break
            }
            nodeTime = tsNode.mTime % 10000
            nodeIndex = getIndexOfTimeNodes(nodeTime)
            if (nodeIndex != -1) {
                if (i == 0) {
                    tempNodes?.set(nodeIndex, TSNode(tsNode.mTime, tsNode.mPClose, tsNode.mPrice, tsNode.mAvg, tsNode.mVolume, tsNode.mAmount, tsNode.mChange, tsNode.mRoc, tsNode.mPClose))
                } else {
                    tsPreNode = tsData[i - 1]
                    tempNodes?.set(nodeIndex, TSNode(tsNode.mTime, tsNode.mPClose, tsNode.mPrice, tsNode.mAvg, tsNode.mVolume, tsNode.mAmount, tsNode.mChange, tsNode.mRoc, tsPreNode.mPrice))
                }
            }
        }
        if (mNodes[0]?.get(0) == null) {
            tsNode = tsData[0]
            mNodes[0]?.let {
                for (k in it.indices) {
                    if (it[k] != null) {
                        break
                    }
                    it[k] = TSNode(tsNode.mTime / 10000 * 10000 + mNodeTimes!![k].toLong(), tsNode.mPClose, tsNode.mPClose, tsNode.mPClose)
                }
            }

        }
    }

    private fun getIndexOfTimeNodes(time: Long): Int {
        mNodeTimes?.let {
            var low = 0
            var hight = it.size - 1
            while (low <= hight) {
                val mid = (low + hight) / 2
                val value = it[mid]
                when {
                    value.toLong() == time -> {
                        return mid
                    }
                    value > time -> {
                        hight = mid - 1
                    }
                    else -> {
                        low = mid + 1
                    }
                }
            }
        }
        return -1
    }
}