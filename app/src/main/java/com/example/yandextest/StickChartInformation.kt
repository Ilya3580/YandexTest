package com.example.yandextest

import android.os.DropBoxManager
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import java.security.KeyStore

class StickChartInformation {
    private var _dateCode : Int = 0
    private var _open : Float = 0f
    private var _high : Float = 0f
    private var _low : Float = 0f
    private var _close : Float = 0f


    constructor(){}
    constructor(_dateCode: Int, _open: Float, _high: Float, _low: Float, _close: Float) {
        this._dateCode = _dateCode
        this._open = _open
        this._high = _high
        this._low = _low
        this._close = _close
    }

    public var dateCode : Int
        get() {return _dateCode}
        set(value) {_dateCode = value}

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
        public fun revertListEntry(listDateRequestChartClass: ArrayList<StickChartInformation>)
            : ArrayList<Entry>{
            val lst = ArrayList<Entry>()
            for(i in (0 until listDateRequestChartClass.count())){
                lst.add(Entry(i.toFloat(), (listDateRequestChartClass[i].open + listDateRequestChartClass[i].close)/2))
            }
            return lst
        }

        public fun revertListCandleEntry(listDateRequestChartClass: ArrayList<StickChartInformation>)
            : ArrayList<CandleEntry>{
            val lst = ArrayList<CandleEntry>()
            for(i in (0 until listDateRequestChartClass.count())){
                lst.add(CandleEntry(i.toFloat(), listDateRequestChartClass[i].high,
                    listDateRequestChartClass[i].low, listDateRequestChartClass[i].open,
                    listDateRequestChartClass[i].close))
            }
            return lst
        }

        public fun convertListPeriod(period: String, listDateRequestChartClass: ArrayList<StickChartInformation>)
            : ArrayList<StickChartInformation> {

            when (period) {
                "D" -> return convertDate(listDateRequestChartClass, 86400)
                "W" -> return convertDate(listDateRequestChartClass, 604800)
                "6M" -> return convertDate(listDateRequestChartClass, 15897600)
                "1Y" -> return convertDate(listDateRequestChartClass, 31536000)
            }
            return listDateRequestChartClass
        }


        private fun convertDate(listDateRequestChartClass: ArrayList<StickChartInformation>, unixDifference : Int)
                : ArrayList<StickChartInformation>  {
            val lst = ArrayList<StickChartInformation>()
            val data = listDateRequestChartClass[listDateRequestChartClass.count() - 1].dateCode
            for (i in listDateRequestChartClass) {
                if (data - i.dateCode <= unixDifference) {
                    lst.add(i)
                }
            }
            return lst

        }
    }
}

class StickWebSocket{
    private var _dateCode : Int = 0
    private var _price : Float = 0f

    constructor(){}
    constructor(_dateCode: Int, _price: Float) {
        this._dateCode = _dateCode
        this._price = _price
    }

    public var dateCode : Int
        get() {return _dateCode}
        set(value) {_dateCode = value}

    public var price : Float
        get() {return  _price}
        set(value) {_price = value}
}
