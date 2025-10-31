package com.example.createyourmeme.data

import com.example.createyourmeme.network.ImgflipApi
import com.example.createyourmeme.network.MemeItem


class MemeRepository(private val api: ImgflipApi = ImgflipApi.instance) {
    suspend fun fetchMemes(): List<MemeItem> {
        val resp = api.getMemes()
        return if (resp.success) resp.data.memes else emptyList()
    }
}
