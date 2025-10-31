package com.example.createyourmeme.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.domain.model.TextBoxState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.forEach

class MemeEditorRepository {

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "shared_images")
            cacheDir.mkdirs()
            val file = File(cacheDir, "share_meme_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareComposedImage(
        context: Context,
        meme: MemeItem,
        texts: List<TextBoxState>,
        displayedWidth: Int,
        displayedHeight: Int
    ) {
        val density = context.resources.displayMetrics.density
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val bmp = loadBitmapFromUrl(context, meme.url)
            if (bmp == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Failed to load image", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val result = createBitmap(bmp.width, bmp.height)
            val canvas = Canvas(result)
            canvas.drawBitmap(bmp, 0f, 0f, null)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }

            val scale = minOf(
                displayedWidth * density / bmp.width.toFloat(),
                displayedHeight * density / bmp.height.toFloat()
            )
            val xOffset = ((displayedWidth * density) - bmp.width * scale) / 2f
            val yOffset = ((displayedHeight * density) - bmp.height * scale) / 2f

            texts.forEach { t ->
                paint.color = t.color.toArgb()
                paint.textSize = t.fontSize.value * density * t.scale
                val actualX = ((t.x * density) - xOffset) / scale
                val actualY = ((t.y * density) - yOffset) / scale
                canvas.drawText(t.text, actualX, actualY, paint)
            }

            val uri = saveBitmapToCache(context, result)
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share meme"))
                } else {
                    Toast.makeText(context, "❌ Share failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveComposedImage(
        context: Context,
        meme: MemeItem,
        texts: List<TextBoxState>,
        displayedWidth: Int,
        displayedHeight: Int
    ) {
        val appContext = context.applicationContext
        val density = context.resources.displayMetrics.density

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SaveMeme", "Loading meme bitmap from: ${meme.url}")
                val baseBitmap = loadBitmapFromUrl(appContext, meme.url)
                if (baseBitmap == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            appContext,
                            "❌ Failed to load base image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val result = createBitmap(baseBitmap.width, baseBitmap.height)
                val canvas = Canvas(result)
                canvas.drawBitmap(baseBitmap, 0f, 0f, null)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textAlign = Paint.Align.LEFT
                    isFakeBoldText = true
                }

                val scale = minOf(
                    displayedWidth * density / baseBitmap.width.toFloat(),
                    displayedHeight * density / baseBitmap.height.toFloat()
                )
                val xOffset = ((displayedWidth * density) - baseBitmap.width * scale) / 2f
                val yOffset = ((displayedHeight * density) - baseBitmap.height * scale) / 2f

                texts.forEach { t ->
                    paint.color = t.color.toArgb()
                    paint.textSize = t.fontSize.value * density * t.scale
                    val actualX = ((t.x * density) - xOffset) / scale
                    val actualY = ((t.y * density) - yOffset) / scale
                    canvas.drawText(t.text, actualX, actualY, paint)
                }

                val resolver = appContext.contentResolver
                val filename = "meme_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/CreateYourMeme"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri == null) {
                    Log.e(
                        "SaveMeme",
                        "Failed to insert MediaStore entry — using fallback file save"
                    )
                    val fallback = File(
                        appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        filename
                    )
                    FileOutputStream(fallback).use {
                        result.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            appContext,
                            "✅ Saved to app folder: ${fallback.absolutePath}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                resolver.openOutputStream(uri)?.use {
                    result.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Log.d("SaveMeme", "✅ Meme saved to: $uri")

                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "✅ Meme saved to gallery", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SaveMeme", "❌ Error saving meme: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "❌ Save failed: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result =
                    context.imageLoader.execute(request).drawable ?: return@withContext null
                if (result is BitmapDrawable) result.bitmap
                else {
                    val bmp = createBitmap(result.intrinsicWidth.takeIf { it > 0 } ?: 800,
                        result.intrinsicHeight.takeIf { it > 0 } ?: 800)
                    val canvas = Canvas(bmp)
                    result.setBounds(0, 0, canvas.width, canvas.height)
                    result.draw(canvas)
                    bmp
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}