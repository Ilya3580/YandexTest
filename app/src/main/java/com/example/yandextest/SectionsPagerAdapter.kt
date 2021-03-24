package com.example.yandextest

import android.content.Context
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
        return 2
    }



}

class SectionsPagerAdapterChart(fm: FragmentManager, private var ticker : String)
    : FragmentPagerAdapter(fm) {

    val TAB_TITLES = arrayOf(
        R.string.chart,
        R.string.summary,
        R.string.forecasts,
        R.string.news,
        R.string.gloabalNews
            )

    override fun getItem(position: Int): Fragment {
        return if(position == 0) {
            FragmentChart.newInstance(ticker)
        }else if(position == 1){
            FragmentInformation.newInstance(ticker, "summary")
        }else{
            FragmentInformation.newInstance(ticker, "")
        }

    }

    override fun getCount(): Int {
        return 5
    }



}

