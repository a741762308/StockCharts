package com.charles.stocks.charts.view.ts.model

class TSNode {
    val mTime: Long
    val mPClose: Double
    val mPrice: Double
    val mAvg: Double
    val mVolume: Double
    val mAmount: Double
    val mChange: Double
    val mRoc: Double
    val mVolumeDirect: Int


    constructor(
        mTime: Long,
        mPClose: Double,
        mPrice: Double,
        mAvg: Double
    ) {
        this.mTime = mTime
        this.mPClose = mPClose
        this.mPrice = mPrice
        this.mAvg = mAvg
        this.mVolume = 0.0
        this.mAmount = 0.0
        this.mChange = 0.0
        this.mRoc = 0.0
        this.mVolumeDirect = 1

    }

    constructor(
        mTime: Long,
        mPClose: Double,
        mPrice: Double,
        mAvg: Double,
        mVolume: Double,
        mAmount: Double,
        mChange: Double,
        mRoc: Double,
        prePrice: Double
    ) {
        this.mTime = mTime
        this.mPClose = mPClose
        this.mPrice = mPrice
        this.mAvg = mAvg
        this.mVolume = mVolume
        this.mAmount = mAmount
        this.mChange = mChange
        this.mRoc = mRoc

        var result = mPrice.compareTo(prePrice)
        when {
            result > 0 -> {
                this.mVolumeDirect = 1
            }
            result < 0 -> {
                this.mVolumeDirect = -1
            }
            else -> {
                this.mVolumeDirect = 0
            }
        }
    }
}