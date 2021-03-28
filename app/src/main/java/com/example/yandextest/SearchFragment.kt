package com.example.yandextest

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import okhttp3.*
import java.io.IOException

//этот клас отвечает за поиск, а также что показывается под ним
class SearchFragment : Fragment() {

    private lateinit var myView : View
    private var mainActivity : MainActivity? = null
    //edittext поиска
    private lateinit var searchEditText: EditText
    //иконка поиска
    private lateinit var iconButton : Button
    //этот крестик который появляется чтобы убрать текст
    private lateinit var clearButton : Button

    //далее идут viewModel
    //этот viewmodel твечает за то что будет под поиском контнейнер с фрагментами или списки stock и favorite
    private lateinit var viewModelSearch: MyViewModel<Boolean>
    //этот viewmodel отвечает за то какой будет фрагмент по поиском
    private lateinit var viewModelUnderSearch: MyViewModel<Boolean>
    //этот viewmodel отвечаает за результаты поиска
    private lateinit var viewModelListUnderSearch : MyViewModel<HashMap<String, String>>

    companion object {

        fun newInstance() = SearchFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_search, container, false)
        retainInstance = true
        searchEditText = myView.findViewById(R.id.searchAppBar)
        iconButton = myView.findViewById(R.id.icon_search)
        clearButton = myView.findViewById(R.id.icon_clear)

        //устанавливаю иконки
        iconButton.background = requireContext().resources.getDrawable(R.drawable.ic_baseline_search_24)
        clearButton.background = requireContext().resources.getDrawable(R.drawable.ic_baseline_clear_24)

        try {
            mainActivity = activity as MainActivity
        }catch (e : Exception){
            Log.d("ERROR", e.toString())
        }
        //здесь я инициализирую viewmodel и передаю в mainactivity
        viewModelSearch = MyViewModel<Boolean>(true)
        viewModelUnderSearch = MyViewModel<Boolean>(true)
        viewModelListUnderSearch = MyViewModel<HashMap<String, String>>(HashMap())
        if(mainActivity != null) {
            mainActivity!!.viewModelSearch = viewModelSearch
            mainActivity!!.underViewModel = viewModelUnderSearch
            mainActivity!!.viewModelListUnderSearch = viewModelListUnderSearch

        }

        searchEditText.maxLines = 1

        //здесь я отслеживаю изменение текста и в зависимости от этого вызываю нужные viewmodel и показываю нужные тконки
        searchEditText.doAfterTextChanged {
            var text = searchEditText.text.toString()
            text = text.trim()
            if(text.isEmpty() ){
                clearButton.visibility = View.GONE
                viewModelUnderSearch.user = true
                viewModelUnderSearch.getUsersValue()
            }else{
                clearButton.visibility = View.VISIBLE
                viewModelUnderSearch.user = false
                viewModelUnderSearch.getUsersValue()
                //функция которая выполняет поиск
                loadQuestionsSearch(searchEditText.text.toString())
            }
        }

        //здесь я отслеиваю нажатие на кнопку ok
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                loadQuestionsSearch(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        //здесь я отслеживаю изменение viewmodel и в зависимости от этого показываю нужную иконку
        viewModelSearch.getUsersValue().observe(viewLifecycleOwner, Observer {
            if (it) {
                iconButton.background =
                    requireContext().resources.getDrawable(R.drawable.ic_baseline_search_24)
                //убираю клавиатуру
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(myView.windowToken, 0)
            } else {
                iconButton.background =
                    requireContext().resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
            }
        })


        //вызываю viewmodel ghb rkbrt yf увшееуче
        searchEditText.setOnClickListener {
            viewModelSearch.user = false
        }

        iconButton.setOnClickListener{
            if(mainActivity != null) {
                if (viewModelSearch.user != null) {
                    if(viewModelSearch.user == false)
                        viewModelSearch.user = true
                }

            }
        }

        //эта бработка иконки отчистки текста
        clearButton.setOnClickListener {
            searchEditText.setText("")
        }
        return myView
    }


    //в этой функции я выполняю поиск с помощью встроенного метода api
    private fun loadQuestionsSearch(text : String){
        //изменяю сссылку
        var url = EnumListName.SEARCH.value
        url = url.replace(EnumListName.MY_SYMBOL.value, text)
        val r = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(r).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                if(body != null)
                {
                    //я испоользовал 3 api и у api которое под поиск есть ограничение здесь я проверяю сработало ли ограничение
                    //если да то вывожу toast
                    //если нет то отправляю результат с помощью viewmodel в fragment ос списками
                    val handler = Handler(Looper.getMainLooper())
                    if(body.contains("Note")){
                        handler.post {
                            Toast.makeText(requireContext(), "restriction api", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }else {
                        val classRequests = ClassRequests()
                        val hashMap = classRequests.parsQuestionSearch(body, requireContext())
                        handler.post {
                            viewModelListUnderSearch.user = hashMap
                            viewModelListUnderSearch.getUsersValue()
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                //отлавливаю момент когда пропадает интернет
                if(!InternetFunctions.hasConnection(requireContext())){
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        InternetFunctions.alertDialog(requireContext())
                    }

                }
            }
        })
    }


}



