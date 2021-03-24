package com.example.yandextest

import android.util.Log
import com.github.mikephil.charting.data.Entry

class DataRequestChartClass(private var _dateCode : String,
                            private var _date : String,
                            private var _price : Float) {

    public var dateCode : String
        get() {return _dateCode}
        set(value) {_dateCode = value}
    public var date : String
        get() {return _date}
        set(value) {_date = value}
    public var price : Float
        get() {return _price}
        set(value) {_price = value}

    companion object {

        public fun convertListEntry(
            period: String,
            listDateRequestChartClass: ArrayList<DataRequestChartClass>
        )
                : ArrayList<Entry> {

            val lst = ArrayList<Entry>()
            if (period == "M" || period == "10Y") {
                for (i in listDateRequestChartClass) {
                    lst.add(Entry(i.dateCode.toFloat(), i.price))
                }
                return lst
            }
            when (period) {
                "D" -> return convertDate(listDateRequestChartClass, 86400)
                "W" -> return convertDate(listDateRequestChartClass, 604800)
                "6M" -> return convertDate(listDateRequestChartClass, 15897600)
                "1Y" -> return convertDate(listDateRequestChartClass, 31536000)
            }
            return ArrayList()
        }


        private fun convertDate(listDateRequestChartClass: ArrayList<DataRequestChartClass>, unixDifference : Int): ArrayList<Entry> {
            val lst = ArrayList<Entry>()
            val data = listDateRequestChartClass[listDateRequestChartClass.count() - 1].dateCode.toInt()
            for (i in listDateRequestChartClass) {
                if (data - i.dateCode.toInt() <= unixDifference) {
                    if(i.price > 0){
                        lst.add(Entry(i.dateCode.toFloat(), i.price))
                    }
                }
            }
            return lst

        }

    }

}