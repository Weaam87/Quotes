package com.example.quotes.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

//constant for the base URL for the web service.
private const val BASE_URL =
    "https://api.jsonbin.io"

private val retrofit = Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
    BASE_URL).build()

interface QuotesApiService {
    @GET("/v3/b/62e7a65260c3536f3fcc8d7a")
    suspend fun getQuotes() : String
}

object QuotesApi {
    val retrofitService : QuotesApiService by lazy {
        retrofit.create(QuotesApiService::class.java)
    }
}