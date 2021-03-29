package com.example.yandextest


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private lateinit var stocks: TextView //text view вкладки stocks
    private lateinit var favourite: TextView//text view вкладки favourite
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter//экземпляр класса наших вкладок
    private lateinit var tabs: TabLayout//pageAdapter в котором наши вкладки
    private lateinit var containerFragment: LinearLayout//контейнер фрагментов которые появляются под поиском
    private lateinit var viewPager: ViewPager//где находятся фрагменты вкладок

    //фрагменты под поиском один с двумя вертикальными списками, другой с одним вертикальным
    private var underSearchFragment = UnderSearchFragment.newInstance()
    private var underListFragment = UnderListFragment.newInstance()

    //диалоговое окно с progressbar
    private lateinit var alert: AlertDialog
    private var flagPause = false//этот флаг нас уведомляет что была вызвана функция onPause.
        //Это нужно для того чтобы при возвращение на активность мы проверяли не обновился ли списко Favorite

    //это viewmodel уведомляет о том что пользователь нажал на поиск и нужно показать вместо viewpager контейнер с фрагментами
    private lateinit var _viewModelSearch: MyViewModel<Boolean>
    public var viewModelSearch: MyViewModel<Boolean>
        get() {
            return _viewModelSearch
        }
        set(value) {
            _viewModelSearch = value
        }
    //это viewmodel уведомляет о том какой надо показать фрагмент с двумя вертикальными списками или с одним горизонтальным
    private lateinit var _underViewModel: MyViewModel<Boolean>
    public var underViewModel: MyViewModel<Boolean>
        get() {
            return _underViewModel
        }
        set(value) {
            _underViewModel = value
        }

    //это viewmodel уведомляет о том что результаты вертикального списка изменились
    private lateinit var _viewModelListUnderSearch: MyViewModel<HashMap<String, String>>
    public var viewModelListUnderSearch: MyViewModel<HashMap<String, String>>
        get() {
            return _viewModelListUnderSearch
        }
        set(value) {
            _viewModelListUnderSearch = value
        }
    //это viewmodel уведомляет о том что список исотрии горизонтального списка изменился
    private lateinit var _viewModelListHistory: MyViewModel<String>
    public var viewModelListHistory: MyViewModel<String>
        get() {
            return _viewModelListHistory
        }
        set(value) {
            _viewModelListHistory = value
        }
    //это viewmodel уведомляет о том что список популярного горизонтального списка изменился
    private lateinit var _viewModelListPopular: MyViewModel<Boolean>
    public var viewModelListPopular: MyViewModel<Boolean>
        get() {
            return _viewModelListPopular
        }
        set(value) {
            _viewModelListPopular = value
        }

    //это начальный и конечный размер текста нужно для анимации
    private val startSizeTextView = 20f
    private val accentSizeTextView = 30f
    //id вкладки
    private var id = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        //здесь я инициализирую выше перечисденные переменные данные но не все, некоторые viewmodel устанавливаются с фрагментов
        containerFragment = findViewById(R.id.underSearchFragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.underSearchFragment, underSearchFragment).commit()
        viewPager = findViewById(R.id.view_pager)
        _viewModelListHistory = MyViewModel<String>("")
        _viewModelListPopular = MyViewModel(true)


        startProgressBar()

        onCreateViewPager()
        startSettingStocksAndFavorite()

        //здесь я проверяю был ли открыт поиск до поворта экрана
        if (savedInstanceState?.getBoolean("flagViewUnderSearchFragment") != null) {
            _viewModelSearch.user = savedInstanceState.getBoolean("flagViewUnderSearchFragment")
        }

        //далее идут слушатели viewmodel
        _viewModelSearch.getUsersValue().observe(this, Observer {
            //здесь я отслеживаю был ли нажат поиск
            if (it) {
                onClickArrowBack()
            } else {
                onClickSearchEditText()
            }
        })

        _underViewModel.getUsersValue().observe(this, Observer {
            //здесь я отслеживаю какой фрагмент установить под поиск
            if (it) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.underSearchFragment, underSearchFragment).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.underSearchFragment, underListFragment).commit()
            }
        })

        _viewModelListUnderSearch.getUsersValue().observe(this, Observer {
            //здесь я обновляю результаты поиска
            if (it.count() > 0) {
                underListFragment.updateList(it)
            }
        })

        _viewModelListHistory.getUsersValue().observe(this, Observer {
            //здесь я обновляю результаты истори запросов
            underSearchFragment.userUpdateQuestions(it)
        })

        _viewModelListPopular.getUsersValue().observe(this, Observer {
            //здесь я обновляю популярные тикеры
            if (it)
                underSearchFragment.updatePopularList()
        })


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //здесь я сохраняю перед поворотм экрана был ли открыт поиск
        outState.putBoolean("flagViewUnderSearchFragment", _viewModelSearch.user ?: true)

    }

    private fun onCreateViewPager() {
        //здесь я настраиваю viewpager
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, this)
        viewPager.adapter = sectionsPagerAdapter

        tabs = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        //здесь я устанавливаю кастомные textview
        tabs.getTabAt(0)?.customView = getTabView(0)
        tabs.getTabAt(1)?.customView = getTabView(1)

        //слушатель viewpager
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

                //запускаю анимации для вкладок
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
        //здесь я кастомизирую вкладки
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
        //это стартовые настройки
        id = 0
        stocks.textSize = accentSizeTextView
        stocks.setTypeface(null, Typeface.BOLD)
        stocks.setTextColor(Color.BLACK)
        favourite.textSize = startSizeTextView
        favourite.setTypeface(null, Typeface.NORMAL)
        favourite.setTextColor(Color.GRAY)
    }

    private fun onAccentStocks() {
        //это функция выделяет вкладку stocks
        _viewModelSearch.user = true
        animateStocksAndFavourite(stocks, favourite)
        stocks.setTypeface(null, Typeface.BOLD)
        favourite.setTypeface(null, Typeface.NORMAL)
    }

    private fun onAccentFavourite() {
        //это функция выделяет вкладку Favourite
        _viewModelSearch.user = true
        animateStocksAndFavourite(favourite, stocks)
        favourite.setTypeface(null, Typeface.BOLD)
        stocks.setTypeface(null, Typeface.NORMAL)

    }

    private fun animateStocksAndFavourite(accentTextView: TextView, notAccentTextView: TextView) {
        //эта функция отвечает за анимации вкладок, она для обеих вкладок едина, но выделяет она ту вкладку которую передали параметром accentTextView
        val animate = AnimateClass()
        animate.animateSizeZoom(accentTextView, startSizeTextView, accentSizeTextView)
        animate.animateSizeZoom(notAccentTextView, accentSizeTextView, startSizeTextView)
        animate.colorAnimateText(accentTextView, Color.GRAY, Color.BLACK)
        animate.colorAnimateText(notAccentTextView, Color.BLACK, Color.GRAY)
    }

    public fun onClickSearchEditText() {
        //это функция ответственна за нажатие на поиск
        containerFragment.visibility = View.VISIBLE
        viewPager.visibility = View.GONE
        tabs.visibility = View.GONE
    }

    public fun onClickArrowBack() {
        //это функция ответственна за нажатие на стрелку назад
        containerFragment.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        //здесь я проверяю был ли выделен поиск, если да то сначала закрою его
        if (!_viewModelSearch.user!!) {
            _viewModelSearch.user = true
        } else {
            super.onBackPressed()
        }

    }

    public fun startProgressBar() {
        //здесь я запускаю progressbar в диалоговом окне
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
        //здесь я отключаю progressbar
        alert.dismiss()
    }


    override fun onStart() {
        super.onStart()
        //здесь я проверяю если была вызвана функция onPause то мы могли вернуться с другой активности и надо проверить списко favorite
        if(flagPause){
            flagPause = false
            sectionsPagerAdapter.startWebSocket()
            updateFavoriteTicker()
        }
    }

    public fun updateFavoriteTicker(){
        //здесь я обновляю списко favorite и вызываю viewmodel который находится во вкладках он описан в другом классе
        val functionsTickers = FunctionsTickers()
        val lst = functionsTickers.listFavoriteTickers(applicationContext)
        sectionsPagerAdapter.viewModelListFavorite.user = lst
        sectionsPagerAdapter.viewModelListFavorite.getUsersValue()
    }

    override fun onPause() {
        super.onPause()
        //это нужно для отслеживания favorite
        flagPause = true
    }

}




