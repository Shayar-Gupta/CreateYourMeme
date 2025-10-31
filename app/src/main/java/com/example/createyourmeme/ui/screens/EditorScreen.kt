package com.example.createyourmeme.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.createyourmeme.network.MemeItem
import com.example.createyourmeme.ui.viewmodel.EditorViewModel
import com.example.createyourmeme.ui.viewmodel.TextBoxState
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    meme: MemeItem,
    onBack: () -> Unit,
    vm: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val textBoxes by vm.textBoxes.collectAsState()

    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Meme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (ensureStoragePermission(context)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                saveComposedImage(context, meme, textBoxes, imageWidth, imageHeight)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please grant storage permission to save memes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Save")
                    }

                    IconButton(onClick = {
                        shareComposedImage(context, meme, textBoxes, imageWidth, imageHeight)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {
                        vm.clear()
                        Toast.makeText(context, "All text cleared", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = meme.url,
                    contentDescription = meme.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            imageWidth = coords.size.width
                            imageHeight = coords.size.height
                        },
                    contentScale = ContentScale.Fit
                )

                textBoxes.forEach { tb ->
                    DraggableEditableText(
                        tb = tb,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        onUpdate = { vm.updateTextBox(it) },
                        onRemove = { vm.removeTextBox(it) }
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    vm.addTextBox("New Text", imageWidth, imageHeight)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Composable
fun DraggableEditableText(
    tb: TextBoxState,
    imageWidth: Int,
    imageHeight: Int,
    onUpdate: (TextBoxState) -> Unit,
    onRemove: (Int) -> Unit
) {
    var offsetX by remember { mutableStateOf(tb.x) }
    var offsetY by remember { mutableStateOf(tb.y) }
    var scale by remember { mutableStateOf(tb.scale) }
    var text by remember { mutableStateOf(tb.text) }
    var editing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offsetX = (offsetX + pan.x).coerceIn(0f, imageWidth.toFloat() - 50f)
                    offsetY = (offsetY + pan.y).coerceIn(0f, imageHeight.toFloat() - 50f)
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    onUpdate(tb.copy(x = offsetX, y = offsetY, scale = scale, text = text))
                }
            }
    ) {
        if (editing) {
            Column(
                modifier = Modifier
                    .background(Color(0x99000000))
                    .padding(8.dp)
            ) {
                BasicTextField(value = text, onValueChange = { text = it })
                Row {
                    Button(onClick = {
                        editing = false
                        onUpdate(tb.copy(text = text, x = offsetX, y = offsetY, scale = scale))
                    }) { Text("Done") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onRemove(tb.id) }) { Text("Delete") }
                }
            }
        } else {
            Text(
                text = text,
                fontSize = (tb.fontSize.value * scale).sp,
                fontWeight = FontWeight.Bold,
                color = tb.color,
                modifier = Modifier
                    .clickable { editing = true }
                    .scale(scale)
            )
        }
    }
}

suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request).drawable ?: return@withContext null
            if (result is BitmapDrawable) result.bitmap
            else {
                val bmp = Bitmap.createBitmap(
                    result.intrinsicWidth.takeIf { it > 0 } ?: 800,
                    result.intrinsicHeight.takeIf { it > 0 } ?: 800,
                    Bitmap.Config.ARGB_8888
                )
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
                    Toast.makeText(appContext, "❌ Failed to load base image", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val result = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
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
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CreateYourMeme")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                Log.e("SaveMeme", "Failed to insert MediaStore entry — using fallback file save")
                val fallback = File(appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
                FileOutputStream(fallback).use {
                    result.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "✅ Saved to app folder: ${fallback.absolutePath}", Toast.LENGTH_LONG).show()
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
                Toast.makeText(appContext, "❌ Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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

        val result = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
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
suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val filename = "meme_${System.currentTimeMillis()}.jpg"
            val resolver = context.contentResolver
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
                    ?: return@withContext false
            resolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            Log.d("SaveMeme", "✅ Saved meme to: $uri")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

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

fun ensureStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true
    else {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted && context is Activity) {
            ActivityCompat.requestPermissions(context, arrayOf(permission), 100)
        }
        granted
    }
}
