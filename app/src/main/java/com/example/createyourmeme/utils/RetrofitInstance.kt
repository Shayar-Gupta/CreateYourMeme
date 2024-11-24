package com.example.createyourmeme.utils

import com.example.createyourmeme.data.ApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    const val Base = "https://api.imgflip.com/"

    val api : ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}