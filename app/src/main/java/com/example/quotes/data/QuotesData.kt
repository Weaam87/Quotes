package com.example.quotes.data

import com.squareup.moshi.Json

data class QuotesData(val id: String,
                      @Json(name = "img_src") val imgSrcUrl: String,
                      val quote: String,
                      val name: String)