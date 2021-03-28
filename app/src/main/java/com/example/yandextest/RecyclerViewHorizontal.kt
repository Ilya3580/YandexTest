package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//этот reyclerview отвечает за вертикальные списки под поиском
open class RecyclerViewHorizontal (private var values: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerViewHorizontal.MyViewHolder>() {

    private lateinit var view: View//view элемента списка
    private lateinit var context : Context
    //эта переменая устанвливает можно лли удалять элементы он абудет true для списка с историей
    private var _flagOnLongClick = false
    public var flagOnLongClick : Boolean
        get() {return _flagOnLongClick}
        set(value) {_flagOnLongClick = value}

    //это функция вызывается когда нужно обновить или изменить список
    public fun setValues(values: ArrayList<String>){
        this.values = values
        notifyDataSetChanged()
    }

    //для хранения информации списков
    private lateinit var sPref : SharedPreferences
    private val POPULAR_LIST = "POPULAR_LIST"
    private val TICKERS = "TICKERS"

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cell_horizontal_recycler_view, parent, false)

        view = itemView
        context = parent.context

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textViewCell.text = values[position]

        //здесь я обрабатываю нажатие на view проверяю естьь ли интернет если да то запускаю другую активность и передаю туда тикер
        holder.itemView.setOnClickListener {
            if(InternetFunctions.hasConnection(context)){
                val intent = Intent(context, ChartActivity::class.java)
                intent.putExtra("TICKER", holder.textViewCell.text.toString())
                context.startActivity(intent)
            }else{
                InternetFunctions.alertDialog(context)
            }
        }
        //далле я обрабатываю долгое нажатие оно будет работать только для списка с истоией
        if(_flagOnLongClick) {
            holder.itemView.setOnLongClickListener {
                sPref = context.getSharedPreferences(POPULAR_LIST, Context.MODE_PRIVATE)
                if(sPref.contains(TICKERS)){
                    //далле я удаляю его из сохраненого списка
                    val ticker = values[position]
                    val lstHistory = ArrayList(sPref.getString(TICKERS, "")!!.split("$"))
                    lstHistory.remove(ticker)
                    var saveStr = ""
                    for(i in (0 until lstHistory.count())){
                        saveStr = if(i == 0){
                            lstHistory[i]
                        }else{
                            saveStr + "$" + lstHistory[i]
                        }
                    }
                    val ed = sPref.edit()
                    ed.putString(TICKERS, saveStr)
                    ed.apply()

                    //здесь я удаляю его view
                    values.removeAt(position)
                    notifyItemRemoved(position)
                }
                return@setOnLongClickListener true
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewCell : TextView
            get() {return field }
        init {
            textViewCell = itemView.findViewById(R.id.cell_horizontal_recycler_view)
        }
    }

}

