package com.example.yandextest

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.contentValuesOf

class InternetFunctions {

    companion object{
        public fun hasConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiInfo != null && wifiInfo.isConnected) {
                return true
            }
            wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (wifiInfo != null && wifiInfo.isConnected) {
                return true
            }
            wifiInfo = cm.activeNetworkInfo
            return wifiInfo != null && wifiInfo.isConnected
        }

        public fun alertDialog(context: Context) : AlertDialog.Builder {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("No internet connection")
            builder.setPositiveButton("OK") { dialog, which ->

            }
            builder.show()
            return builder
        }
    }
}