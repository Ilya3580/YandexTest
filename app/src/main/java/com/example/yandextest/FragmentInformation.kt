package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        var url = if(type == "summary"){
            EnumListName.SUMMARY.value
        }else{
            EnumListName.NEWS.value
        }

        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)

        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        listInformation = if(type == "summary"){
                            parsDataSummary(body)
                        }else{
                            parsDataNews(body)
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


    private fun parsDataNews(text : String) : ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        val jsonArray = json.getJSONArray("item")
        for(i in (0 until jsonArray.length())){
            lst.add((jsonArray[i] as JSONObject).get("description").toString())
        }

        return lst
    }

    private fun parsDataSummary(text : String) : ArrayList<String>{
        val lst = ArrayList<String>()
        val json = JSONObject(text)
        lst.add(json.get("longBusinessSummary").toString())
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