package com.example.createyourmeme.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ImgflipApi {
    @GET("get_memes")
    suspend fun getMemes(): MemeResponse

    companion object {
        val instance: ImgflipApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.imgflip.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImgflipApi::class.java)
        }
    }
}
