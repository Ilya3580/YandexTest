package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.icu.number.NumberFormatter.with
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

//этот recycler view используется для вкладок stocks и favorite но для favorite мы его переопределяем
open class AdapterRecyclerViewStocks(private val values: ArrayList<CellInformation>,//элементы
                                     private var viewModelListFavorite : MyViewModel<ArrayList<String>>,//viewmodel который срабатывает при изменение списка favorite
                                     private var owner : LifecycleOwner,
                                     private var context: Context) :
    RecyclerView.Adapter<AdapterRecyclerViewStocks.MyViewHolder>() {

    protected lateinit var view : View

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

        //здесь я подгружаю картинки. Вы наверное обратили внимание, что картинки загружены не все это связанно с ограничение api
        //finnhub предоставляет инфу с картинками, но всего 60 запросов в минуту, но я заметил, что почти все ссылки на картинки одинаковы меняется только тикер
        //поэтому я не стал отправлять запрос а сразу создаю ссылку и в конце просто заменя тикер
        //но это работает не со всеми тикерами
        //я надеюсь что то что я сделал с картинками даст вам понять что с платным api я сделал бы все нормально
        if(values[position].ticker == "YNDX") {//картинку яндекса я использую также в качестве иконки приложения так что пусть она же и будет картинкой для тикера
            holder.imageLogo.setImageDrawable(context.resources.getDrawable(R.drawable.icon))
        }else{
            Picasso.get()
                .load(
                    EnumListName.PICASSO_URL.value.replace(
                        EnumListName.MY_SYMBOL.value,
                        values[position].ticker
                    )
                )
                .into(holder.imageLogo)
        }

        //здесь я обратываю нажатие на звездочку
        val functionsTickers = FunctionsTickers()
        holder.starButton.setOnClickListener {
            onClick(functionsTickers, position, holder)

        }

        checkStars(holder, position)

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


    //здесь я устанавливаю значение в текст с процентным изменением цены, определяю цвет, знак пллюс иди минус и значек валюты
    private fun settingTextViewDifferencePrice(differencePriceTextView : TextView, priceTextView: TextView, cellInformation : CellInformation){
        var number = 0.0
        var percent = 0.0
        var symbol = ""
        if(cellInformation.differencePrice.contains('-')){
            //number когда изменение ноль содержит нули поэтому если неудается преобразовать значит значение ноль
            try {
                number = cellInformation.differencePrice.substring(1).toDouble()
            }catch (e : Exception){
                number = 0.0
            }
            percent = cellInformation.differencePricePercent.substring(1).toDouble()
            differencePriceTextView.setTextColor(context.resources.getColor(R.color.red))
            symbol = "-"
        }else{
            symbol = "+"
            //number когда изменение ноль содержит нули поэтому если неудается преобразовать значит значение ноль
            try {
                number = cellInformation.differencePrice.toDouble()
            }catch (e : Exception){
                number = 0.0
            }

            percent = cellInformation.differencePricePercent.toDouble()
            differencePriceTextView.setTextColor(context.resources.getColor(R.color.green))
        }

        //для определения значка валюты я использую встроенную библиотеку Currency
        var currency : Currency
        var defaultFactoryDigit : Int = 1//это число содержит сколько цифр обычно используют для округления в этой стране
        var symbolCurrency : String = ""
        try {
            if (cellInformation.currency != "null") {
                currency = Currency.getInstance(cellInformation.currency)
                defaultFactoryDigit = currency.defaultFractionDigits
                symbolCurrency = currency.symbol
            }
        }catch (e : Exception){
            Log.d("TAGA", cellInformation.currency)
        }

        defaultFactoryDigit = 10.0.pow(defaultFactoryDigit).toInt()

        percent = (percent * defaultFactoryDigit).toInt().toDouble() / defaultFactoryDigit
        number = (number * 100).toInt().toDouble() / 100

        priceTextView.text = "${cellInformation.price}$symbolCurrency"
        if(number == 0.0){
            differencePriceTextView.text = ""
            return
        }
        differencePriceTextView.text = "$symbol$number$symbolCurrency ($percent)%"
    }

}


//этот класс отвечает за отступы между элементами
class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {

        outRect.bottom = space
    }

}


