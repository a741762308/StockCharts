package com.charles.stocks.charts.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.charles.stocks.charts.R
import com.charles.stocks.charts.constant.KLType
import com.charles.stocks.charts.http.KLResponse
import com.charles.stocks.charts.model.BaseResponse
import com.charles.stocks.charts.model.Stock
import com.charles.stocks.charts.view.BaseChartsView
import com.charles.stocks.charts.view.ChartsUtil
import com.charles.stocks.charts.view.kl.KLView
import com.charles.stocks.charts.view.kl.model.KLNode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_kl_view.*
import java.io.BufferedInputStream

class KLViewActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kl_view)
        kl_view.setStock(Stock.newStock("hk00700")!!)
        kl_view.setKLType(KLType.DAY)
        kl_view.setOnKLineListener(object : KLView.OnKLineListener {
            @SuppressLint("SetTextI18n")
            override fun onCrossLineChange(node: KLNode?) {
                if (!top_btn.isChecked) {
                    cross_view.visibility = View.INVISIBLE
                    return
                }
                cross_view.visibility = if (node == null) View.INVISIBLE else View.VISIBLE
                node?.let {
                    date_tv.text = "${ChartsUtil.formatQuoteTimeToYYYYMMDD(node.mTime)} ${ChartsUtil.formatQuoteTimeWeekName(node.mTime)}"
                    open_tv.text = ChartsUtil.reBuildNumWithoutZero(node.mOpen, 3)
                    highest_tv.text = ChartsUtil.reBuildNumWithoutZero(node.mHigh, 3)
                    change_tv.text = ChartsUtil.reBuildNumWidthSign(node.mChange, 3)
                    close_tv.text = ChartsUtil.reBuildNumWithoutZero(node.mPClose, 3)
                    lowest_tv.text = ChartsUtil.reBuildNumWithoutZero(node.mLow, 3)
                    roc_tv.text = "${ChartsUtil.reBuildNumWidthSign(node.mRoc, 2)}%"
                    volume_tv.text = "${ChartsUtil.formatNumWithUnitKeep2Decimal(node.mVolume)}股"
                    amount_tv.text = ChartsUtil.formatNumWithUnitKeep2Decimal(node.mAmount)
                    exchange_tv.text = "${ChartsUtil.rebuildNumber(node.mTurnoverRate, 2)}%"
                }
            }
        })
        radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                top_btn.id -> {
                    kl_view.setCrossTipType(BaseChartsView.CrossTip.PinTop)
                }
                float_btn.id -> {
                    kl_view.setCrossTipType(BaseChartsView.CrossTip.Float)
                }
            }
        }
        kl_view.setCrossTipType(if (top_btn.isChecked) BaseChartsView.CrossTip.PinTop else BaseChartsView.CrossTip.Float)

        Observable.create(ObservableOnSubscribe<KLResponse>() {
            val bis = BufferedInputStream(assets.open("kline.json"))
            val response: BaseResponse<KLResponse> = Gson().fromJson(bis.reader(), object : TypeToken<BaseResponse<KLResponse>>() {}.type)
            it.onNext(response.data)
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe({
                kl_view.addKLineData(it)
            }, {
                it.printStackTrace()
            })
    }
}
