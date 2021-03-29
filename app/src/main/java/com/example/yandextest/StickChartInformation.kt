package com.example.yandextest

import android.os.DropBoxManager
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import java.security.KeyStore

//в этом классехранится вся информация о компании в chartfragment мы создаем из этого типа списко
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
        //эта функция используется для того чтобы преобразовать списко для линейного графика
        public fun revertListEntry(listDateRequestChartClass: ArrayList<StickChartInformation>)
            : ArrayList<Entry>{
            val lst = ArrayList<Entry>()
            for(i in (0 until listDateRequestChartClass.count())){
                lst.add(Entry(i.toFloat(), (listDateRequestChartClass[i].open + listDateRequestChartClass[i].close)/2))
            }
            return lst
        }
        //эта функция используется для того чтобы преобразовать списко для свечного графика
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
        //эти две функции используются для того чтобы оставить нужные точки
        //как я уже ранее объяснял api не предоставляло информацию для недели и дня и приходилось брать эту инфу из месяца
        public fun convertListPeriod(period: String, listDateRequestChartClass: ArrayList<StickChartInformation>)
            : ArrayList<StickChartInformation> {

            //эта разница которая должна быть между первой и последней точкой
            //она берется из преобразования в периода в секунду
            when (period) {
                "D" -> return convertDate(listDateRequestChartClass, 86400)//это день 1 день - 86400 секунд и так для всего остального
                "W" -> return convertDate(listDateRequestChartClass, 604800)
                "6M" -> return convertDate(listDateRequestChartClass, 15897600)
                "1Y" -> return convertDate(listDateRequestChartClass, 31536000)
            }
            //если не один из этих параметров не был выбран значит мы передали либо меся либо 10 лет так как они есть в api то их преобразовывать не нужно
            return listDateRequestChartClass
        }

        //в этой функции я обрезаю переод до нужного
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

//этот класс хранит информацию которую возвращает websocket
class StickWebSocket{
    private var _ticker : String = ""
    private var _dateCode : Int = 0
    private var _price : Float = 0f

    constructor(){}
    constructor(_ticker : String, _dateCode: Int, _price: Float) {
        this._dateCode = _dateCode
        this._price = _price
        this._ticker = _ticker
    }

    public var ticker : String
        get() {return _ticker}
        set(value) {_ticker = value}

    public var dateCode : Int
        get() {return _dateCode}
        set(value) {_dateCode = value}

    public var price : Float
        get() {return  _price}
        set(value) {_price = value}
}
