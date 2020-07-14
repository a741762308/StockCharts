package com.charles.stocks.charts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.charles.stocks.charts.R
import com.charles.stocks.charts.http.KLResponse
import com.charles.stocks.charts.http.TSResponse
import com.charles.stocks.charts.model.BaseResponse
import com.charles.stocks.charts.model.Stock
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
