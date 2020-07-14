package com.charles.stocks.charts.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.charles.stocks.charts.constant.Market

class Stock(@Market private val market: String?, private val code: String?) : Parcelable {
    fun getMarket(): String? {
        return market
    }

    fun getCode(): String? {
        return code
    }

    fun getId(): String {
        return market + code
    }

    fun isHkStock(): Boolean {
        return TextUtils.equals(Market.HK, market)
    }

    fun isUSStock(): Boolean {
        return TextUtils.equals(Market.US, market)
    }

    fun isHSStock(): Boolean {
        return isSHStock() || isSZStock()
    }

    fun isSHStock(): Boolean {
        return TextUtils.equals(Market.SH, market)
    }

    fun isSZStock(): Boolean {
        return TextUtils.equals(Market.SZ, market)
    }


    fun isStmStock(): Boolean {
        return isSHStock() && code != null && code.startsWith("688");
    }

    fun equalsId(stock: Stock): Boolean {
        if (equals(stock)) {
            return true
        }
        return market?.equals(stock.market) == true && code?.equals(stock.code, true) == true
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(market)
        writeString(code)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Stock> = object : Parcelable.Creator<Stock> {
            override fun createFromParcel(source: Parcel): Stock = Stock(source)
            override fun newArray(size: Int): Array<Stock?> = arrayOfNulls(size)
        }

        fun newStock(id: String?): Stock? {
            if (id != null && id.length > 2) {
                return Stock(id.substring(0, 2), id.substring(2))
            }
            return null
        }
    }
}