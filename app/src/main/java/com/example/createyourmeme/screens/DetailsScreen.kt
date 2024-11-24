package com.example.createyourmeme.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.createyourmeme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(modifier: Modifier = Modifier, name: String?, url: String?) {
    val context = LocalContext.current
    val texts = remember { mutableStateListOf<TextElement>() } // List of text elements
    var newTextFieldVisible by remember { mutableStateOf(false) } // Controls the text input dialog
    var newText by remember { mutableStateOf(TextFieldValue("")) } // For the text input

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 45.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (url != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Render all draggable text elements
                    texts.forEach { textElement ->
                        DraggableText(
                            text = textElement.text,
                            position = textElement.position,
                            onPositionChange = { newPosition ->
                                textElement.position = newPosition
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (name != null) {
                Text(
                    text = name,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 45.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_bold))
                )
            }
        }

        // Floating action button for adding new text
        FloatingActionButton(
            onClick = { newTextFieldVisible = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Text",
                tint = Color.White
            )
        }

        // Floating action button for sharing the meme
        FloatingActionButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    shareMemeAsImage(context, name, url)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Meme",
                tint = Color.White
            )
        }

        // Text input dialog for adding new text
        if (newTextFieldVisible) {
            AlertDialog(
                onDismissRequest = { newTextFieldVisible = false },
                title = { Text(text = "Add Text") },
                text = {
                    TextField(
                        value = newText,
                        onValueChange = { newText = it },
                        label = { Text("Enter your text") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            texts.add(
                                TextElement(
                                    text = newText.text,
                                    position = Offset(50f, 50f) // Default position
                                )
                            )
                            newText = TextFieldValue("")
                            newTextFieldVisible = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { newTextFieldVisible = false }) {
                        Text("Cancel")

                    }
                }
            )
        }
    }
}

@Composable
fun DraggableText(
    text: String,
    position: Offset,
    onPositionChange: (Offset) -> Unit
) {
    var dragOffset by remember { mutableStateOf(position) }

    Text(
        text = text,
        modifier = Modifier
            .offset {
                IntOffset(dragOffset.x.toInt(), dragOffset.y.toInt())
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragOffset = Offset(
                        x = dragOffset.x + dragAmount.x,
                        y = dragOffset.y + dragAmount.y
                    )
                    onPositionChange(dragOffset)
                }
            }
            .background(Color.Yellow, shape = RoundedCornerShape(4.dp))
            .padding(8.dp),
        fontSize = 18.sp,
        fontFamily = FontFamily(Font(R.font.montserrat_bold))
    )
}

data class TextElement(
    var text: String,
    var position: Offset
)

suspend fun shareMemeAsImage(context: Context, name: String?, imageUrl: String?) {
    if (imageUrl == null) return

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val loader = context.imageLoader
    val result = (loader.execute(request) as? SuccessResult)?.drawable ?: return

    val cacheDir = File(context.cacheDir, "shared_images")
    if (!cacheDir.exists()) cacheDir.mkdirs()

    val file = File(cacheDir, "meme_image.png")
    file.outputStream().use { outputStream ->
        result.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    val fileUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_TEXT, "Check out this meme: $name")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share this meme via"))
}
