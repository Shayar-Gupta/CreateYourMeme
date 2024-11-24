package com.example.createyourmeme.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.createyourmeme.R
import com.example.createyourmeme.models.Meme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    memesList: List<Meme>,
    navController: NavHostController
) {
    var isSearchVisible by remember { mutableStateOf(false) }
    val textState = remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meme App", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Search bar appears/disappears with a smooth transition
                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(500)
                    )
                ) {
                    SearchView(
                        state = textState,
                        placeholder = "Search Memes...",
                        modifier = modifier
                    )
                }

                val searchedText = textState.value.text
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(items = memesList.filter {
                        it.name.contains(searchedText, ignoreCase = true)
                    }, key = { it.id }) { item ->
                        MemeItem(
                            itemName = item.name,
                            itemUrl = item.url,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MemeItem(
    itemName: String,
    itemUrl: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("DetailsScreen?name=$itemName&url=$itemUrl")
            },
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier
                .padding(8.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = itemUrl,
                contentDescription = itemName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Maintain square aspect ratio
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = itemName,
                modifier
                    .fillMaxWidth()
                    .basicMarquee(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black),
                fontFamily = FontFamily(Font(R.font.montserrat_regular))

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    state: MutableState<TextFieldValue>,
    placeholder: String,
    modifier: Modifier
) {
    TextField(
        value = state.value,
        onValueChange = { value -> state.value = value },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(30.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(30.dp)),
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(color = Color.Gray, fontSize = 16.sp)
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
    )
}
