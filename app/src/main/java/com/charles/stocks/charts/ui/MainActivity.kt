package com.charles.stocks.charts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.charles.stocks.charts.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ts.setOnClickListener {
            startActivity(Intent(this, TSViewActivity::class.java))
        }
        kline.setOnClickListener {
            startActivity(Intent(this, KLViewActivity::class.java))
        }
    }
}
