package com.example.createyourmeme.domain.usecase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.data.repository.MemeEditorRepository
import com.example.createyourmeme.domain.model.TextBoxState

class SaveMemeUseCase( private val repo: MemeEditorRepository = MemeEditorRepository()
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun execute(context: Context, meme: MemeItem, texts: List<TextBoxState>, w: Int, h: Int) {
        repo.saveComposedImage(context, meme, texts, w, h)
    }
}