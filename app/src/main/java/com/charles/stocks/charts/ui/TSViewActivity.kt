package com.charles.stocks.charts.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.charles.stocks.charts.R
import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.BaseResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.BaseChartsView
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.ts.OnTSListener
import com.charles.stocks.charts.view.ts.model.TSNode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ts_view.*
import java.io.BufferedInputStream

class TSViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ts_view)
        ts_view.setStock(Stock.newStock("usBABA")!!)
        ts_view.updateQuote(261.580, 261.900, 255.710)
        ts_view.setOnTSListener(object : OnTSListener {
            @SuppressLint("SetTextI18n")
            override fun onCrossLineChange(node: TSNode?) {
                if (!top_btn.isChecked) {
                    cross_view.visibility = View.INVISIBLE
                    return
                }
                cross_view.visibility = if (node == null) View.INVISIBLE else View.VISIBLE
                node?.let {
                    date_tv.text = ChartsUtil.formatTSNodeTimeToMMDDHHmm(node.mTime)
                    price_tv.text = ChartsUtil.reBuildNumWithoutZero(node.mPrice, 3)
                    change_tv.text = ChartsUtil.reBuildNumWidthSign(node.mChange, 3)
                    amount_tv.text = ChartsUtil.formatNumWithUnitKeep2Decimal(node.mAmount)
                    avg_tv.text = ChartsUtil.rebuildNumber(node.mAvg, 3)
                    roc_tv.text = "${ChartsUtil.reBuildNumWidthSign(node.mRoc, 2)}%"
                    volume_tv.text = "${ChartsUtil.formatNumWithUnitKeep2Decimal(node.mVolume)}股"
                }
            }
        })
        radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                top_btn.id -> {
                    ts_view.setCrossTipType(BaseChartsView.CrossTip.PinTop)
                }
                float_btn.id -> {
                    ts_view.setCrossTipType(BaseChartsView.CrossTip.Float)
                }
            }
        }
        ts_view.setCrossTipType(if (top_btn.isChecked) BaseChartsView.CrossTip.PinTop else BaseChartsView.CrossTip.Float)

        Observable.create(ObservableOnSubscribe<TSResponse>() {
            val bis = BufferedInputStream(assets.open("ts.json"))
            val response: BaseResponse<TSResponse> = Gson().fromJson(bis.reader(), object : TypeToken<BaseResponse<TSResponse>>() {}.type)
            it.onNext(response.data)
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe({
                ts_view.addTSData(it)
            }, {
                it.printStackTrace()
            })
    }

}
