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

class UnderListFragment : Fragment {

    private lateinit var myView : View
    private lateinit var listView : ListView
    private var hashMap = HashMap<String, String>()
    private lateinit var mainActivity : MainActivity
    private lateinit var viewModelListHistory : MyViewModel<String>
    private lateinit var myContext: Context
    private lateinit var alert : AlertDialog

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
        listView.adapter = AdapterListView(ArrayList(hashMap.keys), ArrayList(hashMap.values), requireContext())
        try {
            mainActivity = activity as MainActivity
        }catch (e : Exception){
            Log.e("E", e.toString())
        }
        if(mainActivity != null){
             viewModelListHistory = mainActivity.viewModelListHistory
        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            Log.d("TAGA", ArrayList(hashMap.keys)[position])
            val ticker = ArrayList(hashMap.keys)[position]
            val functionsTickers = FunctionsTickers()
            functionsTickers.alertDialog("Add ticker: \"$ticker\"?", ticker, mainActivity, requireContext(),
                layoutInflater.inflate(R.layout.view_alert_dialog_add, null), viewModelListHistory)
            return@setOnItemLongClickListener true
        }

        return myView
    }

    public fun updateList(lst : HashMap<String, String>){
        this.hashMap = lst
        listView.adapter = AdapterListView(ArrayList(lst.keys), ArrayList(lst.values), myContext)
        listView.setOnItemClickListener { parent, view, position, id ->
            val ticker = ArrayList(this.hashMap.keys)[position]
            if(viewModelListHistory != null) {
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

}

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
