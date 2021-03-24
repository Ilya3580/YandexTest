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

open class AdapterRecyclerViewStocks(private val values: ArrayList<CellInformation>,
                                     private var viewModelListFavorite : MyViewModel<ArrayList<String>>,
                                     private var owner : LifecycleOwner,
                                     private var context: Context) :
    RecyclerView.Adapter<AdapterRecyclerViewStocks.MyViewHolder>() {

    protected lateinit var view : View

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)

        view = itemView

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
        if(position % 2 == 0){
            holder.containerView.setBackgroundColor(context.resources.getColor(R.color.light_gray))
        }else{
            holder.containerView.setBackgroundColor(context.resources.getColor(R.color.white))
        }

        holder.textViewTicker.text = values[position].ticker
        holder.textViewCompany.text = values[position].company
        settingTextViewDifferencePrice(holder.textViewDifferencePrice, holder.textViewPrice, values[position])
        Picasso.get()
                .load(EnumListName.PICASSO_URL.value.replace(EnumListName.MY_SYMBOL.value, values[position].ticker))
                .into(holder.imageLogo)

        val functionsTickers = FunctionsTickers()
        holder.starButton.setOnClickListener {
            onClick(functionsTickers, position, holder)

        }

        checkStars(holder, position)

    }

    open fun onClick(functionsTickers : FunctionsTickers, position: Int, holder: MyViewHolder){
        if (functionsTickers.checkStatusSharedPreference(values[position].ticker, context)) {
            functionsTickers.delayTickersFavorites(values[position].ticker, context)
        } else {
            functionsTickers.saveTickersFavorites(values[position].ticker, context)

        }

        viewModelListFavorite.user = functionsTickers.listFavoriteTickers(context)
        viewModelListFavorite.getUsersValue()
    }

    open fun checkStars(holder: MyViewHolder, position: Int){
        viewModelListFavorite.getUsersValue().observe(owner, Observer {
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



    private fun settingTextViewDifferencePrice(differencePriceTextView : TextView, priceTextView: TextView, cellInformation : CellInformation){
        var number = 0.0
        var percent = 0.0
        var symbol = ""
        if(cellInformation.differencePrice.contains('-')){
            number = cellInformation.differencePrice.substring(1).toDouble()
            percent = cellInformation.differencePricePercent.substring(1).toDouble()
            differencePriceTextView.setTextColor(context.resources.getColor(R.color.red))
            symbol = "-"
        }else{
            symbol = "+"
            number = cellInformation.differencePrice.trim().toDouble()
            percent = cellInformation.differencePricePercent.toDouble()
            differencePriceTextView.setTextColor(context.resources.getColor(R.color.green))
        }

        var currency : Currency
        var defaultFactoryDigit : Int = 1
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



class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {

        outRect.bottom = space
    }

}


