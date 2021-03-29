package com.example.yandextest

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Executable
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//этот клас хранит вспомогательные функции
class ClassRequests {

    private val REQUESTS = "REQUESTS"
    private val TICKERS = "TICKERS"
    private lateinit var sPref : SharedPreferences

    //эта функция разбирает json для поиска
    public fun parsQuestionSearch(text : String, context: Context) : HashMap<String, String>{
        //этой функцией я проверяю не выдало ли api ограничение
        if(text.contains("[")) {
            val hashMap = HashMap<String, String>()
            val json = JSONObject(text)
            val bestMatches = json.getJSONArray("bestMatches")
            for (i in (0 until bestMatches.length())) {
                val l = bestMatches[i] as JSONObject
                hashMap[l.get("1. symbol").toString()] = l.get("2. name").toString()
            }
            return convertList(hashMap)
        }else{
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Ограничение api положалуйста повторите запрос позже", Toast.LENGTH_LONG).show()
            }

            return convertList(HashMap())
        }
    }
    //этой функция проверяет есть ли какято информация в памяти устройства если да то возвращаетт её
    public fun checkTicker(ticker : String, context: Context) : CellInformation?{
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        return if(sPref.contains(ticker)) {
            val tickersSPref = sPref.getString(ticker, "")
            val mas = tickersSPref?.split("$")
            if(mas != null){
                CellInformation(mas[0], mas[1], mas[2], mas[3], mas[4], mas[5])
            }else{
                null
            }
        }else{
            null
        }
    }
    //этой функцией сохраняет тикер в память устройства
    public fun saveTicker(cellInformation: CellInformation, context: Context){
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        val ed = sPref.edit()
        ed.putString(cellInformation.ticker, cellInformation.toString())
        ed.apply()
    }
    //эта функция разбирает json и сохраняет информацию о компании
    public fun parsTickersData(text: String, lst : ArrayList<CellInformation>, context: Context){
        val json = JSONArray(text)
        for(i in (0 until json.length())) {
            val ticker = json.getJSONObject(i).get("symbol")
            val company = json.getJSONObject(i).get("shortName").toString()
            val price = json.getJSONObject(i).get("regularMarketPrice").toString()
            val differencePrice = json.getJSONObject(i).get("regularMarketChange").toString()
            val differencePricePercent = json.getJSONObject(i).get("regularMarketChangePercent").toString()
            val currency = json.getJSONObject(i).get("currency").toString()
            for(j in (0 until lst.count())){
                if(lst[j].ticker == ticker){
                    lst[j].company = company
                    lst[j].price = price
                    lst[j].differencePrice = differencePrice
                    lst[j].differencePricePercent = differencePricePercent
                    lst[j].currency = currency
                    saveTicker(lst[j], context)
                }
            }
        }
    }
    //эта функция разбирает json и возвращает список зар=груженных тикеров
    public fun parsCheckURL(text : String, context: Context) : ArrayList<String>{
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        val lst = ArrayList<String>()
        val json = JSONArray(text).getJSONObject(0).getJSONArray("quotes")
        for(i in (0 until json.length())) {
            lst.add(json.getString(i))
        }
        return convertList(lst)
    }
    //эта функция сохраняет список тикеров
    public fun saveList(context: Context, lst : ArrayList<String>){
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        var lstStr = ""
        for(i in (0 until lst.count())){
            lstStr = if(i == 0){
                lst[i]
            }else{
                lstStr + "$" + lst[i]
            }
        }

        val ed = sPref.edit()
        ed.putString(TICKERS, lstStr)
        ed.apply()
    }
    //эта функция возвращает список объектов котоорые хранят всю информацию о компании
    public fun readListCellInformation(context: Context) : ArrayList<CellInformation>{
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        if(sPref.contains(TICKERS)){
            val lst = ArrayList<CellInformation>()
            val listString = sPref.getString(TICKERS, "")!!.split("$")
            for(i in listString){
                val cellInformation = checkTicker(i, context)
                if(cellInformation != null)
                    lst.add(cellInformation)
            }
            return lst
        }else{
            return ArrayList()
        }
    }
    //эта функция возвращает список сохраненый тикеров в памяти устройства
    public fun readList(context: Context) : ArrayList<String>{
        sPref = context.getSharedPreferences(REQUESTS, Context.MODE_PRIVATE)
        if(sPref.contains(TICKERS)){
            return  ArrayList(sPref.getString(TICKERS, "")!!.split("$"))
        }else{
            return ArrayList()
        }
    }

    //эти две функции удаляют из списка индексы и валютные пары потому что не все api которые я использую потдерживают эти данные
    public fun convertList(lstPreview : ArrayList<String>) : ArrayList<String>{
        val lstSymbol = ArrayList<String>()
        lstSymbol.add(".")
        lstSymbol.add("-")
        lstSymbol.add(" ")
        lstSymbol.add("^")
        lstSymbol.add("=")
        val lstFinal = ArrayList<String>()
        for(i in lstPreview){
            var flag = true
            for( n in lstSymbol){
                if(i.contains(n)){
                    flag = false
                    break
                }
            }
            if(flag){
                lstFinal.add(i)
            }
        }

        return lstFinal
    }
    private fun convertList(lstPreview : HashMap<String, String>) : HashMap<String,String>{
        val lstSymbol = ArrayList<String>()
        lstSymbol.add(".")
        lstSymbol.add("-")
        lstSymbol.add(" ")
        lstSymbol.add("^")
        lstSymbol.add("=")
        val lstFinal = HashMap<String, String>()
        for(i in lstPreview){
            var flag = true
            for( n in lstSymbol){
                if(i.key.contains(n)){
                    flag = false
                    break
                }
            }
            if(flag){
                lstFinal[i.key] = i.value
            }
        }

        return lstFinal
    }

}