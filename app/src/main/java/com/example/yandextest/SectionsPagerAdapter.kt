package com.example.yandextest

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.Observer

//эти два класса ответственны за вкладки в активностях
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, lifecycleOwner: LifecycleOwner)
    : FragmentPagerAdapter(fm) {

    //viewmodel который отвечает за списко favorite мы его передадим в список stock и favorite
    private lateinit var _viewModelListFavorite : MyViewModel<ArrayList<String>>
    public var viewModelListFavorite : MyViewModel<ArrayList<String>>
        get(){ return _viewModelListFavorite }
        set(value) { _viewModelListFavorite = value }

    private lateinit var viewModelListWebSocket: MyViewModel<ArrayList<StickWebSocket>>
    private lateinit var viewModelInternet : MyViewModel<Boolean>
    private lateinit var webSocket: WebSocket
    private lateinit var request : Request
    private lateinit var listener : Listener
    private lateinit var client : OkHttpClient
    private var fragmentS : Fragment? = null
    private var fragmentF : Fragment? = null

    init{
        //здесь я сохраняю спиок favorite viewmodel из директории приложения
        val functionsTickers = FunctionsTickers()
        _viewModelListFavorite = MyViewModel<ArrayList<String>>(functionsTickers.listFavoriteTickers(context))
        viewModelListWebSocket = MyViewModel(ArrayList())

        viewModelInternet = MyViewModel(true)

        //здесь я отслеживаю пропадает ли интернет и вызываю соответствующую функцию отслежиываю из websocket
        viewModelInternet.getUsersValue().observe(lifecycleOwner, androidx.lifecycle.Observer {
            if(it){
                startWebSocket()
            }else{
                if(fragmentS != null){
                    (fragmentS as FragmentRecyclerViewSection).notInternet()
                }
            }
        })


    }

    public fun startWebSocket(){
        //здесь я запускаю websocket
        request = Request.Builder().url(EnumListName.WEB_SOCKET.value).build()
        listener = Listener(viewModelListWebSocket, viewModelInternet, context)
        client = OkHttpClient()
        webSocket = client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()


    }

    //названия вкладок
    val TAB_TITLES = arrayOf(
            R.string.tab_text_1,
            R.string.tab_text_2)

    override fun getItem(position: Int): Fragment {
        //здесь я определяю какой списко в какую вкладку добавить
        //списко favorite наследуются от stocks. На вопрос почему два разных класса для похожих списков я отвечу, что я хотел сделать так чтобы
        //можно было перетаскивать элементы в списке favorite в нужном порядке и удалять свайпом в сторону
        //пожалуйста посмотри как это выглядит в приложении потому что получилось красиво
        return if(position == 0)
        {
            if(fragmentS == null) {
                fragmentS = FragmentRecyclerViewSection.newInstance(
                    EnumListName.STOCKS.value,
                    _viewModelListFavorite,
                    viewModelListWebSocket,
                    viewModelInternet,
                    webSocket
                )
            }
            fragmentS!!
        }else{
            if(fragmentF == null) {
                fragmentF = FragmentRecyclerViewSection.newInstance(
                    EnumListName.FAVORITE.value,
                    _viewModelListFavorite,
                    viewModelListWebSocket,
                    viewModelInternet,
                    webSocket
                )
            }
            fragmentF!!
        }

    }
    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.count()
    }



}

//в этом классе почти все также как и в первом только здесь я сохраняю фрагменты в список, чтобы они каждый раз не загружались с интернета
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

    //отключаю удаление фрагмента
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

    }

    //эта функия нужна для того чтобы когда мы в классе зарузим информацию о компании мы предали значе валюты во фрагмент с графиком
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

