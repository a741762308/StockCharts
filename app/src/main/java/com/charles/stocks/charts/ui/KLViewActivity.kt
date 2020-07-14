package com.charles.stocks.charts.ui

import android.os.Bundle
import com.charles.stocks.charts.R
import com.charles.stocks.charts.http.KLResponse
import com.charles.stocks.charts.model.BaseResponse
import com.charles.stocks.charts.model.Stock
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
