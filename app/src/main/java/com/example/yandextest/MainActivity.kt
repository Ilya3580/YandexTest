package com.example.yandextest


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var stocks: TextView
    private lateinit var favourite: TextView
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var tabs: TabLayout
    private lateinit var containerFragment: LinearLayout
    private lateinit var viewPager: ViewPager
    private var underSearchFragment = UnderSearchFragment.newInstance()
    private var underListFragment = UnderListFragment.newInstance()
    private lateinit var alert: AlertDialog
    private var flagPause = false

    private lateinit var _viewModelSearch: MyViewModel<Boolean>
    public var viewModelSearch: MyViewModel<Boolean>
        get() {
            return _viewModelSearch
        }
        set(value) {
            _viewModelSearch = value
        }

    private lateinit var _underViewModel: MyViewModel<Boolean>
    public var underViewModel: MyViewModel<Boolean>
        get() {
            return _underViewModel
        }
        set(value) {
            _underViewModel = value
        }

    private lateinit var _viewModelListUnderSearch: MyViewModel<HashMap<String, String>>
    public var viewModelListUnderSearch: MyViewModel<HashMap<String, String>>
        get() {
            return _viewModelListUnderSearch
        }
        set(value) {
            _viewModelListUnderSearch = value
        }

    private lateinit var _viewModelListHistory: MyViewModel<String>
    public var viewModelListHistory: MyViewModel<String>
        get() {
            return _viewModelListHistory
        }
        set(value) {
            _viewModelListHistory = value
        }

    private lateinit var _viewModelListPopular: MyViewModel<Boolean>
    public var viewModelListPopular: MyViewModel<Boolean>
        get() {
            return _viewModelListPopular
        }
        set(value) {
            _viewModelListPopular = value
        }


    private val startSizeTextView = 20f
    private val accentSizeTextView = 30f
    private var id = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        containerFragment = findViewById(R.id.underSearchFragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.underSearchFragment, underSearchFragment).commit()
        viewPager = findViewById(R.id.view_pager)
        _viewModelListHistory = MyViewModel<String>("")
        _viewModelListPopular = MyViewModel(true)

        startProgressBar()

        onCreateViewPager()
        startSettingStocksAndFavorite()

        if (savedInstanceState?.getBoolean("flagViewUnderSearchFragment") != null) {
            _viewModelSearch.user = savedInstanceState.getBoolean("flagViewUnderSearchFragment")
        }

        _viewModelSearch.getUsersValue().observe(this, Observer {
            if (it) {
                onClickArrowBack()
            } else {
                onClickSearchEditText()
            }
        })

        _underViewModel.getUsersValue().observe(this, Observer {
            if (it) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.underSearchFragment, underSearchFragment).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.underSearchFragment, underListFragment).commit()
            }
        })

        _viewModelListUnderSearch.getUsersValue().observe(this, Observer {
            if (it.count() > 0) {
                underListFragment.updateList(it)
            }
        })

        _viewModelListHistory.getUsersValue().observe(this, Observer {
            underSearchFragment.userUpdateQuestions(it)
        })

        _viewModelListPopular.getUsersValue().observe(this, Observer {
            if (it)
                underSearchFragment.updatePopularList()
        })


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("flagViewUnderSearchFragment", _viewModelSearch.user ?: true)

    }

    private fun onCreateViewPager() {
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = sectionsPagerAdapter

        tabs = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.customView = getTabView(0)
        tabs.getTabAt(1)?.customView = getTabView(1)

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
                if (position == 0 && position != id) {
                    onAccentStocks()
                    id = 0
                } else if (position == 1 && position != id) {
                    onAccentFavourite()
                    id = 1
                }
            }
        })
    }

    private fun getTabView(position: Int): View {

        val view = LayoutInflater.from(applicationContext).inflate(R.layout.custom, null)
        val textView = view.findViewById<TextView>(R.id.tabViewCell)
        textView.text =
            applicationContext.resources.getString(sectionsPagerAdapter.TAB_TITLES[position])
        if (position == 0) {
            stocks = textView
        } else {
            favourite = textView

        }
        return view

    }

    private fun startSettingStocksAndFavorite() {
        id = 0
        stocks.textSize = accentSizeTextView
        stocks.setTypeface(null, Typeface.BOLD)
        stocks.setTextColor(Color.BLACK)
        favourite.textSize = startSizeTextView
        favourite.setTypeface(null, Typeface.NORMAL)
        favourite.setTextColor(Color.GRAY)
    }

    private fun onAccentStocks() {
        _viewModelSearch.user = true
        animateStocksAndFavourite(stocks, favourite)
        stocks.setTypeface(null, Typeface.BOLD)
        favourite.setTypeface(null, Typeface.NORMAL)
    }

    private fun onAccentFavourite() {
        _viewModelSearch.user = true
        animateStocksAndFavourite(favourite, stocks)
        favourite.setTypeface(null, Typeface.BOLD)
        stocks.setTypeface(null, Typeface.NORMAL)

    }

    private fun animateStocksAndFavourite(accentTextView: TextView, notAccentTextView: TextView) {
        val animate = AnimateClass()
        animate.animateSizeZoom(accentTextView, startSizeTextView, accentSizeTextView)
        animate.animateSizeZoom(notAccentTextView, accentSizeTextView, startSizeTextView)
        animate.colorAnimateText(accentTextView, Color.GRAY, Color.BLACK)
        animate.colorAnimateText(notAccentTextView, Color.BLACK, Color.GRAY)
    }

    public fun onClickSearchEditText() {
        containerFragment.visibility = View.VISIBLE
        viewPager.visibility = View.GONE
        tabs.visibility = View.GONE
    }

    public fun onClickArrowBack() {
        containerFragment.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (!_viewModelSearch.user!!) {
            _viewModelSearch.user = true
        } else {
            super.onBackPressed()
        }

    }

    public fun startProgressBar() {
        val builder = AlertDialog.Builder(this)
        val progressBar = ProgressBar(this)

        builder.setView(progressBar)

        builder.setCancelable(false)

        alert = builder.create()
        alert.show()

        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.window?.setLayout(300, 300)

    }

    public fun stopProgressBar() {
        alert.dismiss()
    }


    override fun onStart() {
        super.onStart()
        if(flagPause){
            flagPause = false
            updateFavoriteTicker()
        }
    }

    public fun updateFavoriteTicker(){
        val functionsTickers = FunctionsTickers()
        val lst = functionsTickers.listFavoriteTickers(applicationContext)
        sectionsPagerAdapter.viewModelListFavorite.user = lst
        sectionsPagerAdapter.viewModelListFavorite.getUsersValue()
    }

    override fun onPause() {
        super.onPause()
        flagPause = true
    }

}




