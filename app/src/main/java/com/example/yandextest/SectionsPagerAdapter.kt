package com.example.yandextest

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    private lateinit var _viewModelListFavorite : MyViewModel<ArrayList<String>>
    public var viewModelListFavorite : MyViewModel<ArrayList<String>>
        get(){ return _viewModelListFavorite }
        set(value) { _viewModelListFavorite = value }

    init{
        val functionsTickers = FunctionsTickers()
        _viewModelListFavorite = MyViewModel<ArrayList<String>>(functionsTickers.listFavoriteTickers(context))
    }

    val TAB_TITLES = arrayOf(
            R.string.tab_text_1,
            R.string.tab_text_2)

    override fun getItem(position: Int): Fragment {
        return if(position == 0)
        {
            FragmentRecyclerViewSection.newInstance(EnumListName.STOCKS.value, _viewModelListFavorite)
        }else{
            FragmentRecyclerViewSection.newInstance(EnumListName.FAVORITE.value, _viewModelListFavorite)
        }

    }
    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.count()
    }



}

class SectionsPagerAdapterChart(fm: FragmentManager, private var ticker : String, private var context: Context)
    : FragmentPagerAdapter(fm) {

    val TAB_TITLES = arrayOf(
        R.string.chart,
        R.string.summary,
        R.string.recommendation,
        R.string.newsSentiment,
        R.string.news
            )

    var arrayFragment = ArrayList<Fragment>()
    private var currencySymbol : String = ""

    override fun getItem(position: Int): Fragment {
        if(arrayFragment.count() == 0){
            for(i in (0 until 5)){
                val fragment = if(i == 0) {
                    val fragmentChart =  FragmentChart.newInstance(ticker)
                    fragmentChart.currencySymbol = currencySymbol
                    fragmentChart
                }else{
                    FragmentInformation.newInstance(ticker, context.resources.getString(TAB_TITLES[i]))
                }

                arrayFragment.add(fragment)
            }
        }
        return arrayFragment[position]

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

    }

    public fun setCurrency(symbolCurrency : String){
        if(arrayFragment.count() > 0) {
            (arrayFragment[0] as FragmentChart).currencySymbol = symbolCurrency
            currencySymbol = symbolCurrency
        }else{
            currencySymbol = symbolCurrency
        }
    }


    override fun getCount(): Int {
        return TAB_TITLES.count()
    }

}

