package com.example.yandextest

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread

//этот фрагмент отвечает за списки stock и favorite
//в кончтрукторе мы получаем viewmodel и тип
//viewmodel срабатывает при изменнеие спика favorite
//тип отвечает какой должен быть fragment
class FragmentRecyclerViewSection(
    private var valueStocksOrFavorite: String,
    private var viewModelListFavorite: MyViewModel<ArrayList<String>>,
    private var viewModelListWebSocket : MyViewModel<ArrayList<StickWebSocket>>,
    private var viewModelInternet : MyViewModel<Boolean>,
    private var webSocket: WebSocket) : Fragment() {

    private var alertNotInternet : AlertDialog? = null
    private lateinit var recyclerView : RecyclerView//recyclerview с нашими объектами
    private lateinit var myView : View
    private lateinit var lst : ArrayList<CellInformation>//список в котором хранится вся инфа о компаниях
    private var mainActivity : MainActivity? = null
    private lateinit var lstTickers : ArrayList<String>//список тикеров
    private var flagShowNotInternet = false//это нужно чтобы показывать сообщение один раз что нет интернета
    //потому что может произойти так что интренет пропадет когда будут два потоко обрабатывать информацию и оба дадут понять что нет интрнета

    public fun setWebSocket(webSocket : WebSocket){
        this.webSocket = webSocket
        try {
            if (valueStocksOrFavorite == EnumListName.STOCKS.value) {
                (recyclerView.adapter as AdapterRecyclerViewStocks).setWebSocket(webSocket)
            } else {
                (recyclerView.adapter as AdapterRecyclerViewFavorite).setWebSocket(webSocket)
            }
        }catch (e : Exception){

        }

    }


    companion object {
        fun newInstance(valueStocksOrFavorite : String, viewModelListFavorite : MyViewModel<ArrayList<String>>, viewModelListWebSocket : MyViewModel<ArrayList<StickWebSocket>>, viewModelInternet : MyViewModel<Boolean>, webSocket: WebSocket)
                = FragmentRecyclerViewSection(valueStocksOrFavorite, viewModelListFavorite, viewModelListWebSocket, viewModelInternet, webSocket)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_recycler_view, container, false)

        //инициализирую recycler view и устанавливаю отступы и Layout Manager
        recyclerView = myView.findViewById(R.id.recyclerView)
        recyclerView.addItemDecoration(SpacesItemDecoration(50))
        recyclerView.layoutManager = LinearLayoutManager(context)
        //создаю экземпляр списка
        lst = ArrayList()
        retainInstance = true

        try{
            mainActivity = activity as MainActivity
        }catch (e : Exception){
        }


        //здесь я определяю какой тип списка должен быть у нас
        if(valueStocksOrFavorite == EnumListName.STOCKS.value){
            settingStocks()
            internetCheck()
        }else{
            settingFavorite()
            //список favorite я создаю сразу потому что список избранных тикеров сохранен в памяти устройства
            val custom = AdapterRecyclerViewFavorite(lst, viewModelListFavorite, viewLifecycleOwner,requireContext(), viewModelListWebSocket,webSocket)

            //эту функцию я вызываю чтобы в recycler view favorite был создан слушатель изменения списка favorite
            custom.checkNewFavoriteTickers()

            //этот класс я использую для того чтобы можно было перетаскивать элементы и удалять смахиванием
            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(custom)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerView)
            recyclerView.adapter = custom
        }



        return myView
    }

    private fun loadTickers(){
        //здесь я изменяю ссылку и пдружаю тикеры
        val classRequests = ClassRequests()
        val url = EnumListName.STOCKS_TICKERS.value
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                //в этой строчке я конвертирую json в список тикеров
                lstTickers = classRequests.parsCheckURL(body.toString(), requireContext())
                //эта функция удаляет из спика индексы и валютные пары я это делаю потому что не во всех api которые я использую потдерживается этот тип тикеров
                lstTickers = classRequests.convertList(lstTickers)
                //далее я сохраняю ту информацию которая у нас есть в список, а позже я её обновляю
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
            }
        })
    }


    var number = 0//эта переменная нужна чтобы узнать что последний запрос был выполнен
    //в api mboum можно отправить на получание инфы 50 тикеров здесь я набираю 50 тикеров и отправляю
    private fun convertUrlParsCellInformation(){
        number = 0
        //я добавляю все тикеры в tickersLoad через запятую а потом отправляю запрос
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
                loadListCellInformation(tickersLoad, lastCount)

            }

        }



    }

    private fun loadListCellInformation(lstTicker : String, lastNumber : Int){
        var url = EnumListName.QUOTE.value
        url = url.replace(EnumListName.MY_SYMBOL.value, lstTicker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    //здесь я обрабатываю список тикеров которые мы получаем
                    val classRequests = ClassRequests()
                    //в этой функции я их и обрабатываю и сохраняю в память приложения
                    classRequests.parsTickersData(body, lst, requireContext())
                    number++
                    if(number == lastNumber) {
                        //эта кусок кода сработает по завершению последнего запроса
                        //здесь я их получаю из памяти и сохраняю в список
                        classRequests.saveList(requireContext(), lstTickers)
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            val custom = AdapterRecyclerViewStocks(lst, viewModelListFavorite, viewLifecycleOwner, requireContext(), viewModelListWebSocket, webSocket)
                            recyclerView.adapter = custom
                            if(mainActivity != null) {
                                val vm = mainActivity!!.viewModelListPopular
                                vm.user = true
                                vm.getUsersValue()
                                mainActivity!!.stopProgressBar()
                            }
                        }
                    }
                }



            }

            override fun onFailure(call: Call, e: IOException) {
            }
        })
    }

    private fun settingStocks(){
        //отчищаю список тикеров а потом отправляю его на обновление
        lst.clear()
        loadTickers()
    }

    private fun settingFavorite(){
        //здесь я настраиваю favorite получаю из памяти тикер и инфу о нем и добавляю в список
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

    //запускаем поток который будет ослеживать есть ли интернет или нету если нету то он вызовет функцию notInternet
    public fun internetCheck(){
        Thread(Runnable {
            while (true) {
                if (!InternetFunctions.hasConnection(requireContext())) {
                    notInternet()
                }
            }
        }).start()


    }

    //в этой функции показывается диалоговое окно что нет интернета и запускается поток который отслеживает его появление
    private fun notInternet(){
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if(!flagShowNotInternet) {
                flagShowNotInternet = true
                showSaveData()
                if (mainActivity != null)
                    mainActivity!!.stopProgressBar()
                alertNotInternet = InternetFunctions.alertDialog(requireContext()).create()
                //запускаю поток который проверяет появился интрнет или нет
                //если появился то обновляю данные по новому
                Thread(Runnable {
                    while (true) {
                        if (InternetFunctions.hasConnection(requireContext())) {
                            val handler1 = Handler(Looper.getMainLooper())
                            handler1.post {
                                flagShowNotInternet = false
                                viewModelInternet.user = true
                                viewModelInternet.getUsersValue()
                                if(alertNotInternet != null) {
                                    alertNotInternet!!.dismiss()
                                }
                                settingStocks()//я вызываю только настройки вкладки stocks потому что там я загружаю данные для двух вкладок

                                if (mainActivity != null)
                                    mainActivity!!.startProgressBar()
                            }
                            break
                        }
                    }
                }).start()
            }
        }
    }

    //если нет итернета то чтобы списки не были пустыми я показываю то что было сохраненно в последний раз
    private fun showSaveData(){
        if(valueStocksOrFavorite == EnumListName.STOCKS.value){
            var listCache = ArrayList<CellInformation>()
            val classRequests = ClassRequests()
            listCache = classRequests.readListCellInformation(requireContext())
            val custom = AdapterRecyclerViewStocks(listCache, viewModelListFavorite, viewLifecycleOwner, requireContext(), viewModelListWebSocket, webSocket)
            recyclerView.adapter = custom
        }
    }


}




