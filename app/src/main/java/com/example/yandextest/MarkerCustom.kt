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

//этот класс отвечает за кастомизацию маркера который появляется при нажатии на график
//мы передаем такие параметры как context, layoutResource - layout нашего маркера, idPrice и idData это id еучемшуц которые находятся внутри  layout
//максимальные и минимальные значения x и y это нежно для того чтобы отображать маркер в том месте где он не закрывает точку на которую мы нажали
//ну и соответственно наш список в котором хранится информация о каждом тикере
class MarkerCustom(context: Context?, layoutResource: Int, idPrice : Int, idData : Int,
                   private var maxX : Float, private var minX : Float,
                   private var maxY : Float, private var minY : Float,
                   private var idType : Int, private var stickChartInformation: ArrayList<StickChartInformation>
)
    : MarkerView(context, layoutResource) {
    //здесь будет хранится точка на которую мы нажали
    private lateinit var e : Entry

    //далле мы инициализируем наши textview по id которые мы получили
    private var price: TextView
    private var date : TextView
    init {

        price = findViewById(idPrice)
        date = findViewById(idData)
    }

    override fun refreshContent(e: Entry, highlight: Highlight) {
        //сохраняем нашу точку
        this.e = e
        //берем координату по x и используем в качестве индекса чтобы получить информацию со списка
        val i = e.x.toInt()

        //далее в зависимости какой тип у нашего графика мы устанавливаем нужный текст
        if(idType == 0) {
            price.text = e.y.toString()
        }else{
            val text = "open : ${stickChartInformation[i].open}\nclose : ${stickChartInformation[i].close}" +
                    "\nhigh : ${stickChartInformation[i].high}\nlow : ${stickChartInformation[i].low}"
            price.text = text
        }

        //далее преобразуем дату из формата unix и устанавливаем в textview
        val answer = java.util.Date((stickChartInformation[i].dateCode.toLong() * 1000))
        val mas = answer.toString().split(" ")
        date.text = "${mas[2]} ${mas[1]} ${mas[5]}"

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        //здесь я определяю в каком месте должен высветится наш маркер
        var x = 0f
        var y = 0f
        x = if (e.x < (maxX + minX) / 2) {
            width.toFloat()/1.4.toFloat()
        } else {
            -width.toFloat() * 1.4.toFloat()
        }

        y = if (e.y < (maxY + minY) / 2) {
            -height.toFloat() * 1.4.toFloat()
        } else {
            height.toFloat()/1.4.toFloat()
        }
        return MPPointF(x, y)

    }
}


