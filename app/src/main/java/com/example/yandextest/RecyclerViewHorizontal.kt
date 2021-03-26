package com.example.yandextest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

open class RecyclerViewHorizontal (private var values: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerViewHorizontal.MyViewHolder>() {

    private lateinit var view: View
    private lateinit var context : Context
    private var _flagOnLongClick = false
    public var flagOnLongClick : Boolean
        get() {return _flagOnLongClick}
        set(value) {_flagOnLongClick = value}

    public fun setValues(values: ArrayList<String>){
        this.values = values
        notifyDataSetChanged()
    }

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
        holder.itemView.setOnClickListener {
            if(InternetFunctions.hasConnection(context)){
                val intent = Intent(context, ChartActivity::class.java)
                intent.putExtra("TICKER", holder.textViewCell.text.toString())
                context.startActivity(intent)
            }else{
                InternetFunctions.alertDialog(context)
            }
        }
        if(_flagOnLongClick) {
            holder.itemView.setOnLongClickListener {
                if(sPref.contains(TICKERS)){
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

