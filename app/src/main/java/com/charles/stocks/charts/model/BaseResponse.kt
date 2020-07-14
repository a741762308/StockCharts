package com.charles.stocks.charts.model

class BaseResponse<T> {
    var code: Int = 0
    var msg: String? = ""
    var data: T? = null
}