package com.example.quotes.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotes.data.QuotesData
import com.example.quotes.network.QuotesApi
import kotlinx.coroutines.launch

//Add an enum to represent all the available statuses
enum class QuotesApiStatus { LOADING, ERROR, DONE }

class QuotesViewModel : ViewModel() {

    private val _status = MutableLiveData<QuotesApiStatus>()
    val status: LiveData<QuotesApiStatus> = _status

    private val _photo = MutableLiveData<QuotesData?>()
    val photo: LiveData<QuotesData?> = _photo

    private val _quote = MutableLiveData<QuotesData?>()
    val quote: LiveData<QuotesData?> = _quote

    private val _name = MutableLiveData<QuotesData?>()
    val name: LiveData<QuotesData?> = _name

    init {
        getQuotesDetails()
    }

    private fun getQuotesDetails() {
        viewModelScope.launch {
            _status.value = QuotesApiStatus.LOADING
            try {
                _photo.value = QuotesApi.retrofitService.getQuotes()[0]
                _quote.value = QuotesApi.retrofitService.getQuotes()[0]
                _name.value = QuotesApi.retrofitService.getQuotes()[0]
                _status.value = QuotesApiStatus.DONE
            } catch (e: Exception) {
                _status.value = QuotesApiStatus.ERROR
                _photo.value = null
                _quote.value = null
                _name.value = null
            }
        }
    }
}