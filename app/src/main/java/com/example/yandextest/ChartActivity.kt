package com.example.yandextest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class ChartActivity : AppCompatActivity() {

    private lateinit var viewPager : ViewPager//где находятся фрагменты вкладок
    private lateinit var arrowBack : ImageView//стредака для возврата на предыдущую активность
    private lateinit var sectionsPagerAdapter : SectionsPagerAdapterChart//экземпляр класса наших вкладок
    private lateinit var tabs : TabLayout//pageAdapter в котором наши вкладки
    private lateinit var lstTextViewTabs : ArrayList<TextView>//список textview которые находятся в наших вкладках
    private lateinit var cellInformation: CellInformation//здесь будем хранить инфу о компании
    private lateinit var textViewTicker : TextView//textview который находится в appbar там будет отображаться тикер
    private lateinit var textViewCompany : TextView//textview который находится в appbar там будет отображаться название компании
    private lateinit var buttonStar : ImageButton//звездочка которая показывает находится ли тикер в избранном
    private lateinit var functionsTickers: FunctionsTickers//класс с вспомогательными функциями
    private lateinit var ticker : String//здесь будем хранить тикер
    private var flagFavorite : Boolean = false//это переменная отвечает находится ли тикер в избранном или нет
    private val countTextViewTabs = 5//количество вкладок

    //нужно для анимации
    private val startSizeTextView = 15f//начальный размер текста
    private val accentSizeTextView = 20f//конечный размер текста
    private var id = 0//id той вкладки на которой мы находимя

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        supportActionBar?.hide()

        //инициализирую объекты
        arrowBack = findViewById(R.id.arrowBack)
        viewPager = findViewById<ViewPager>(R.id.viewPagerChart)
        textViewTicker = findViewById(R.id.tickerAppBar)
        textViewCompany = findViewById(R.id.companyAppBar)
        buttonStar = findViewById(R.id.buttonStar)
        functionsTickers = FunctionsTickers()
        lstTextViewTabs = ArrayList()

        //сохраняю тикер которые нам передали из другой активности
        ticker = intent.extras?.get("TICKER").toString()

        internetCheck()

        onCreateViewPager()

        settingStart()

        //дествие при нажатии на стрелку
        arrowBack.setOnClickListener {
            onBackPressed()
        }

        //определяю какой должна быть звезда и соответственно переменная flagFavorite
        flagFavorite = if(functionsTickers.checkStatusSharedPreference(ticker, applicationContext)){
            buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_on))
            true
        }else{
            buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_off))
            false
        }

        //обрабатываю нажатие на звездочку и удаляю или добавляю в список избранных
        buttonStar.setOnClickListener {
            if(flagFavorite){
                flagFavorite = false
                buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_off))
                functionsTickers.delayTickersFavorites(ticker, applicationContext)
            }else{
                flagFavorite = true
                buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_on))
                functionsTickers.saveTickersFavorites(ticker, applicationContext)
            }
        }

        loadCompanyInformation()
    }

    private fun settingStart(){
        //начальные настройки для вкладки под индексом id, id = 0 изначально
        val animateClass = AnimateClass()
        animateClass.animateSizeZoom(lstTextViewTabs[id],startSizeTextView,accentSizeTextView)
        animateClass.colorAnimateText(lstTextViewTabs[id], Color.GRAY, Color.BLACK)
    }

    //здесь я настраиваю viewpager аналогично как в mainactivity
    private fun onCreateViewPager() {
        sectionsPagerAdapter = SectionsPagerAdapterChart(supportFragmentManager, ticker, applicationContext)
        viewPager.adapter = sectionsPagerAdapter

        tabs = findViewById(R.id.tabsChart)
        tabs.setupWithViewPager(viewPager)
        tabs.tabMode = TabLayout.MODE_SCROLLABLE

        for(i in (0 until countTextViewTabs)){
            tabs.getTabAt(i)?.customView = getTabView(i)
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if(position != id) {
                    val animateClass = AnimateClass()

                    animateClass.animateSizeZoom(lstTextViewTabs[position],startSizeTextView,accentSizeTextView)
                    animateClass.colorAnimateText(lstTextViewTabs[position], Color.GRAY, Color.BLACK)

                    animateClass.animateSizeZoom(lstTextViewTabs[id],accentSizeTextView,startSizeTextView)
                    animateClass.colorAnimateText(lstTextViewTabs[id], Color.BLACK, Color.GRAY)
                    id = position
                }


            }
        })
    }

    //здесь я кастомизирую textview вкладки
    private fun getTabView(position: Int): View {

        val view = LayoutInflater.from(applicationContext).inflate(R.layout.custom, null)
        val textView = view.findViewById<TextView>(R.id.tabViewCell)
        textView.textSize = startSizeTextView
        textView.text = applicationContext.resources.getString(sectionsPagerAdapter.TAB_TITLES[position])
        lstTextViewTabs.add(textView)
        return view

    }

    //здесь я подружаю инфу о компании
    private fun loadCompanyInformation(){
        //изменяю ссылку
        var url = EnumListName.QUOTE.value
        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    //здесь я разбираю json и получаю cellInformation
                    val classRequests = ClassRequests()
                    val lst = ArrayList<CellInformation>()
                    lst.add(CellInformation(intent.extras?.get("TICKER").toString()))
                    classRequests.parsTickersData(body, lst, applicationContext)
                    cellInformation = lst[lst.count() - 1]



                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        //здесь я получаю значек валюты и предаю его через sectionsPagerAdapter в fragmentchart
                        var currency : Currency
                        var symbolCurrency : String = ""
                        try {
                            if (cellInformation.currency != "null") {
                                currency = Currency.getInstance(cellInformation.currency)
                                symbolCurrency = currency.symbol
                            }
                        }catch (e : Exception){
                            Log.d("TAGA", cellInformation.currency)
                        }

                        sectionsPagerAdapter.setCurrency(symbolCurrency)
                        loadAppBar()
                    }
                }



            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(applicationContext)){
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    applicationContext.startActivity(intent)
                }
            }
        })
    }

    //устанавливаю тикер и название компании в appbar
    private fun loadAppBar(){
        textViewTicker.text = cellInformation.ticker
        textViewCompany.text = cellInformation.company
    }

    public fun internetCheck(){
        Thread(Runnable {
            while (true) {
                if (!InternetFunctions.hasConnection(applicationContext)) {
                    val intent = Intent(this, MainActivity :: class.java)
                    startActivity(intent)
                    break
                }
            }
        }).start()


    }

}