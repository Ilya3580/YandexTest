package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

//это слушатель для websocket
class Listener(
    private var viewModel: MyViewModel<StickWebSocket>, private var context: Context,
    private var ticker: String
) : WebSocketListener() {
    //здесь запускаю webcoket
    override fun onOpen(webSocket: WebSocket, response: Response) {
        //Log.d("TAGA", ticker)
        webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"$ticker\"}")
    }
    //здесь запускаю получаю от него сообщения
    override fun onMessage(webSocket: WebSocket, message: String) {
        //Log.d("TAGA", message.toString())
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
        val price = jsonI.get("p").toString().toFloat()
        val time = jsonI.get("t").toString().toLong() / 1000

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            viewModel.user = StickWebSocket(time.toInt(), price)
            viewModel.getUsersValue()
        }
    }
}