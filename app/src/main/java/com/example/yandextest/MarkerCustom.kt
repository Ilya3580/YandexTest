package com.example.yandextest

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class MarkerCustom(context: Context?, layoutResource: Int, idPrice : Int, idData : Int,
                   private var maxX : Float, private var minX : Float,
                   private var maxY : Float, private var minY : Float)
    : MarkerView(context, layoutResource) {
    private lateinit var e : Entry

    private var price: TextView
    private var date : TextView

    init {

        price = findViewById(idPrice)
        date = findViewById(idData)
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        price.text = e.y.toString()
        val answer = java.util.Date((e.x.toLong() * 1000))
        val mas = answer.toString().split(" ")
        date.text = "${mas[2]} ${mas[1]} ${mas[5]}"
        this.e = e
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        var x = 0f
        var y = 0f
        x = if(e.x < (maxX + minX)/2){
            width.toFloat()
        }else{
            -width.toFloat()*2
        }

        y = if(e.y < (maxY + minY)/2){
            -height.toFloat()*2
        }else{
            height.toFloat()
        }
        return MPPointF(x, y)

    }
}


