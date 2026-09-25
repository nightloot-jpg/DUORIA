package com.example.duoria.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.duoria.R
import com.example.duoria.model.FeedPost
import com.example.duoria.model.StoryItem
import com.example.duoria.ui.components.PageHeader
import com.example.duoria.ui.theme.*
import com.example.duoria.viewmodel.DuoriaViewModel
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.feedPosts.collectAsState()
    val viewMode by viewModel.feedViewMode.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    var newPostPlace by remember { mutableStateOf("") }

    val stories = listOf(
        StoryItem("Tú", R.drawable.feed3, "hace 1 h"),
        StoryItem("Yuki", R.drawable.feed2, "hace 4 h")
    )

    if (showCreatePostDialog) {
        Dialog(onDismissRequest = { showCreatePostDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Nueva Publicación",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = { newPostText = it },
                        label = { Text("¿Qué estás sintiendo hoy?") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_post_text")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPostPlace,
                        onValueChange = { newPostPlace = it },
                        label = { Text("Ubicación (ej: Madrid, Parque de El Retiro)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_post_place")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showCreatePostDialog = false }
                        ) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPostText.isNotBlank()) {
                                    viewModel.addPost(newPostText, newPostPlace)
                                    newPostText = ""
                                    newPostPlace = ""
                                    showCreatePostDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("btn_publish_post")
                        ) {
                            Text("Publicar", color = BgDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                title = "Nuestro Feed",
                subtitle = "Solo para dos",
                action = {
                    IconButton(
                        onClick = { showCreatePostDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RoseGradient)
                            .testTag("btn_add_post")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear Publicación",
                            tint = Color.White
                        )
                    }
                }
            )
        }

        // Stories Bar & Mode Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stories
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stories.forEach { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.openStory(story) }
                                .testTag("story_avatar_${story.name}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(RoseGradient)
                                    .padding(2.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = story.imageRes),
                                    contentDescription = story.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.dp, BgDark, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = story.name,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Grid / Feed Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                        .padding(3.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setFeedView("feed") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (viewMode == "feed") RosePrimary else Color.Transparent)
                            .testTag("feed_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ViewAgenda,
                            contentDescription = "Feed",
                            tint = if (viewMode == "feed") SurfaceDark else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setFeedView("grid") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (viewMode == "grid") RosePrimary else Color.Transparent)
                            .testTag("grid_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "Cuadrícula",
                            tint = if (viewMode == "grid") SurfaceDark else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (viewMode == "grid") {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val sampleImages = listOf(
                        R.drawable.feed2, R.drawable.feed3, R.drawable.feed1,
                        R.drawable.feed1, R.drawable.feed2, R.drawable.feed3,
                        R.drawable.feed3, R.drawable.feed1, R.drawable.feed2
                    )
                    sampleImages.chunked(3).forEach { rowList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            rowList.forEach { imgRes ->
                                Image(
                                    painter = painterResource(id = imgRes),
                                    contentDescription = "Foto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onToggleLike = { viewModel.toggleLikePost(post.id) },
                    onDoubleTapLike = { viewModel.likePost(post.id) },
                    onAddComment = { comment -> viewModel.addPostComment(post.id, comment) }
                )
            }
        }
    }

    // Story Dialog
    activeStory?.let { story ->
        StoryViewerDialog(
            story = story,
            onClose = { viewModel.closeStory() }
        )
    }
}

@Composable
private fun PostCard(
    post: FeedPost,
    onToggleLike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentsExpanded by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var showHeartAnimation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RoseGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.take(1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceDark
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Ubicación",
                            tint = RosePrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = post.place,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                Text(
                    text = post.timeAgo,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Image with double tap to like
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                onDoubleTapLike()
                                showHeartAnimation = true
                            }
                        )
                    }
            ) {
                Image(
                    painter = painterResource(id = post.imageRes),
                    contentDescription = post.text,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (showHeartAnimation) {
                    LaunchedEffect(Unit) {
                        delay(900)
                        showHeartAnimation = false
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Like",
                            tint = AccentPink,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // Footer & details
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice note player if exists
                post.voiceSeconds?.let { secs ->
                    VoiceNotePlayer(seconds = secs)
                }

                // Like & Comments action icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.clickable { onToggleLike() }
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) AccentPink else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (post.isLiked) "1/1 ❤️" else "0/1",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.clickable { commentsExpanded = !commentsExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = post.comments.size.toString(),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Post Caption
                Text(
                    text = "${post.author}  ${post.text}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                // Comments List & Input
                if (commentsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.comments.forEach { comment ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = comment.author,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = comment.text,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Comentario íntimo…", fontSize = 12.sp, color = TextMuted) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDarkElevated,
                                    unfocusedContainerColor = SurfaceDarkElevated,
                                    focusedBorderColor = RosePrimary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commentText.isNotBlank()) {
                                        onAddComment(commentText.trim())
                                        commentText = ""
                                    }
                                })
                            )

                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        onAddComment(commentText.trim())
                                        commentText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RoseGradient)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Enviar",
                                    tint = SurfaceDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceNotePlayer(seconds: Int) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPos by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            if (currentPos >= seconds) {
                isPlaying = false
                currentPos = 0f
            } else {
                currentPos += 0.1f
            }
        }
    }

    val bars = listOf(4, 9, 6, 12, 8, 14, 5, 10, 7, 13, 6, 9, 11, 4, 8, 12, 6, 10, 5, 7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceDarkElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(RoseGradient)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = SurfaceDark,
                modifier = Modifier.size(16.dp)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bars.forEachIndexed { index, height ->
                val progressFrac = currentPos / seconds.toFloat()
                val isFilled = (index.toFloat() / bars.size.toFloat()) <= progressFrac
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((height * 1.5).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isFilled) RosePrimary else TextMuted.copy(alpha = 0.4f))
                )
            }
        }

        val remainingSecs = (seconds - currentPos).coerceAtLeast(0f).toInt()
        Text(
            text = "0:%02d".format(remainingSecs),
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun StoryViewerDialog(
    story: StoryItem,
    onClose: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.015f
        }
        onClose()
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
        ) {
            Image(
                painter = painterResource(id = story.imageRes),
                contentDescription = story.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            // Top bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = RosePrimary,
                    trackColor = SurfaceDarkElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${story.name} · ${story.timeAgo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
