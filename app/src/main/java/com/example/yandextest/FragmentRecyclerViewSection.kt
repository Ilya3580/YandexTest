package com.example.yandextest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import java.io.IOException


class FragmentRecyclerViewSection(
    private var valueStocksOrFavorite: String,
    private var viewModelListFavorite: MyViewModel<ArrayList<String>>) : Fragment() {

    private lateinit var recyclerView : RecyclerView
    private lateinit var myView : View
    private lateinit var lst : ArrayList<CellInformation>
    private lateinit var mainActivity : MainActivity
    private lateinit var lstTickers : ArrayList<String>
    private var flagShowNotInternet = false

    companion object {
        fun newInstance(valueStocksOrFavorite : String, viewModelListFavorite : MyViewModel<ArrayList<String>>)
                = FragmentRecyclerViewSection(valueStocksOrFavorite, viewModelListFavorite)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_recycler_view, container, false)

        recyclerView = myView.findViewById(R.id.recyclerView)
        recyclerView.addItemDecoration(SpacesItemDecoration(50))
        recyclerView.layoutManager = LinearLayoutManager(context)

        retainInstance = true

        try{
            mainActivity = activity as MainActivity
        }catch (e : Exception){
        }

        lst = ArrayList()

        if(valueStocksOrFavorite == EnumListName.STOCKS.value){
            settingStocks()
        }else{
            settingFavorite()
            val custom = AdapterRecyclerViewFavorite(lst, viewModelListFavorite, viewLifecycleOwner,requireContext())

            custom.checkNewFavoriteTickers()

            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(custom)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerView)
            recyclerView.adapter = custom
        }

        return myView
    }

    private fun loadTickers(){
        val classRequests = ClassRequests()
        val url = EnumListName.STOCKS_TICKERS.value
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                lstTickers = classRequests.parsCheckURL(body.toString(), requireContext())
                lstTickers = classRequests.convertList(lstTickers)
                for(i in lstTickers){
                    val ci = classRequests.checkTicker(i, requireContext())
                    if(ci != null){
                        lst.add(ci)
                    }else{
                        lst.add(CellInformation(i))
                    }
                }
                convertUrlParsCellInformation()
            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(requireContext())){
                    notInternet()
                }
            }
        })
    }

    private fun convertUrlParsCellInformation(){
        val classRequests = ClassRequests()
        val countTickersOneRequest = 50
        var tickersLoad = ""
        var count = 0
        var number = 0
        val functionsTickers = FunctionsTickers()
        val lstFavorite = functionsTickers.listFavoriteTickers(requireContext())

        for(i in lstFavorite){
            var flagAdd = true
            for(j in lst){
                if(j.ticker == i){
                    flagAdd = false
                    break
                }
            }
            if(flagAdd) {
                val ci = classRequests.checkTicker(i, requireContext())
                if (ci != null) {
                    lst.add(ci)
                } else {
                    lst.add(CellInformation(i))
                }
            }
        }

        val lastCount = (lst.count() + (countTickersOneRequest - 1))/countTickersOneRequest
        for(i in lst){
            tickersLoad = if(count == 0) {
                i.ticker
            }else{
                tickersLoad + "," + i.ticker
            }
            count++
            if(count == countTickersOneRequest || countTickersOneRequest * number + count == lst.count()){
                count = 0
                number ++
                loadListCellInformation(tickersLoad, number, lastCount)

            }

        }



    }

    private fun loadListCellInformation(lstTicker : String, number : Int, lastNumber : Int){
        var url = EnumListName.QUOTE.value
        url = url.replace(EnumListName.MY_SYMBOL.value, lstTicker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    val classRequests = ClassRequests()
                    classRequests.parsTickersData(body, lst, requireContext())
                    if(number == lastNumber) {
                        classRequests.saveList(requireContext(), lstTickers)
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            val custom = AdapterRecyclerViewStocks(lst, viewModelListFavorite, viewLifecycleOwner, requireContext())
                            recyclerView.adapter = custom
                            if(mainActivity != null) {
                                val vm = mainActivity.viewModelListPopular
                                vm.user = true
                                vm.getUsersValue()
                                mainActivity.stopProgressBar()
                            }
                        }
                    }
                }



            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(requireContext())){
                    notInternet()
                }
            }
        })
    }

    private fun settingStocks(){
        //TODO убрать комент
        //lst.clear()
        //loadTickers()
        notInternet()

    }

    private fun settingFavorite(){
        val functionsTickers = FunctionsTickers()
        val lstPreview = functionsTickers.listFavoriteTickers(requireContext())
        lst.clear()
        for(i in lstPreview){
            val classRequests = ClassRequests()
            val ci = classRequests.checkTicker(i, requireContext())
            if(ci != null){
                lst.add(ci)
            }else{
                lst.add(CellInformation(i))
            }
        }

    }

    private fun notInternet(){
        if(!flagShowNotInternet) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                showSaveData()
                if (mainActivity != null)
                    mainActivity.stopProgressBar()
                val alert = InternetFunctions.alertDialog(requireContext())
                Thread(Runnable {
                    while (true) {
                        if (InternetFunctions.hasConnection(requireContext())) {
                            val handler1 = Handler(Looper.getMainLooper())
                            handler1.post {
                                alert.create().dismiss()
                                settingStocks()
                                if (mainActivity != null)
                                    mainActivity.startProgressBar()
                            }
                            break
                        }
                    }
                })/*.start()*///TODO убрать комент
            }
            flagShowNotInternet = true
        }
    }

    private fun showSaveData(){
        if(valueStocksOrFavorite == EnumListName.STOCKS.value){
            var listCache = ArrayList<CellInformation>()
            val classRequests = ClassRequests()
            listCache = classRequests.readListCellInformation(requireContext())
            val custom = AdapterRecyclerViewStocks(listCache, viewModelListFavorite, viewLifecycleOwner, requireContext())
            recyclerView.adapter = custom
        }
    }

}




