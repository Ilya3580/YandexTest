package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

//этот класс отвечает за графики в конструкторе мы передаем ему тикер
class FragmentChart(private var ticker : String) : Fragment() {

    private lateinit var myView : View//это view фрагмента
    private lateinit var lstTextViews : ArrayList<TextView>//список кнопок под графиком где устанвливаем период
    private lateinit var mLineChart : LineChart//линейный график
    private lateinit var mLineStick : CandleStickChart//график со свечами
    private lateinit var alert : AlertDialog//диалоговое окно с progressbar
    //экземпляр класса для загруки информации с интернета
    private lateinit var client : OkHttpClient
    private lateinit var lstEntry : ArrayList<Entry>//спикок для линейного графика
    private lateinit var lstCandleEntry: ArrayList<CandleEntry>//спикок для графика со свечками
    private lateinit var lstDataRequestChartClass : ArrayList<StickChartInformation>//спикок с загруженной информацией с интернета
    private lateinit var viewModelWebSocket : MyViewModel<StickWebSocket>//viewmodel который получает сообщения от Listener webSocket
    private var textView : TextView? = null//textView с ценой акций
    private lateinit var chartIndicator : ImageButton//этот индикатор при нажатии на него мы меняем тип графика
    var symbolPeriod = "D"//начальный символ позже мы его переопределяем на тот который сохранился при просмотре в последний оаз

    //следующие пять строк нужны для сохранения информации
    private lateinit var sPref : SharedPreferences
    private val CHART = "CHART"
    private val SYMBOL_DAY = "SYMBOL_DAY"
    private val CANDLE_OR_CHART = "CANDLE_OR_CHART"
    private var idCandleOrChart = 0

    //это максимальные и минимальные значения графика они нужны для того чтобы отобрызить окно при нажатиии на график с нужной стороны
    var maxX = Float.MIN_VALUE
    var minX = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE
    var minY = Float.MAX_VALUE

    //здесь мы получаем символ валюты эту функциию мы вызваем из SectionPageAdapter
    //enterCurrency нужен чтобы переносить информации если ориентация горизонатльная или через пробел если вертикальная
    private var enterCurrency = ""
    private var _currencySymbol = ""
    public var currencySymbol : String
        get() {return _currencySymbol}
        set(value) {
            _currencySymbol = value
            if(textView != null)
                textView!!.text = textView!!.text.toString() + enterCurrency + _currencySymbol

        }

    //здесь id и текст выбранного элемента
    private var idElement= 0
    private var symbolElement = "D"



    companion object {

        fun newInstance(ticker : String) = FragmentChart(ticker)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true

        //здесь я определяю какую view использовать в зависимости о ориентации экрана а также устанавливаю enterCurrency
        myView = if(requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            enterCurrency = " "
            inflater.inflate(R.layout.fragment_chart_verical, container, false)
        }else{
            enterCurrency = "\n"
            inflater.inflate(R.layout.fragment_chart_horizontal, container, false)
        }

        //инициализирую элементы из view
        mLineChart = myView.findViewById(R.id.graphChart)
        mLineStick = myView.findViewById(R.id.graphStick)
        chartIndicator = myView.findViewById(R.id.indicatorChart)
        textView = myView.findViewById(R.id.price)

        //создаю экземпляры разных классов
        lstDataRequestChartClass = ArrayList()
        viewModelWebSocket = MyViewModel(StickWebSocket())
        lstEntry = ArrayList()
        sPref = requireContext().getSharedPreferences(CHART, Context.MODE_PRIVATE)

        //определяю какой тип графика стоит использовать
        idCandleOrChart = if(sPref.contains(CANDLE_OR_CHART)){
            if(sPref.getString(CANDLE_OR_CHART, "") == "0"){
                0
            }else{
                1
            }
        }else{
            0
        }

        //здесь я сохраняю в список все кнопик с периодом
        lstTextViews = ArrayList()
        lstTextViews.add(myView.findViewById(R.id.day))
        lstTextViews.add(myView.findViewById(R.id.week))
        lstTextViews.add(myView.findViewById(R.id.month))
        lstTextViews.add(myView.findViewById(R.id.halfYear))
        lstTextViews.add(myView.findViewById(R.id.year))
        lstTextViews.add(myView.findViewById(R.id.tenYear))

        //здесь отслеживаюю нажатие на индикатор смены типа графика
        chartIndicator.setOnClickListener {
            idCandleOrChart = if(idCandleOrChart == 0){
                1
            }else{
                0
            }

            //сохраняю эту иформацию
            val ed = sPref.edit()
            ed.putString(CANDLE_OR_CHART, idCandleOrChart.toString())
            ed.apply()

            //и показыаю тип графика который нам нужен
            if(idCandleOrChart == 0) {
                mLineStick.visibility = View.GONE
                mLineChart.visibility =  View.VISIBLE
            }else{
                mLineStick.visibility = View.VISIBLE
                mLineChart.visibility =  View.GONE
            }
            //и обновляю в нем информацию
            updateData()
        }

        //здесь я определяю какой элемент переиод и какую кнопку периода нужно выделить
        symbolElement = if(sPref.contains(SYMBOL_DAY + ticker)){
            sPref.getString(SYMBOL_DAY + ticker, "")!!
        }else{
            val ed = sPref.edit()
            ed.putString(SYMBOL_DAY + ticker, "D")
            ed.apply()
            "D"

        }

        startSetting()

        settingGraph()

        updateDataRequest(symbolElement)

        //это слушатель viewmodel которы отвечает за сообщения websocket
        viewModelWebSocket.getUsersValue().observe(viewLifecycleOwner, Observer {
            //этот тип слушателей вызвается при создании поэтому надо провереть тот ли это случай
            if(lstDataRequestChartClass.count() > 0) {
                var periodInt = 0
                //так как дата кодируется в формете unix то эта разница для каждо типа которая должна быть
                //эта разница берется не с воздуха а получается путем преобразований времени в секнуду
                //то есть время период для дневного графика это 5 минут а это 300 секнуд, для недельного 15 минут а это 900 мин
                when (symbolPeriod) {
                    "D" -> periodInt = 300
                    "W" -> periodInt = 900
                    "M" -> periodInt = 1800
                    "6M" -> periodInt = 86400
                    "1Y" -> periodInt = 86400
                    "10Y" -> periodInt = 2592000
                }

                //здесь я сохраняю данные в нащ список
                val item = lstDataRequestChartClass[lstDataRequestChartClass.count() - 1]
                //этот if отвечает нужно ли создать новую свечу или изменить старую
                //это делается для того чтобы задавать точки и свечи с определенным периодом
                //иначе будет много свечей или точек в одном месте
                if(it.dateCode - item.dateCode < periodInt){
                    item.close = it.price
                    //далее я опредеяю уменьшилось или увеличилось значение акции и в зависимости этого устанавливаю параметры
                    if(item.high < it.price){
                        item.high = it.price
                    }
                    if(item.low > it.price){
                        item.low = it.price
                    }else if(item.low == 0f){
                        item.low = it.price
                    }
                }else{
                    //созда.ю новую свечу
                    lstDataRequestChartClass.add(StickChartInformation(it.dateCode, it.price, 0f,0f,0f))
                }
                //обновляю данные
                updateData()
            }
        })

        return myView
    }

    private fun startSetting(){
        //это начальные настройки для кнопок под графиком
        //а также слушатели на нажатие
        for(i in (0 until lstTextViews.count())){
            if(lstTextViews[i].text.toString() == symbolElement){
                lstTextViews[i].background = requireContext().resources.getDrawable(R.color.black)
                lstTextViews[i].setTextColor(Color.WHITE)
                idElement = i
            }
            lstTextViews[i].setOnClickListener {
                val ed = sPref.edit()
                ed.putString(SYMBOL_DAY + ticker, lstTextViews[i].text.toString())
                ed.apply()
                val animate = AnimateClass()

                animate.colorAnimateBackground(lstTextViews[idElement], Color.BLACK, Color.WHITE)
                animate.colorAnimateText(lstTextViews[idElement], Color.WHITE, Color.BLACK)

                animate.colorAnimateBackground(lstTextViews[i], Color.WHITE, Color.BLACK)
                animate.colorAnimateText(lstTextViews[i], Color.BLACK, Color.WHITE)

                idElement = i


                updateDataRequest(lstTextViews[i].text.toString())
            }
        }
    }

    private fun settingGraph(){
        //это начальные настройки графиков в них я отключаю название компании под р=графиком так как оно уже есть в appbar
        //значение справа сверху под графиком так все это отобразится при коике на график
        mLineChart.legend.isEnabled = false
        mLineChart.xAxis.setDrawLabels(false)
        mLineChart.xAxis.setDrawGridLines(false)
        mLineChart.axisRight.setDrawLabels(false)
        mLineChart.description.text = ""

        mLineStick.legend.isEnabled = false
        mLineStick.xAxis.setDrawLabels(false)
        mLineStick.xAxis.setDrawGridLines(false)
        mLineStick.axisRight.setDrawLabels(false)
        mLineStick.description.text = ""

        //здесь я показываю тот тип графика которы выбран
        if(idCandleOrChart == 0) {
            mLineStick.visibility = View.GONE
            mLineChart.visibility =  View.VISIBLE
        }else{
            mLineStick.visibility = View.VISIBLE
            mLineChart.visibility =  View.GONE
        }
    }

    //здесь я обновляю список исторических данных
    private fun updateDataRequest(period : String){
        startProgressBar()
        //здесь я устанавливаю период через для определенного промежутка времени
        when(period){
            "D" -> symbolPeriod = "5m"
            "W" -> symbolPeriod = "15m"
            "M" -> symbolPeriod = "30m"
            "6M" -> symbolPeriod = "1d"
            "1Y" -> symbolPeriod = "1d"
            "10Y" -> symbolPeriod = "1mo"
        }
        //здесь я устанавливаю период и тикер в ссылку
        var url = EnumListName.DATA_CHART.value
        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)
        url = url.replace(EnumListName.MY_SYMBOL2.value, symbolPeriod)

        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    //получаю информацию и обратываю её
                    parsHistoryData(body, period)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                //если во время обновления пропадает интернет то возвращаю на начальную активность
                if(!InternetFunctions.hasConnection(requireContext())){
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    requireContext().startActivity(intent)
                }
            }
        })
    }

    private fun parsHistoryData(text : String, period: String){
        //здесь я получаю список инсторических данных и сохраняю в спикок
        val json = JSONObject(text)
        val jsonArray = json.getJSONObject("items")
        lstDataRequestChartClass = ArrayList()
        for(i in (0 until jsonArray.names().length())){
            val nameJson = jsonArray.getJSONObject(jsonArray.names()[i].toString())
            val dateCode = jsonArray.names()[i].toString().toInt()
            val high = nameJson.get("high").toString().toFloat()
            val low = nameJson.get("low").toString().toFloat()
            val open = nameJson.get("open").toString().toFloat()
            val close = nameJson.get("close").toString().toFloat()
            lstDataRequestChartClass.add(StickChartInformation(dateCode, open, high, low, close))
        }

        //так как в api которое я использую нет данных за неделю, день и т.д. то вызываю другие исторические данные и обрезаю их до нужных
        lstDataRequestChartClass = StickChartInformation.convertListPeriod(period, lstDataRequestChartClass)

        //далле я ищу и устанвливаю минимальрые значения для y для x 'nj просто ноль и длина списка
        minX = 0f
        maxX = lstDataRequestChartClass.count().toFloat()
        for(i in lstDataRequestChartClass){
            if(i.high > maxY){
                maxY = i.high
            }

            if(i.high < minY){
                minY = i.high
            }

            if(i.low > maxY){
                maxY = i.low
            }

            if(i.low < minY){
                minY = i.low
            }
        }

        //обновляю дынные графика
        updateData()

        //запускаю websocket
        startWebSocket()
    }

    private fun updateData(){
        //здесь я проверяю задает ли последний элемент максимум и миниму это нужно для того чтобы при добавление элемент от websoket изменить максиму и минимум
        if(lstDataRequestChartClass.count() > 0) {
            val i = lstDataRequestChartClass[lstDataRequestChartClass.count() - 1]
            if (i.high > maxY) {
                maxY = i.high
            }

            if (i.high < minY) {
                minY = i.high
            }

            if (i.low > maxY) {
                maxY = i.low
            }

            if (i.low < minY) {
                minY = i.low
            }
        }

        //вызываю соотвествующие настроки для разных типов графиков
        if(idCandleOrChart == 0) {
            //это функция преобразовывает список для линейного графика
            lstEntry = StickChartInformation.revertListEntry(lstDataRequestChartClass)
            updateDataChart()
        }else{
            //это функция преобразовывает список для свечного графика
            lstCandleEntry = StickChartInformation.revertListCandleEntry(lstDataRequestChartClass)
            updateDataCandle()
        }

    }

    private fun updateDataChart(){
        //добавляю списко в график
        val setComp = LineDataSet(lstEntry,"")
        setComp.axisDependency = YAxis.AxisDependency.LEFT

        //убираю все лишнее
        setComp.setDrawValues(false)
        setComp.setDrawCircleHole(false)
        setComp.setDrawCircles(false)
        //настраиваю цвет линни
        setComp.color = Color.BLACK

        //это требования апи потому оно позволяет создавать несколько линий на одном графике но не в этом случаю
        val dataSet = ArrayList<ILineDataSet>()
        dataSet.add(setComp)

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            //изменяю текст цены графика
            if(lstDataRequestChartClass.count() - 1 >= 0)
                textView!!.text = lstDataRequestChartClass[lstDataRequestChartClass.count() - 1].close.toString() + enterCurrency + _currencySymbol

            //добавляю все данные в график
            val data = LineData(dataSet)
            mLineChart.data = data

            //включаю нажатие на график и устанавливаю кастомные настройки
            mLineChart.setTouchEnabled(true)
            val mv = MarkerCustom(requireContext(), R.layout.custom_marker_view_layout,
                R.id.markerPrice, R.id.markerDate, maxX, minX, maxY, minY, 0, lstDataRequestChartClass)

            //создаю график
            mLineChart.marker = mv
            mLineChart.invalidate()


            stopProgressBar()
        }
    }

    private fun updateDataCandle(){

        //добавляю списко в график и настраиваю свечной график
        val cds = CandleDataSet(lstCandleEntry, "")
        cds.shadowWidth = 0.3f
        cds.shadowColor = Color.DKGRAY
        cds.increasingPaintStyle = Paint.Style.FILL
        cds.decreasingPaintStyle = Paint.Style.FILL
        cds.decreasingColor = requireContext().resources.getColor(R.color.red)
        cds.increasingColor = requireContext().resources.getColor(R.color.green)
        cds.setDrawValues(false)

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            //изменяю текст цены графика
            if(lstDataRequestChartClass.count() - 1 >= 0)
                textView!!.text = lstDataRequestChartClass[lstDataRequestChartClass.count() - 1].close.toString() + enterCurrency + _currencySymbol

            val cd = CandleData(cds)
            //включаю нажатие на график и устанавливаю кастомные настройки
            mLineStick.setTouchEnabled(true)
            val mv = MarkerCustom(requireContext(), R.layout.custom_marker_view_layout,
                R.id.markerPrice, R.id.markerDate, maxX, minX, maxY, minY, 1, lstDataRequestChartClass)

            //создаю график
            mLineStick.marker = mv
            mLineStick.data = cd
            mLineStick.invalidate()
            stopProgressBar()
        }
    }

    private fun startProgressBar() {
        val builder = AlertDialog.Builder(requireContext())
        val progressBar = ProgressBar(requireContext())

        builder.setView(progressBar)

        builder.setCancelable(false)

        alert = builder.create()
        alert.show()

        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.window?.setLayout(300, 300)

    }

    public fun stopProgressBar(){
        alert.dismiss()
    }

    private fun startWebSocket(){
        //здесь я запускаю websocket
        val request = Request.Builder().url(EnumListName.WEB_SOCKET.value).build()
        val listener = Listener(viewModelWebSocket, requireContext(), ticker)
        client = OkHttpClient()
        client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()

    }

}
