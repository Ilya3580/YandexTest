package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

//это слушатель для websocket
class ListenerChart(
    private var viewModel: MyViewModel<StickWebSocket>, private var context: Context,
    private var ticker: String
) : WebSocketListener() {
    //здесь запускаю webcoket
    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"$ticker\"}")
    }
    //здесь запускаю получаю от него сообщения
    override fun onMessage(webSocket: WebSocket, message: String) {
        parsDateWebSocket(message)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    }

    //здесь если просиходит ошибка то проверяю если нет интернета то возвращаю в начальную активность
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (!InternetFunctions.hasConnection(context)) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }

        }
    }

    //здесь я разбираю сообщение и передаю его в viewmodel
    private fun parsDateWebSocket(message: String) {
        if (message.contains("ping")) {
            return
        }
        val json = JSONObject(message)
        val data = json.getJSONArray("data")
        val jsonI = data[data.length() - 1] as JSONObject
        val symbol = jsonI.get("s").toString().toString()
        val price = jsonI.get("p").toString().toFloat()
        val time = jsonI.get("t").toString().toLong() / 1000

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            viewModel.user = StickWebSocket(symbol, time.toInt(), price)
            viewModel.getUsersValue()
        }
    }
}


//этот слушатель отвечает за websocket который на главном экране
class Listener(
    private var viewModel: MyViewModel<ArrayList<StickWebSocket>>, private var viewModelInternet : MyViewModel<Boolean>, private var context: Context
) : WebSocketListener() {


    //здесь запускаю подписки webcoket
    //также подписки я запускаю из adapter, а здесь они нужны для того чтобы подписки заработали после выключения и включения интернета
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("TAGA", "onOpen")
        var str = ""
        for(i in viewModel.user!!){
            str += " " + i.ticker
        }
        Log.d("TAGA", str)

    }
    //здесь запускаю получаю от него сообщения
    override fun onMessage(webSocket: WebSocket, message: String) {
        Log.d("TAGA", message.toString())
        parsDateWebSocket(message)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    }

    //здесь если просиходит ошибка то проверяю если нет интернета то возвращаю в начальную активность
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if(!InternetFunctions.hasConnection(context)){
            Log.d("TAGA", "FAIL")
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                viewModelInternet.user = false
                viewModelInternet.getUsersValue()
            }
        }

    }

    //здесь я разбираю сообщение и передаю его в viewmodel, который в adapter
    private fun parsDateWebSocket(message: String) {
        if (message.contains("ping") or message.contains("Invalid")) {
            return
        }
        val json = JSONObject(message)
        val data = json.getJSONArray("data")
        val jsonI = data[data.length() - 1] as JSONObject
        val symbol = jsonI.get("s").toString().toString()
        val price = jsonI.get("p").toString().toFloat()
        val time = jsonI.get("t").toString().toLong() / 1000
        val item = StickWebSocket(symbol, time.toInt(), price)

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val lstUser = viewModel.user
            for(i in (0 until (lstUser?.count() ?: 0))){
                if(lstUser!![i].ticker == symbol){
                    lstUser[i] = item
                    viewModel.user = lstUser
                    viewModel.getUsersValue()
                }
            }


        }
    }
}