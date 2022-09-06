package com.example.quotes.network

import com.example.quotes.data.QuotesData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

//constant for the base URL for the web service.
private const val BASE_URL =
    "https://api.npoint.io"

//Create a Moshi object
private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

//Create a Retrofit object
private val retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory
    .create(moshi)).baseUrl(BASE_URL).build()

interface QuotesApiService {
    @GET("/a4ec39c241f277b2f29e")
    suspend fun getQuotes() : List<QuotesData>
}

object QuotesApi {
    val retrofitService : QuotesApiService by lazy {
        retrofit.create(QuotesApiService::class.java)
    }
}