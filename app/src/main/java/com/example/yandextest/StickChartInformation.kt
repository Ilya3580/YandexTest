package com.example.yandextest

import android.os.DropBoxManager
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import java.security.KeyStore

class StickChartInformation( private var _date : Int, private var _open : Float,
                             private var _high : Float, private var _low : Float,
                             private var _close : Float) {
    public var date : Int
        get() {return _date}
        set(value) {_date = value}

    public var open : Float
        get() {return  _open}
        set(value) {_open = value}

    public var high : Float
        get() {return  _high}
        set(value) {_high = value}

    public var low : Float
        get(){return _low}
        set(value) {_low = value}

    public var close : Float
        get() {return _close}
        set(value) {_close = value}

    companion object{
        public fun revertListEntry(listDateRequestChartClass: ArrayList<StickChartInformation>) : ArrayList<Entry>{
            val lst = ArrayList<Entry>()
            for(i in listDateRequestChartClass){
                lst.add(Entry(i.date.toFloat(), (i.open + i.close)/2))
            }
            return lst
        }

        public fun revertListCandleEntry(listDateRequestChartClass: ArrayList<StickChartInformation>) : ArrayList<CandleEntry>{
            val lst = ArrayList<CandleEntry>()
            for(i in listDateRequestChartClass){
                lst.add(CandleEntry(i.date.toFloat(), i.high, i.low, i.open, i.close))
            }
            return lst
        }

        public fun convertListPeriod(
            period: String,
            listDateRequestChartClass: ArrayList<StickChartInformation>
        )
                : ArrayList<StickChartInformation> {

            when (period) {
                "D" -> return convertDate(listDateRequestChartClass, 86400)
                "W" -> return convertDate(listDateRequestChartClass, 604800)
                "6M" -> return convertDate(listDateRequestChartClass, 15897600)
                "1Y" -> return convertDate(listDateRequestChartClass, 31536000)
            }
            return ArrayList()
        }


        private fun convertDate(listDateRequestChartClass: ArrayList<StickChartInformation>, unixDifference : Int)
                : ArrayList<StickChartInformation>  {
            val lst = ArrayList<StickChartInformation>()
            val data = listDateRequestChartClass[listDateRequestChartClass.count() - 1].date
            for (i in listDateRequestChartClass) {
                if (data - i.date <= unixDifference) {
                    lst.add(i)
                }
            }
            return lst

        }
    }
}