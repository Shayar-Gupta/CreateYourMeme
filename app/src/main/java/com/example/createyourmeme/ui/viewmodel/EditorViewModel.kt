package com.example.createyourmeme.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TextBoxState(
    val id: Int,
    val text: String,
    val x: Float,
    val y: Float,
    val fontSize: TextUnit,
    val color: Color,
    val scale: Float = 1f
)

class EditorViewModel : ViewModel() {
    private val _textBoxes = MutableStateFlow<List<TextBoxState>>(emptyList())
    val textBoxes: StateFlow<List<TextBoxState>> = _textBoxes.asStateFlow()

    fun addTextBox(
        text: String,
        imageWidth: Int = 0,
        imageHeight: Int = 0
    ) {
        val id = (textBoxes.value.maxOfOrNull { it.id } ?: 0) + 1

        val centerX = if (imageWidth > 0) imageWidth / 2f - 100f else 100f
        val centerY = if (imageHeight > 0) imageHeight / 2f else 100f

        val newTextBox = TextBoxState(
            id = id,
            text = text,
            x = centerX,
            y = centerY,
            scale = 1f,
            fontSize = 32.sp,
            color = Color.White
        )
        _textBoxes.value = _textBoxes.value + newTextBox
    }

    fun updateTextBox(updated: TextBoxState) {
        _textBoxes.value = _textBoxes.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeTextBox(id: Int) {
        _textBoxes.value = _textBoxes.value.filterNot { it.id == id }
    }

    fun clear() {
        _textBoxes.value = emptyList()
    }
}
