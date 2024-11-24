package com.mkrdeveloper.memeappjetpack
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap

@Composable
fun captureComposableAsBitmap(
    content: @Composable () -> Unit,
    onBitmapReady: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val composeView = ComposeView(context)

    composeView.apply {
        setContent {
            content()
        }
        // Wait for the layout pass and post the capture operation
        post {
            // Capture the bitmap once layout is complete
            val bitmap = composeView.drawToBitmap(Bitmap.Config.ARGB_8888)
            onBitmapReady(bitmap)
        }
    }
}

