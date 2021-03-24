package com.example.yandextest

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

class ListenerWebSocket() : WebSocketListener() {

    private lateinit var webSocket: WebSocket
    public fun request(requests: String){
        Log.d("TAGA", requests)
        webSocket.send(requests)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket

        Log.d("TAGA", "Connect")
    }

    override fun onMessage(webSocket: WebSocket, message: String) {
        Log.d("TAGA", "message")
        Log.d("TAGA", message)
        saveViewModel(message)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("TAGA", "Clouse")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d("TAGA", "Fail")

    }

    private fun saveViewModel(text : String){
        if(!text.contains("ping")) {
            val json = JSONObject(text)
            val array = json.get("data") as JSONArray
            val ticker = (array[array.length() - 1] as JSONObject).get("s").toString()
            val price = (array[array.length() - 1] as JSONObject).get("p").toString()
        }
    }
}

class ViewModelStatusWebSocket<T>(value : T) : MyViewModel<T>(value) {
    private var _userDictionary = HashMap<String, Double>()
    public var userDictionary : HashMap<String, Double>
        get() {return _userDictionary}
        set(value) {_userDictionary = value}

}