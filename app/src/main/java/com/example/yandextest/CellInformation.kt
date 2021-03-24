package com.example.yandextest

import java.util.*

class CellInformation {
    private var _ticker : String = ""
    private var _company : String = ""
    private var _price : String = ""
    private var _differencePrice : String = ""
    private var _currency : String = ""
    private var _differencePricePercent : String = ""

    constructor(ticker : String, company : String, price : String, differencePrice : String, differencePricePercent : String, currency: String) {
        _currency = currency
        _ticker = ticker
        _company = company
        _price = price
        _differencePrice = differencePrice
        _differencePricePercent = differencePricePercent
    }

    constructor(ticker : String){
        _ticker = ticker
    }

    var ticker : String
        get() {return _ticker}
        set(value) {_ticker = value}

    var company : String
        get() {return _company}
        set(value) {_company = value}

    var price : String
        get() {return _price}
        set(value) {_price = value}

    var differencePrice : String
        get() {return _differencePrice}
        set(value) {_differencePrice = value}
    var differencePricePercent : String
        get() {return _differencePricePercent}
        set(value) {_differencePricePercent = value}

    var currency : String
        get(){return  _currency}
        set(value) {_currency = value}

    override fun toString(): String {
        return "$_ticker\$$_company\$$_price\$$_differencePrice\$$_differencePricePercent\$$_currency"
    }

}