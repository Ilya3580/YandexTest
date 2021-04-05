package com.example.yandextest
// здесь я храню спико запрсов, имен, и т.д.
enum class EnumListName(val value: String) {
    KEY_MBOUM("Z9St2YmOOVfKwXNp8uncdXmBLqUqEynMfeKLJwIB4CJZsK1LU6Xnu5pDhZYy"),
    KEY_FINNHUB("c1gebin48v6p69n8u4t0"),
    KEY_ALPHAVANTAGE("IONIFB3VXWUN4RMW"),
    MY_SYMBOL("<MY_SYMBOL>"),
    MY_SYMBOL2("<MY_SYMBOL2>"),
    STOCKS_TICKERS("https://mboum.com/api/v1/tr/trending?apikey=" + KEY_MBOUM.value),
    WEB_SOCKET("wss://ws.finnhub.io?token=" + KEY_FINNHUB.value),
    QUOTE("https://mboum.com/api/v1/qu/quote/?symbol=<MY_SYMBOL>&apikey=" + KEY_MBOUM.value),
    SEARCH("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=<MY_SYMBOL>&apikey=" + KEY_ALPHAVANTAGE.value),
    FAVORITES_TICKERS("FAVORITES"),
    STOCKS("STOCKS"),
    FAVORITE("FAVORITE"),
    PICASSO_URL("https://finnhub.io/api/logo?symbol=<MY_SYMBOL>"),
    DATA_CHART("https://mboum.com/api/v1/hi/history/?symbol=<MY_SYMBOL>&interval=<MY_SYMBOL2>&diffandsplits=true&apikey=" + KEY_MBOUM.value),
    SUMMARY("https://mboum.com/api/v1/qu/quote/profile/?symbol=<MY_SYMBOL>&apikey=" + KEY_MBOUM.value),
    RECOMMENDATION("https://mboum.com/api/v1/qu/quote/recommendation-trend/?symbol=<MY_SYMBOL>&apikey=" + KEY_MBOUM.value),
    NEWS_SENTIMENTS("https://finnhub.io/api/v1/news-sentiment?symbol=<MY_SYMBOL>&token=" + KEY_FINNHUB.value),
    NEWS("https://mboum.com/api/v1/ne/news/?symbol=<MY_SYMBOL>&apikey=" + KEY_MBOUM.value)


}