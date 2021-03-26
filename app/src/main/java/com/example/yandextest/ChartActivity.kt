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

    private lateinit var viewPager : ViewPager
    private lateinit var arrowBack : ImageView
    private lateinit var sectionsPagerAdapter : SectionsPagerAdapterChart
    private lateinit var tabs : TabLayout
    private lateinit var lstTextViewTabs : ArrayList<TextView>
    private lateinit var cellInformation: CellInformation
    private lateinit var textViewTicker : TextView
    private lateinit var textViewCompany : TextView
    private lateinit var buttonStar : ImageButton
    private lateinit var functionsTickers: FunctionsTickers
    private lateinit var ticker : String
    private var flagFavorite : Boolean = false
    private val countTextViewTabs = 5
    private val startSizeTextView = 15f
    private val accentSizeTextView = 20f
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        supportActionBar?.hide()

        arrowBack = findViewById(R.id.arrowBack)
        viewPager = findViewById<ViewPager>(R.id.viewPagerChart)
        textViewTicker = findViewById(R.id.tickerAppBar)
        textViewCompany = findViewById(R.id.companyAppBar)
        buttonStar = findViewById(R.id.buttonStar)
        functionsTickers = FunctionsTickers()
        lstTextViewTabs = ArrayList()
        ticker = intent.extras?.get("TICKER").toString()

        onCreateViewPager()

        settingStart()

        arrowBack.setOnClickListener {
            onBackPressed()
        }

        flagFavorite = if(functionsTickers.checkStatusSharedPreference(ticker, applicationContext)){
            buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_on))
            true
        }else{
            buttonStar.setImageDrawable(resources.getDrawable(android.R.drawable.btn_star_big_off))
            false
        }

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
        val animateClass = AnimateClass()

        animateClass.animateSizeZoom(lstTextViewTabs[id],startSizeTextView,accentSizeTextView)
        animateClass.colorAnimateText(lstTextViewTabs[id], Color.GRAY, Color.BLACK)
    }

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

    private fun getTabView(position: Int): View {

        val view = LayoutInflater.from(applicationContext).inflate(R.layout.custom, null)
        val textView = view.findViewById<TextView>(R.id.tabViewCell)
        textView.textSize = startSizeTextView
        textView.text = applicationContext.resources.getString(sectionsPagerAdapter.TAB_TITLES[position])
        lstTextViewTabs.add(textView)
        return view

    }

    private fun loadCompanyInformation(){
        var url = EnumListName.QUOTE.value
        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    val classRequests = ClassRequests()
                    val lst = ArrayList<CellInformation>()
                    lst.add(CellInformation(intent.extras?.get("TICKER").toString()))
                    classRequests.parsTickersData(body, lst, applicationContext)
                    cellInformation = lst[lst.count() - 1]



                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
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

    private fun loadAppBar(){
        textViewTicker.text = cellInformation.ticker
        textViewCompany.text = cellInformation.company
    }

}