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

class FragmentChart(private var ticker : String) : Fragment() {

    private lateinit var myView : View
    private lateinit var lstTextViews : ArrayList<TextView>
    private lateinit var mLineChart : LineChart
    private lateinit var mLineStick : CandleStickChart
    private lateinit var alert : AlertDialog
    private lateinit var client : OkHttpClient
    private lateinit var lstEntry : ArrayList<Entry>
    private lateinit var lstCandleEntry: ArrayList<CandleEntry>
    private lateinit var viewModelWebSocket : MyViewModel<Boolean>
    private var textView : TextView? = null

    private lateinit var sPref : SharedPreferences
    private val CANDLE_OR_CHART = "CANDLE_OR_CHART"
    private var idCandleOrChart = 0

    private var enterCurrency = ""
    private var _currencySymbol = ""
    public var currencySymbol : String
        get() {return _currencySymbol}
        set(value) {
            _currencySymbol = value
            if(textView != null)
                textView!!.text = textView!!.text.toString() + enterCurrency + _currencySymbol

        }

    private var idElement= 0



    companion object {

        fun newInstance(ticker : String) = FragmentChart(ticker)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        myView = if(requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            enterCurrency = " "
            inflater.inflate(R.layout.fragment_chart_verical, container, false)
        }else{
            enterCurrency = "\n"
            inflater.inflate(R.layout.fragment_chart_horizontal, container, false)
        }
        mLineChart = myView.findViewById(R.id.graphChart)
        mLineStick = myView.findViewById(R.id.graphStick)
        textView = myView.findViewById(R.id.price)

        sPref = requireContext().getSharedPreferences(CANDLE_OR_CHART, Context.MODE_PRIVATE)

        val ed = sPref.edit()
        ed.putString(CANDLE_OR_CHART, "0")
        ed.apply()

        idCandleOrChart = if(sPref.contains(CANDLE_OR_CHART)){
            if(sPref.getString(CANDLE_OR_CHART, "") == "0"){
                0
            }else{
                1
            }
        }else{
            0
        }

        viewModelWebSocket = MyViewModel(false)
        lstEntry = ArrayList()

        lstTextViews = ArrayList()
        lstTextViews.add(myView.findViewById(R.id.day))
        lstTextViews.add(myView.findViewById(R.id.week))
        lstTextViews.add(myView.findViewById(R.id.month))
        lstTextViews.add(myView.findViewById(R.id.halfYear))
        lstTextViews.add(myView.findViewById(R.id.year))
        lstTextViews.add(myView.findViewById(R.id.tenYear))

        startSetting()

        settingGraph()
        updateData("D")

        viewModelWebSocket.getUsersValue().observe(viewLifecycleOwner, Observer {
            if(it) {
                updateData()
                viewModelWebSocket.user = false
            }
        })

        return myView
    }

    private fun startSetting(){
        lstTextViews[0].background = requireContext().resources.getDrawable(R.color.black)
        lstTextViews[0].setTextColor(Color.WHITE)

        for(i in (0 until lstTextViews.count())){
            lstTextViews[i].setOnClickListener {
                val animate = AnimateClass()

                animate.colorAnimateBackground(lstTextViews[idElement], Color.BLACK, Color.WHITE)
                animate.colorAnimateText(lstTextViews[idElement], Color.WHITE, Color.BLACK)

                animate.colorAnimateBackground(lstTextViews[i], Color.WHITE, Color.BLACK)
                animate.colorAnimateText(lstTextViews[i], Color.BLACK, Color.WHITE)

                idElement = i


                updateData(lstTextViews[i].text.toString())
            }
        }
    }

    private fun settingGraph(){
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

        if(idCandleOrChart == 0) {
            mLineStick.visibility = View.GONE
            mLineChart.visibility =  View.VISIBLE
        }else{
            mLineStick.visibility = View.VISIBLE
            mLineChart.visibility =  View.GONE
        }
    }

    private fun updateData(period : String){
        startProgressBar()
        var symbolPeriod = period
        when(period){
            "D" -> symbolPeriod = "5m"
            "W" -> symbolPeriod = "5m"
            "M" -> symbolPeriod = "5m"
            "6M" -> symbolPeriod = "1d"
            "1Y" -> symbolPeriod = "1d"
            "10Y" -> symbolPeriod = "1mo"
        }
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
                    parsHistoryData(body, period)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(requireContext())){
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    requireContext().startActivity(intent)
                }
            }
        })
    }

    private fun parsHistoryData(text : String, period: String){
        val json = JSONObject(text)
        val jsonArray = json.getJSONObject("items")
        var lstDataRequestChartClass = ArrayList<StickChartInformation>()
        for(i in (0 until jsonArray.names().length())){
            val nameJson = jsonArray.getJSONObject(jsonArray.names()[i].toString())
            val date = jsonArray.names()[i].toString().toInt()
            val high = nameJson.get("high").toString().toFloat()
            val low = nameJson.get("low").toString().toFloat()
            val open = nameJson.get("open").toString().toFloat()
            val close = nameJson.get("close").toString().toFloat()
            lstDataRequestChartClass.add(StickChartInformation(date, open, high, low, close))
        }

        lstDataRequestChartClass = StickChartInformation.convertListPeriod(period, lstDataRequestChartClass)
        if(idCandleOrChart == 0) {
            lstEntry = StickChartInformation.revertListEntry(lstDataRequestChartClass)
        }else{
            lstCandleEntry = StickChartInformation.revertListCandleEntry(lstDataRequestChartClass)
        }

        updateData()

        //startWebSocket()
    }

    private fun updateData(){
        if(idCandleOrChart == 0){
            updateDataChart()
        }else{
            updateDataCandle()
        }
    }

    private fun updateDataChart(){
        var maxX = Float.MIN_VALUE
        var minX = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minY = Float.MAX_VALUE

        for(i in lstEntry){
            if(i.x > maxX){
                maxX = i.x
            }

            if(i.x < minX){
                minX = i.x
            }

            if(i.y > maxY){
                maxY = i.y
            }

            if(i.y < minY){
                minY = i.y
            }
        }

        val setComp = LineDataSet(lstEntry,"")
        setComp.axisDependency = YAxis.AxisDependency.LEFT
        setComp.setDrawValues(false)
        setComp.setDrawCircleHole(false)
        setComp.setDrawCircles(false)

        setComp.color = Color.BLACK

        val dataSet = ArrayList<ILineDataSet>()
        dataSet.add(setComp)

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if(lstEntry.count() - 1 >= 0)
                textView!!.text = lstEntry[lstEntry.count() - 1].y.toString() + enterCurrency + _currencySymbol

            val data = LineData(dataSet)
            mLineChart.data = data
            mLineChart.setTouchEnabled(true)
            val mv = MarkerCustom(requireContext(), R.layout.custom_marker_view_layout,
                R.id.markerPrice, R.id.markerDate, maxX, minX, maxY, minY)
            mLineChart.marker = mv
            mLineChart.invalidate()
            stopProgressBar()
        }
    }

    private fun updateDataCandle(){

        val cds = CandleDataSet(lstCandleEntry, "Entries")
        cds.shadowColor = Color.DKGRAY
        cds.shadowWidth = 0.7f
        cds.increasingPaintStyle = Paint.Style.FILL
        cds.decreasingPaintStyle = Paint.Style.FILL
        cds.decreasingColor = Color.GREEN
        cds.increasingColor = Color.RED

        /*val setComp = LineDataSet(lstEntry,"")
        setComp.axisDependency = YAxis.AxisDependency.LEFT
        setComp.setDrawValues(false)
        setComp.setDrawCircleHole(false)
        setComp.setDrawCircles(false)

        setComp.color = Color.BLACK

        val dataSet = ArrayList<ILineDataSet>()
        dataSet.add(setComp)
*/
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if(lstEntry.count() - 1 >= 0)
                textView!!.text = lstEntry[lstEntry.count() - 1].y.toString() + enterCurrency + _currencySymbol

            val cd = CandleData(cds)
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
        val request = Request.Builder().url("wss://ws.finnhub.io?token=c15isdv48v6tvr5klgag").build()
        val listener = Listener(lstEntry, viewModelWebSocket, requireContext(), ticker)
        client = OkHttpClient()
        client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()
    }


    class Listener(private var lst : ArrayList<Entry>,
                   private var viewModel: MyViewModel<Boolean>, private var context: Context,
                   private var ticker : String
                   ) : WebSocketListener(){
        override fun onOpen(webSocket: WebSocket, response: Response) {
            //Log.d("TAGA", ticker)
            webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"$ticker\"}")
        }

        override fun onMessage(webSocket: WebSocket, message: String) {
            //Log.d("TAGA", message.toString())
            parsDateWebSocket(message)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if(!InternetFunctions.hasConnection(context)){
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }

            }
        }

        private fun parsDateWebSocket(message: String){
            if(message.contains("ping")){
                return
            }
            val json = JSONObject(message)
            val data = json.getJSONArray("data")
            val jsonI = data[data.length() -1] as JSONObject
            val price = jsonI.get("p").toString().toFloat()
            val time = jsonI.get("t").toString().toLong()/1000.toFloat()
            //Log.d("TAGA", price.toString())
            //Log.d("TAGA", time.toString())

            val handler = Handler(Looper.getMainLooper())
            handler.post {
                lst.add(Entry(time, price))
                viewModel.user = true
                viewModel.getUsersValue()
            }
        }
    }

}
