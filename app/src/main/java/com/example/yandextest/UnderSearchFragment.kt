package com.example.yandextest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class UnderSearchFragment() : Fragment() {

    private lateinit var myView : View
    private lateinit var recyclerViewPopular : RecyclerView
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var classRequests: ClassRequests
    private lateinit var textViewPopular : TextView
    private lateinit var textViewHistory : TextView
    private lateinit var sPref : SharedPreferences
    private lateinit var lstHistory : ArrayList<String>
    private lateinit var adapterHistory : RecyclerViewHorizontal

    private val POPULAR_LIST = "POPULAR_LIST"
    private val TICKERS = "TICKERS"

    companion object {

        fun newInstance() = UnderSearchFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_under_search, container, false)
        retainInstance = true
        classRequests = ClassRequests()
        lstHistory = ArrayList()
        textViewPopular = myView.findViewById(R.id.popularTextView)
        textViewHistory = myView.findViewById(R.id.historyTextView)
        recyclerViewHistory = myView.findViewById(R.id.recyclerViewHistory)
        recyclerViewPopular = myView.findViewById(R.id.recyclerViewPopular)
        sPref = requireContext().getSharedPreferences(POPULAR_LIST, Context.MODE_PRIVATE)

        settingHistoryList()
        updatePopularList()

        return myView
    }

    private fun settingHistoryList(){
        recyclerViewHistory.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        if(sPref.contains(TICKERS)){
            lstHistory = ArrayList(sPref.getString(TICKERS, "")!!.split("$"))
        }
        if(lstHistory.count() == 0){
            textViewHistory.visibility = View.GONE
        }else{
            textViewHistory.visibility = View.VISIBLE
            adapterHistory = RecyclerViewHorizontal(lstHistory)
            adapterHistory.flagOnLongClick = true
            recyclerViewHistory.adapter = adapterHistory
        }


    }

    private fun saveList(){
        var str = ""
        for(i in (0 until lstHistory.count())){
            str = if(i == 0){
                lstHistory[i]
            }else{
                str + "$" + lstHistory[i]
            }
        }
        val ed = sPref.edit()
        ed.putString(TICKERS, str)
        ed.apply()
    }

    public fun userUpdateQuestions(question : String){
        if(question != ""){
            val i = lstHistory.indexOf(question)
            if(i >= 0){
                lstHistory.removeAt(i)
            }

            lstHistory.add(0, question)
            saveList()

            adapterHistory.setValues(lstHistory)
        }
    }

    public fun updatePopularList(){
        val lstPreview = classRequests.readList(requireContext())
        recyclerViewPopular.layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.HORIZONTAL)
        if(lstPreview.count() == 0){
            textViewPopular.visibility = View.GONE
        }else{
            textViewPopular.visibility = View.VISIBLE
            recyclerViewPopular.adapter = RecyclerViewHorizontal(lstPreview )
        }
    }



}
