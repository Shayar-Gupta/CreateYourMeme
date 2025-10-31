package com.example.createyourmeme.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class MemeResponse(
    val success: Boolean,
    val data: MemeData
)

data class MemeData(
    val memes: List<MemeItem>
)

@Parcelize
data class MemeItem(
    val id: String,
    val name: String,
    val url: String,
    val width: Int,
    val height: Int,
    val box_count: Int
) : Parcelable
