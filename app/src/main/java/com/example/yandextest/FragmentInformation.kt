package com.example.yandextest

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
import org.json.JSONObject
import java.io.IOException

class FragmentInformation(private var ticker : String, private var type : String) : Fragment() {

    private lateinit var myView : View
    private lateinit var listView : ListView
    private lateinit var listInformation : ArrayList<String>

    companion object {

        fun newInstance(ticker : String, type : String) = FragmentInformation(ticker, type)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_information, container, false)

        retainInstance = true
        listView = myView.findViewById(R.id.list_view)

        var url = ""

        when(type){
            requireContext().resources.getString(R.string.summary) -> url = EnumListName.SUMMARY.value
            requireContext().resources.getString(R.string.recommendation) -> url = EnumListName.RECOMMENDATION.value
            requireContext().resources.getString(R.string.newsSentiment) -> url = EnumListName.NEWS_SENTIMENTS.value
            requireContext().resources.getString(R.string.news) -> url =  EnumListName.NEWS.value
        }

        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    listInformation = ArrayList()
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        when(type){
                            requireContext().resources.getString(R.string.summary) -> listInformation = parsDataSummary(body)
                            requireContext().resources.getString(R.string.recommendation) -> listInformation = parsDataRecommendation(body)
                            requireContext().resources.getString(R.string.newsSentiment) -> listInformation = parsDataNewsSentiment(body)
                            requireContext().resources.getString(R.string.news) -> listInformation = parsDataNews(body)
                        }
                        listView.adapter = Adapter(listInformation, requireContext())
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(requireContext())){
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    requireContext().startActivity(intent)
                }
            }
        })
        return myView
    }

    private fun parsDataSummary(text : String) : ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        lst.add(json.get("longBusinessSummary").toString())
        return lst
    }

    private fun parsDataRecommendation(text : String): ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        val jsonArray = json.getJSONArray("trend")
        for(i in (0 until jsonArray.length())){
            var strElement = ""
            strElement+= "period : " + (jsonArray[i] as JSONObject).get("period") + "\n"
            strElement+= "strong buy : " + (jsonArray[i] as JSONObject).get("strongBuy") + "\n"
            strElement+= "buy : " + (jsonArray[i] as JSONObject).get("buy") + "\n"
            strElement+= "hold : " + (jsonArray[i] as JSONObject).get("hold") + "\n"
            strElement+= "sell : " + (jsonArray[i] as JSONObject).get("sell") + "\n"
            strElement+= "strong sell : " + (jsonArray[i] as JSONObject).get("strongSell")
            lst.add(strElement)
        }
        return lst
    }

    private fun parsDataNewsSentiment(text: String) : ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        var strElement = "buzz\n"
        var jsonChild = json.getJSONObject("buzz")
        strElement+= "   articles in last week : " + jsonChild.get("articlesInLastWeek") + "\n"
        strElement+= "   buzz : " + jsonChild.get("buzz") + "\n"
        strElement+= "   weekly average : " + jsonChild.get("weeklyAverage") + "\n\n"

        strElement+= "company news score : " + json.get("companyNewsScore") + "\n"
        strElement+= "sector average bullish percent : " + json.get("sectorAverageBullishPercent") + "\n"
        strElement+= "sector average news score : " + json.get("sectorAverageNewsScore") + "\n\n"

        strElement+="sentiment\n"
        jsonChild = json.getJSONObject("sentiment")
        strElement+= "   bearish percent : " + jsonChild.get("bearishPercent") + "\n"
        strElement+= "   bullish percent : " + jsonChild.get("bullishPercent") + "\n"

        lst.add(strElement)
        return lst
    }

    private fun parsDataNews(text : String) : ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        val jsonArray = json.getJSONArray("item")
        for(i in (0 until jsonArray.length())){
            lst.add((jsonArray[i] as JSONObject).get("description").toString())
        }

        return lst
    }


}

class Adapter(private var items : ArrayList<String>, context : Context)
    : ArrayAdapter<String>(context, R.layout.element_listview_information, items){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.element_listview_information, parent, false)

        val textView = view.findViewById<TextView>(R.id.textViewInformation)
        textView.text = items[position]

        return view
    }
}