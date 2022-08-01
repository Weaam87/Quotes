package com.example.quotes.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotes.network.QuotesApi
import kotlinx.coroutines.launch

class QuotesViewModel : ViewModel() {

    val status = MutableLiveData<String>()

    init {
        getQuotesDetails()
    }

    private fun getQuotesDetails() {
        viewModelScope.launch {
            try {
                val listResult = QuotesApi.retrofitService.getQuotes()
                status.value = listResult
            } catch (e: Exception) {
                status.value = "Failure : ${e.message}"
            }
        }
    }
}