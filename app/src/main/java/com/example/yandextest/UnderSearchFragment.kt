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

//этот фрагмент овечает за два вертикальных спика которе появляются под поиском
class UnderSearchFragment() : Fragment() {

    private lateinit var myView : View

    //это два вертикальных списка
    private lateinit var recyclerViewPopular : RecyclerView
    private lateinit var recyclerViewHistory: RecyclerView

    //это экземпляр класса в котором находятся вспомогательные функции
    private lateinit var classRequests: ClassRequests

    //это textviews которые находятся над двумя списками
    private lateinit var textViewPopular : TextView
    private lateinit var textViewHistory : TextView

    //список и адаптер который отображается в истории
    private lateinit var lstHistory : ArrayList<String>
    private lateinit var adapterHistory : RecyclerViewHorizontal

    //это для кеширования информации
    private lateinit var sPref : SharedPreferences
    private val POPULAR_LIST = "POPULAR_LIST"
    private val TICKERS = "TICKERS"

    companion object {

        fun newInstance() = UnderSearchFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_under_search, container, false)
        retainInstance = true
        //инициализируем списки и другие объекты
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

    //здесь мы настраиваем список истории
    private fun settingHistoryList(){
        //здесь мы используем StaggeredGridLayoutManager чтобы он был горизонтальным с двумя строчками
        recyclerViewHistory.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        if(sPref.contains(TICKERS)){
            //загружаем список истории
            lstHistory = ArrayList(sPref.getString(TICKERS, "")!!.split("$"))
        }
        //если спиок пустой то убираем textview
        if(lstHistory.count() == 0){
            textViewHistory.visibility = View.GONE
        }else{//если спиок не пустой то добавляем список в адаптер и включаем textview над списком
            textViewHistory.visibility = View.VISIBLE
            adapterHistory = RecyclerViewHorizontal(lstHistory)
            //это свойство с помощиб которого можно удалять элементы долгим нажатием оно будет включенно только в списке истории
            adapterHistory.flagOnLongClick = true
            recyclerViewHistory.adapter = adapterHistory
        }


    }

    //в этой функции мы кешируем список истории запросов
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

    //эта функция добавляет или добавляет элемент в списоке истории
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
        //эта функция вызывается во viewmodel она обнавляет весь список сразу а не по одному элементу потому что если список попоулярных запросов обновляется то обновляется весь
        //а не по одному элементу как со списком истории
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
