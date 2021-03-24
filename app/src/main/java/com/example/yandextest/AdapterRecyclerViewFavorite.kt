package com.example.yandextest

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.*
import kotlin.collections.ArrayList


class AdapterRecyclerViewFavorite(private val values: ArrayList<CellInformation>,
                                  private val viewModelListFavorite : MyViewModel<ArrayList<String>>,
                                  private var owner: LifecycleOwner,
                                  private var context: Context
) : AdapterRecyclerViewStocks(values, viewModelListFavorite, owner, context), ItemTouchHelperAdapter{



    override fun onClick(functionsTickers: FunctionsTickers, position: Int, holder: MyViewHolder) {
        for(i in (0 until values.count())) {
            if(values[i].ticker == holder.textViewTicker.text.toString()) {
                onItemDismiss(i)
                return
            }
        }

    }
    override fun onItemDismiss(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
        val functionsTickers = FunctionsTickers()
        functionsTickers.delayTickerFavorite(position, context)
        viewModelListFavorite.user = functionsTickers.listFavoriteTickers(context)
        viewModelListFavorite.getUsersValue()
    }


    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(values, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(values, i, i - 1)
            }
        }
        val functionsTickers = FunctionsTickers()
        functionsTickers.replacePosition(fromPosition, toPosition, context)
        notifyItemMoved(fromPosition, toPosition)


    }


    override fun checkStars(holder: MyViewHolder, position: Int) {
        viewModelListFavorite.getUsersValue().observe(owner, androidx.lifecycle.Observer {
            val functionsTickers = FunctionsTickers()
            val lst = functionsTickers.listFavoriteTickers(context)
            if(position < values.size && lst.count() <= values.count()) {
                for (i in lst) {
                    if (i == values[position].ticker) {
                        holder.starButton.setImageDrawable(context.resources.getDrawable(android.R.drawable.btn_star_big_on))
                        return@Observer
                    }
                }
                values.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, values.size)

            }
        })
    }

    public fun checkNewFavoriteTickers(){
        viewModelListFavorite.getUsersValue().observe(owner, androidx.lifecycle.Observer {

            val functionsTickers = FunctionsTickers()
            val lst = functionsTickers.listFavoriteTickers(context)
            if (lst.count() > values.count()) {
                val classRequests = ClassRequests()
                val cl = classRequests.checkTicker(lst[lst.count() - 1], context)
                if (cl != null) {
                    values.add(cl)
                    notifyItemChanged(values.count(), cl)
                    notifyItemRangeChanged(0, values.count())
                }

            }
        })
    }

}

class SimpleItemTouchHelperCallback(adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    private val mAdapter: ItemTouchHelperAdapter = adapter
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder,
                        target: ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        mAdapter.onItemDismiss(viewHolder.adapterPosition)
    }

}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
}