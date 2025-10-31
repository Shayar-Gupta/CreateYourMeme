package com.example.createyourmeme.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

data class TextBoxState(
    val id: Int,
    val text: String,
    val x: Float,
    val y: Float,
    val fontSize: TextUnit,
    val color: Color,
    val scale: Float = 1f
)