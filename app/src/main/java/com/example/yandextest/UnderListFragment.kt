package com.example.yandextest

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import java.io.IOException
import java.lang.Exception

//это клас выводит результаты поиска в список. Список под поиском
class UnderListFragment : Fragment {

    private lateinit var myView : View
    private lateinit var listView : ListView
    private var hashMap = HashMap<String, String>()//здесь храним тикер и название компании
    private var mainActivity : MainActivity? = null
    private lateinit var viewModelListHistory : MyViewModel<String>//это нужно чтобы обновить список истории при клике на какйто элемент
    private lateinit var myContext: Context

    companion object {

        fun newInstance() = UnderListFragment()
        fun newInstance(hashMap: HashMap<String, String>) = UnderListFragment(hashMap)
    }

    constructor(){}

    constructor(hashMap: HashMap<String, String>){
        this.hashMap = hashMap
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_list_view, container, false)
        myContext = requireContext()
        listView = myView.findViewById(R.id.listViewSearch)
        retainInstance = true

        //утанавливаю список в listview
        listView.adapter = AdapterListView(ArrayList(hashMap.keys), ArrayList(hashMap.values), requireContext())
        try {
            mainActivity = activity as MainActivity
        }catch (e : Exception){
            Log.e("E", e.toString())
        }
        if(mainActivity != null){
             viewModelListHistory = mainActivity!!.viewModelListHistory
        }

        //обрабатываю длинный клик на элемент списка
        listView.setOnItemLongClickListener { parent, view, position, id ->
            Log.d("TAGA", ArrayList(hashMap.keys)[position])
            val ticker = ArrayList(hashMap.keys)[position]
            val functionsTickers = FunctionsTickers()
            if(mainActivity != null){
                //вывожу диалоговое окно с уточнение добавить элемент в избранное
                functionsTickers.alertDialog("Add ticker: \"$ticker\"?", ticker, mainActivity!!, requireContext(),
                    layoutInflater.inflate(R.layout.view_alert_dialog_add, null), viewModelListHistory)
            }

            return@setOnItemLongClickListener true
        }

        return myView
    }

    //эта функция обновляет элементы списка
    public fun updateList(lst : HashMap<String, String>){
        this.hashMap = lst
        listView.adapter = AdapterListView(ArrayList(lst.keys), ArrayList(lst.values), myContext)

        //обрабатываю нажатие на элемент списка. Запускаю активность и передаю туда тикер
        listView.setOnItemClickListener { parent, view, position, id ->
            val ticker = ArrayList(this.hashMap.keys)[position]
            viewModelListHistory.user = ticker
            viewModelListHistory.getUsersValue()
            if(InternetFunctions.hasConnection(requireContext())){
                val intent = Intent(context, ChartActivity::class.java)
                intent.putExtra("TICKER", ticker)
                requireContext().startActivity(intent)
            }else{
                InternetFunctions.alertDialog(requireContext())
            }
        }
    }

}

//это адаптер listview здесь все стандартно
class AdapterListView(private var tickers : ArrayList<String>,private var companyName : ArrayList<String> , context : Context)
    : ArrayAdapter<String>(context, R.layout.item_listview_search, tickers){

    private lateinit var view : View
    private lateinit var textViewTicker : TextView
    private lateinit var textViewCompanyName : TextView

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        view = LayoutInflater.from(context).inflate(R.layout.item_listview_search, parent, false)
        textViewTicker = view.findViewById(R.id.tickerListView)
        textViewCompanyName = view.findViewById(R.id.nameCompanyListView)

        textViewTicker.text = tickers[position]
        textViewCompanyName.text = companyName[position]
        return view
    }
}
