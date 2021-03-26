package com.example.yandextest

enum class EnumListName(val value: String) {
    MY_SYMBOL("<MY_SYMBOL>"),
    MY_SYMBOL2("<MY_SYMBOL2>"),
    STOCKS_TICKERS("https://mboum.com/api/v1/tr/trending?apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1"),
    WEB_SOCKET("wss://ws.finnhub.io?token=c15isdv48v6tvr5klgag"),
    QUOTE("https://mboum.com/api/v1/qu/quote/?symbol=<MY_SYMBOL>&apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1"),
    SEARCH("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=<MY_SYMBOL>&apikey=Q11YRI420QDDGFHE"),
    FAVORITES_TICKERS("FAVORITES"),
    STOCKS("STOCKS"),
    FAVORITE("FAVORITE"),
    PICASSO_URL("https://finnhub.io/api/logo?symbol=<MY_SYMBOL>"),
    DATA_CHART("https://mboum.com/api/v1/hi/history/?symbol=<MY_SYMBOL>&interval=<MY_SYMBOL2>&diffandsplits=true&apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1"),
    SUMMARY("https://mboum.com/api/v1/qu/quote/profile/?symbol=<MY_SYMBOL>&apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1"),
    RECOMMENDATION("https://mboum.com/api/v1/qu/quote/recommendation-trend/?symbol=<MY_SYMBOL>&apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1"),
    NEWS_SENTIMENTS("https://finnhub.io/api/v1/news-sentiment?symbol=<MY_SYMBOL>&token=c15isdv48v6tvr5klgag"),
    NEWS("https://mboum.com/api/v1/ne/news/?symbol=<MY_SYMBOL>&apikey=b0tGXI8PUxeykiLsk2DmlNi9UMebVwBy9e8jWgxvAZdXCJ3cDG3Rccj6jzr1")


}