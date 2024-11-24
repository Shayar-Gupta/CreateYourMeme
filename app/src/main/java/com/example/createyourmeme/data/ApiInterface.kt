package com.example.createyourmeme.data

import com.example.createyourmeme.models.AllMemesData
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET("get_memes")
    suspend fun getMemesList() : Response<AllMemesData>
}