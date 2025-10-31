package com.example.createyourmeme.ui.screens

import Montserrat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.createyourmeme.network.MemeItem
import com.example.createyourmeme.ui.viewmodel.MemeListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MemeListScreen(onMemeSelected: (MemeItem) -> Unit, viewModel: MemeListViewModel = viewModel()) {
    val memes by viewModel.memes.collectAsState()
    val loading by viewModel.loading.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(columns = GridCells.Adaptive(140.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
            items(memes) { meme ->
                Card(modifier = Modifier
                    .padding(8.dp)
                    .clickable { onMemeSelected(meme) }) {
                    Column {
                        AsyncImage(model = meme.url, contentDescription = meme.name, modifier = Modifier
                            .height(160.dp)
                            .fillMaxWidth(), contentScale = ContentScale.Crop)
                        Text(meme.name, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Montserrat), modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
