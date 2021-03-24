package com.example.yandextest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHorizontal (private var values: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerViewHorizontal.MyViewHolder>() {

    private lateinit var view: View
    private lateinit var context : Context

    public fun setValues(values: ArrayList<String>){
        this.values = values
        notifyDataSetChanged()
    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cell_horizontal_recycler_view, parent, false)

        view = itemView
        context = parent.context

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textViewCell.text = values[position]
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewCell : TextView
            get() {return field }

        init {
            textViewCell = itemView.findViewById(R.id.cell_horizontal_recycler_view)
        }
    }



}

