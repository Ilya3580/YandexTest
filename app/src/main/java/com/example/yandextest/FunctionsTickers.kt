package com.example.yandextest

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import java.io.IOException

class FunctionsTickers {
    private lateinit var sPref : SharedPreferences

    //эта функция проверяет тике в списке favorite или нет
    public fun checkStatusSharedPreference(ticker: String, context: Context) : Boolean{sPref = context.getSharedPreferences(EnumListName.FAVORITES_TICKERS.value, Context.MODE_PRIVATE)
        if(sPref.contains(EnumListName.FAVORITES_TICKERS.value)){
            val str = sPref.getString(EnumListName.FAVORITES_TICKERS.value, "")
            val lst = ArrayList(str!!.split(" ")).filter { it.isNotEmpty() }
            for(i in lst){
                if(i == ticker){
                    return true
                }
            }
            return false

        }else{
            return false
        }
    }
    //эта функция сохраняет тикер в список favorite
    public fun saveTickersFavorites(ticker : String, context: Context)
    {
        sPref = context.getSharedPreferences(EnumListName.FAVORITES_TICKERS.value, Context.MODE_PRIVATE)
        if(sPref.contains(EnumListName.FAVORITES_TICKERS.value)){
            val str = sPref.getString(EnumListName.FAVORITES_TICKERS.value, "")
            val lst = ArrayList(str!!.split(" ")).filter { it.isNotEmpty() }
            for(i in lst){
                if(i == ticker){
                    return
                }
            }

            val ed = sPref.edit()
            ed.putString(EnumListName.FAVORITES_TICKERS.value, "$str$ticker ")
            ed.apply()

        }else{
            val ed = sPref.edit()
            ed.putString(EnumListName.FAVORITES_TICKERS.value, ticker + " ")
            ed.apply()
        }

    }
    //эта функция удаляет тикер из списка favorite
    public fun delayTickersFavorites(ticker : String, context: Context){
        sPref = context.getSharedPreferences(EnumListName.FAVORITES_TICKERS.value, Context.MODE_PRIVATE)
        if(sPref.contains(EnumListName.FAVORITES_TICKERS.value)){
            val str = sPref.getString(EnumListName.FAVORITES_TICKERS.value, "")
            val lst = ArrayList(str!!.split(" ").filter { it.isNotEmpty() })
            var saveStr = ""
            for(i in (0 until lst.count())){
                if(lst[i] != ticker){
                    saveStr += lst[i] + " "
                }
            }

            val ed = sPref.edit()
            ed.putString(EnumListName.FAVORITES_TICKERS.value, saveStr)
            ed.apply()

        }
    }
    //эта функция возвращает список favorite
    public fun listFavoriteTickers(context: Context) :ArrayList<String> {
        sPref = context.getSharedPreferences(EnumListName.FAVORITES_TICKERS.value, Context.MODE_PRIVATE)
        if(sPref.contains(EnumListName.FAVORITES_TICKERS.value)) {
            val str = sPref.getString(EnumListName.FAVORITES_TICKERS.value, "")
            return ArrayList(str!!.split(" ").filter { it.isNotEmpty() })

        }
        return ArrayList()
    }
    //эта функция меняет два тикера в списке favorite местами это нужно для того чтобы запомнить порядок в котором мы должны вывести тикеры
    //и запомнить их после перестановки
    public fun replacePosition(fromPosition: Int, toPosition: Int, context : Context){
        val lst = listFavoriteTickers(context)
        val ticker = lst[fromPosition]
        lst[fromPosition] = lst[toPosition]
        lst[toPosition] = ticker

        var saveStr = ""
        for(i in (0 until lst.count())){
            saveStr += lst[i] + " "
        }

        val ed = sPref.edit()
        ed.putString(EnumListName.FAVORITES_TICKERS.value, saveStr)
        ed.apply()
    }
    //эта функция удаляет тикер из списка favorite по позиции
    public fun delayTickerFavorite(position : Int, context : Context){
        val lst = listFavoriteTickers(context)
        lst.removeAt(position)

        var saveStr = ""
        for(i in (0 until lst.count())){
            saveStr += lst[i] + " "
        }

        val ed = sPref.edit()
        ed.putString(EnumListName.FAVORITES_TICKERS.value, saveStr)
        ed.apply()
    }
    //эта функция вызывается при долгом нажатии на элемент горизонтального списка
    //и выводит сообщение с уточнением хотим ли мы добавить тикер в избранное
    public fun alertDialog(text : String, ticker: String, mainActivity: MainActivity,
                           context: Context, view : View, viewModel: MyViewModel<String>) : AlertDialog.Builder {
        val alert : AlertDialog
        val builder = AlertDialog.Builder(context)
        val textView = view.findViewById<TextView>(R.id.textView)
        val textViewOk = view.findViewById<CardView>(R.id.textViewOk)
        val textViewNo = view.findViewById<CardView>(R.id.textViewNo)
        textView.text = text
        builder.setView(view)

        alert = builder.create()
        textViewOk.setOnClickListener {
            alert.dismiss()
            val functionsTickers = FunctionsTickers()
            if(!functionsTickers.checkStatusSharedPreference(ticker, context)){
                loadCompanyInformation(ticker, mainActivity, context, viewModel)
            }else{
                Toast.makeText(context, "Ticker already added!", Toast.LENGTH_SHORT).show()
            }
        }
        textViewNo.setOnClickListener {
            alert.dismiss()
        }
        builder.setView(view)
        alert.show()
        return builder
    }

    //эта функция вызывается с диалогового окна исохраняет информацию о отм тикере который мы добавляем в избранное
    private fun loadCompanyInformation(ticker : String, mainActivity: MainActivity, context: Context, viewModel: MyViewModel<String>){
        var url = EnumListName.QUOTE.value
        url = url.replace(EnumListName.MY_SYMBOL.value, ticker)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    val classRequests = ClassRequests()
                    val lst = ArrayList<CellInformation>()
                    lst.add(CellInformation(ticker))
                    classRequests.parsTickersData(body, lst, context)
                    val functionsTickers = FunctionsTickers()
                    functionsTickers.saveTickersFavorites(ticker, context)
                    classRequests.saveTicker(lst[0], context)

                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        viewModel.user = ticker
                        viewModel.getUsersValue()
                        mainActivity.updateFavoriteTicker()
                    }
                }



            }

            override fun onFailure(call: Call, e: IOException) {
                if(!InternetFunctions.hasConnection(context)){
                    InternetFunctions.alertDialog(context)
                }
            }
        })
    }
}
