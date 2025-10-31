package com.example.createyourmeme.domain.usecase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.data.repository.MemeEditorRepository
import com.example.createyourmeme.domain.model.TextBoxState

class ShareMemeUseCase (private val repo: MemeEditorRepository = MemeEditorRepository()
) {
    fun execute(context: Context, meme: MemeItem, texts: List<TextBoxState>, w: Int, h: Int) {
        repo.shareComposedImage(context, meme, texts, w, h)
    }
}