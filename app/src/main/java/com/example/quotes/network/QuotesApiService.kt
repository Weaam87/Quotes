package com.example.quotes.network

import com.example.quotes.data.QuotesData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

//constant for the base URL for the web service.
private const val BASE_URL =
    "https://jsonkeeper.com"

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private val retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory
    .create(moshi)).baseUrl(BASE_URL).build()

interface QuotesApiService {
    @GET("/b/MZ5D")
    suspend fun getQuotes() : List<QuotesData>
}

object QuotesApi {
    val retrofitService : QuotesApiService by lazy {
        retrofit.create(QuotesApiService::class.java)
    }
}