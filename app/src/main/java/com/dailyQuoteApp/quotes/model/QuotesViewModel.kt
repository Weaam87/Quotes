package com.dailyQuoteApp.quotes.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyQuoteApp.quotes.data.QuotesData
import com.dailyQuoteApp.quotes.network.QuotesApi
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

    fun getIndex(index : Int ) {
        getQuotesDetails(index)
    }

     private fun getQuotesDetails(index : Int) {
        viewModelScope.launch {
            _status.value = QuotesApiStatus.LOADING
            try {
                _photo.value = QuotesApi.retrofitService.getQuotes()[index]
                _quote.value = QuotesApi.retrofitService.getQuotes()[index]
                _name.value = QuotesApi.retrofitService.getQuotes()[index]
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