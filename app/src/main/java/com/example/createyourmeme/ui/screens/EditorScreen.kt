package com.example.createyourmeme.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.createyourmeme.data.model.MemeItem
import com.example.createyourmeme.data.repository.MemeEditorRepository
import com.example.createyourmeme.domain.model.TextBoxState
import com.example.createyourmeme.ui.viewmodel.EditorViewModel
import com.example.createyourmeme.utils.PermissionUtils.ensureStoragePermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                                vm.saveMeme(context, meme, imageWidth, imageHeight)
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
                        vm.shareMeme(context, meme, imageWidth, imageHeight)
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