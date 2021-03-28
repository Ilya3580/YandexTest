package com.example.yandextest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//это viewmodel которые есть во всех активностях мне нужно было хранить разные переменные в нем поэтому я использую generic
open class MyViewModel<T>(value: T) : ViewModel() {
    private val _usersValue = MutableLiveData<T>()
    public var user : T?
        get() {return _usersValue.value}
        set(value) {_usersValue.value = value}

    init {
        _usersValue.value = value
    }
    fun getUsersValue(): LiveData<T> {
        return _usersValue
    }
    

}