package com.example.createyourmeme.data.repository

import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.data.network.ImgflipApi

class MemeRepository(private val api: ImgflipApi = ImgflipApi.Companion.instance) {
    suspend fun fetchMemes(): List<MemeItem> {
        val resp = api.getMemes()
        return if (resp.success) resp.data.memes else emptyList()
    }
}