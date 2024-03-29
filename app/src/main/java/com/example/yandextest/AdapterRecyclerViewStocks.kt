package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.icu.number.NumberFormatter.with
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.squareup.picasso.Picasso
import okhttp3.WebSocket
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

//этот recycler view используется для вкладок stocks и favorite но для favorite мы его переопределяем
open class AdapterRecyclerViewStocks(private val values: ArrayList<CellInformation>,//элементы
                                     private var viewModelListFavorite : MyViewModel<ArrayList<String>>,//viewmodel который срабатывает при изменение списка favorite
                                     private var owner : LifecycleOwner,
                                     private var context: Context,
                                     private var viewModelListWebSocket : MyViewModel<ArrayList<StickWebSocket>>,
                                     private var webSocket: WebSocket) :
    RecyclerView.Adapter<AdapterRecyclerViewStocks.MyViewHolder>() {

    protected lateinit var view : View

    public open fun setWebSocket(webSocket : WebSocket){
        this.webSocket = webSocket

    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)

        view = itemView

        //обрабатываю клик на view и запуска активность
        itemView.setOnClickListener {
            if(InternetFunctions.hasConnection(context)){
                val intent = Intent(context, ChartActivity::class.java)
                intent.putExtra("TICKER", itemView.findViewById<TextView>(R.id.ticker).text.toString())
                context.startActivity(intent)
            }else{
                InternetFunctions.alertDialog(context)
            }
        }

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //ставлю соответствующий фон для itemview
        if(position % 2 == 0){
            holder.containerView.setBackgroundColor(context.resources.getColor(R.color.light_gray))
        }else{
            holder.containerView.setBackgroundColor(context.resources.getColor(R.color.white))
        }

        //здесь я беру инфу со списка и устанавливаю его в textview
        holder.textViewTicker.text = values[position].ticker
        holder.textViewCompany.text = values[position].company
        //эта функция устанавливает инфу с процентным изменение цены
        settingTextViewDifferencePrice(holder.textViewDifferencePrice, holder.textViewPrice, values[position])

        if(values[position].ticker == "YNDX") {//картинку яндекса я использую также в качестве иконки приложения так что пусть она же и будет картинкой для тикера
            holder.imageLogo.setImageDrawable(context.resources.getDrawable(R.drawable.icon))
        }else{
            loadImage(holder, position)
        }

        //здесь я обратываю нажатие на звездочку
        val functionsTickers = FunctionsTickers()
        holder.starButton.setOnClickListener {
            onClick(functionsTickers, position, holder)

        }

        checkStars(holder, position)
    }

    // подгружаю картинки, картинки загрузятся не все потому что некоторых ссылок в api нету
    //это связанно с тем, что большая часть ссылок на картинок отличаются только тикером
    //но не которые ссылки этому не соответствуют, поэтому подгрузятся не все
    //это сделанно для того чтобы ограничение api не влияло на chartActivity
    private fun loadImage(holder: MyViewHolder, position: Int){
        Picasso.get()
            .load(
                EnumListName.PICASSO_URL.value.replace(
                    EnumListName.MY_SYMBOL.value,
                    values[position].ticker
                )
            )
            .into(holder.imageLogo)
    }

    //здесь я добавляю тикер в избранное или удаляю зависит
    open fun onClick(functionsTickers : FunctionsTickers, position: Int, holder: MyViewHolder){
        if (functionsTickers.checkStatusSharedPreference(values[position].ticker, context)) {
            functionsTickers.delayTickersFavorites(values[position].ticker, context)
        } else {
            functionsTickers.saveTickersFavorites(values[position].ticker, context)

        }

        //и отправляю сообщение во viewmodel
        viewModelListFavorite.user = functionsTickers.listFavoriteTickers(context)
        viewModelListFavorite.getUsersValue()
    }

    //здесь я устанавливаю слушатель для viewmodel
    open fun checkStars(holder: MyViewHolder, position: Int){
        viewModelListFavorite.getUsersValue().observe(owner, Observer {
            //определяю есть ли звезда в списке favorite и в зависимости от этого определяю какой она должна быть
            val functionsTickers = FunctionsTickers()
            val lst = functionsTickers.listFavoriteTickers(context)
            for(i in lst){
                if(i == values[position].ticker){
                    holder.starButton.setImageDrawable(context.resources.getDrawable(android.R.drawable.btn_star_big_on))
                    return@Observer
                }
            }
            holder.starButton.setImageDrawable(context.resources.getDrawable(android.R.drawable.btn_star_big_off))
        })
    }

    //здесь я устанавливаю значение в текст с процентным изменением цены, определяю цвет, знак пллюс иди минус и значек валюты
    private fun settingTextViewDifferencePrice(differencePriceTextView : TextView, priceTextView: TextView, cellInformation : CellInformation){
        var number = 0.0
        var percent = 0.0
        var symbol = ""
        if(cellInformation.differencePrice.contains('-')){
            //number когда изменение ноль содержит нули поэтому если неудается преобразовать значит значение ноль
            try {
                number = cellInformation.differencePrice.substring(1).trim().toDouble()
                percent = cellInformation.differencePricePercent.substring(1).trim().toDouble()
            }catch (e : Exception){
                number = 0.0
                percent = 0.0
            }
            differencePriceTextView.setTextColor(context.resources.getColor(R.color.red))
            symbol = "-"
        }else{
            symbol = "+"
            //number когда изменение ноль содержит нули поэтому если неудается преобразовать значит значение ноль
            try {
                number = cellInformation.differencePrice.trim().toDouble()
                percent = cellInformation.differencePricePercent.trim().toDouble()
            }catch (e : Exception){
                number = 0.0
                percent = 0.0
            }

            differencePriceTextView.setTextColor(context.resources.getColor(R.color.green))
        }

        //для определения значка валюты я использую встроенную библиотеку Currency
        var currency : Currency
        var symbolCurrency : String = ""
        try {
            if (cellInformation.currency != "null") {
                currency = Currency.getInstance(cellInformation.currency)
                symbolCurrency = currency.symbol
            }
        }catch (e : Exception){
            Log.d("TAGA", cellInformation.currency)
        }

        createWebSocket(cellInformation, priceTextView, symbolCurrency)

        percent = (percent * 100).toInt().toDouble() / 100
        number = (number * 100).toInt().toDouble() / 100

        priceTextView.text = "${cellInformation.price}$symbolCurrency"
        if(number == 0.0){
            differencePriceTextView.text = ""
            return
        }
        differencePriceTextView.text = "$symbol$number$symbolCurrency ($percent)%"


    }

    private fun createWebSocket(cellInformation: CellInformation, priceTextView: TextView, symbolCurrency : String){
        //здесь я подписываюсь на обновления нужного мне тикера
        viewModelListWebSocket.user?.add(StickWebSocket(cellInformation.ticker, 0,cellInformation.price.toFloat()))
        webSocket.send("{\"type\":\"subscribe\",\"symbol\":\"${cellInformation.ticker}\"}")

        //здесь отслеживаю изменение его цены
        viewModelListWebSocket.getUsersValue().observe(owner, Observer {
            if(it.count() > 0) {
                for(i in it){
                    if(i.ticker == cellInformation.ticker && i.price != null){
                        priceTextView.text = i.price.toString() + symbolCurrency
                        break
                    }
                }
            }
        })
    }

    //в этой функции я отменяю подписку на viewmodel, но перед этим проверяю нет ли такого тикера. Если есть это значит что этот тикер есть в Favorite
    //значит мы просто удаляем его из списка а подписку не трогаем
    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        unsubscribe(holder.textViewTicker.text.toString())
    }

    //здесь я отменяю подписки, но не все если в списке будет два одинаковых тикера это значит что тикер есть в двух вкладках и подписку отменять нельзя
    open fun unsubscribe(ticker: String){
        val lst = viewModelListWebSocket.user
        var flag = true
        var countItems = 0
        var index = -1
        for(i in (0 until (lst?.count() ?: 0))){
            if(ticker == lst!![i].ticker){
                countItems++
                if(flag) {
                    index = i
                    flag = false
                }
            }
        }
        if(countItems == 1){
            webSocket.send("{\"type\":\"unsubscribe\",\"symbol\":\"${ticker}\"}")
        }

        if(!flag){
            viewModelListWebSocket.user?.removeAt(index)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTicker : TextView
            get() {return field }
        var textViewCompany : TextView
            get() {return field }
        var textViewPrice : TextView
            get() {return field }
        var textViewDifferencePrice : TextView
            get() {return field }
        var containerView : LinearLayout
            get() {return field}
        var starButton : ImageButton
            get() {return field}
        var imageLogo : ImageView
        init {
            imageLogo = itemView.findViewById(R.id.logo)
            textViewTicker = itemView.findViewById(R.id.ticker)
            textViewCompany = itemView.findViewById(R.id.company)
            textViewPrice = itemView.findViewById(R.id.price)
            textViewDifferencePrice = itemView.findViewById(R.id.difference_price)
            containerView = itemView.findViewById(R.id.containerCellRecyclerView)
            starButton = itemView.findViewById(R.id.buttonStars)

        }
    }

}


//этот класс отвечает за отступы между элементами
class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {

        outRect.bottom = space
    }

}


