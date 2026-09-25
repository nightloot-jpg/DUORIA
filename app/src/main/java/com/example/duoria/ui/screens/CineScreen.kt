package com.example.duoria.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.duoria.ui.components.PageHeader
import com.example.duoria.ui.theme.*
import com.example.duoria.viewmodel.DuoriaViewModel
import com.example.duoria.viewmodel.FloatingEmoji
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun CineScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUrl by viewModel.currentVideoUrl.collectAsState()
    val isVoiceActive by viewModel.isVoiceChannelActive.collectAsState()
    val floatingEmojis by viewModel.cineFloatingEmojis.collectAsState()
    val chatMessages by viewModel.cineChat.collectAsState()
    var customLinkInput by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(currentUrl) {
        val mediaItem = MediaItem.fromUri(currentUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        isPlaying = false

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Time ticker for progress bar
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val m = totalSecs / 60
        val s = totalSecs % 60
        return "%d:%02d".format(m, s)
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
                title = "Sala de Cine",
                subtitle = "En directo con Yuki"
            )
        }

        // Video Player Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = GlowColor),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                    )

                    // Floating Emojis Layer
                    floatingEmojis.forEach { item ->
                        FloatingEmojiItem(item = item)
                    }

                    // Live Synced Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xBB171214))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentSuccess)
                        )
                        Text(
                            text = "Sincronizado · 2 viendo",
                            fontSize = 10.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Video Controller Slider
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                            .testTag("cine_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = SurfaceDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = formatDuration(currentPositionMs),
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Slider(
                        value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()) else 0f,
                        onValueChange = { frac ->
                            val target = (frac * durationMs).toLong()
                            exoPlayer.seekTo(target)
                            currentPositionMs = target
                        },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = RosePrimary,
                            activeTrackColor = RosePrimary,
                            inactiveTrackColor = SurfaceDarkElevated
                        )
                    )

                    Text(
                        text = formatDuration(durationMs),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Reaction Bar & Mic Voice Channel Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reactions pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("❤️", "🍿", "😂", "😭", "😍", "🔥").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { viewModel.triggerCineReaction(emoji) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Voice Mic Button
                IconButton(
                    onClick = { viewModel.toggleVoiceChannel() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isVoiceActive) RoseGradient else androidx.compose.ui.graphics.SolidColor(CardBackground))
                        .border(1.dp, if (isVoiceActive) RosePrimary else CardBorder, CircleShape)
                        .testTag("cine_mic_button")
                ) {
                    Icon(
                        imageVector = if (isVoiceActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Canal de voz",
                        tint = if (isVoiceActive) SurfaceDark else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (isVoiceActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎙️ Canal de voz abierto con Yuki",
                    fontSize = 12.sp,
                    color = RosePrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Video Library & URL Loader
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Biblioteca de vídeos",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.videoLibrary) { item ->
                            val isSelected = currentUrl == item.url
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) RoseGradient else androidx.compose.ui.graphics.SolidColor(SurfaceDarkElevated))
                                    .clickable { viewModel.selectVideo(item.url) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) SurfaceDark else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customLinkInput,
                            onValueChange = { customLinkInput = it },
                            placeholder = { Text("Enlace .mp4 o directo…", fontSize = 12.sp, color = TextMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Link,
                                    contentDescription = "Link",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cine_url_input"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDarkElevated,
                                unfocusedContainerColor = SurfaceDarkElevated,
                                focusedBorderColor = RosePrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (customLinkInput.isNotBlank()) {
                                    viewModel.selectVideo(customLinkInput.trim())
                                    customLinkInput = ""
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary, contentColor = SurfaceDark)
                        ) {
                            Text("Cargar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Room Chat
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Chat de sala",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chatMessages.forEach { msg ->
                            val isMe = msg.author == "Tú" || msg.author == "Alberto"
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isMe) RoseGradient else androidx.compose.ui.graphics.SolidColor(SurfaceDarkElevated))
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = if (isMe) SurfaceDark else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Escribe…", fontSize = 13.sp, color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cine_chat_input"),
                            shape = RoundedCornerShape(24.dp),
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
                                viewModel.sendCineChatMessage(chatInput)
                                chatInput = ""
                            })
                        )
                        IconButton(
                            onClick = {
                                viewModel.sendCineChatMessage(chatInput)
                                chatInput = ""
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RoseGradient)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Enviar",
                                tint = SurfaceDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.FloatingEmojiItem(item: FloatingEmoji) {
    val transition = rememberInfiniteTransition(label = "float_emoji")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )

    Text(
        text = item.emoji,
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = (item.xPercent * 2.5f).dp, y = offsetY.dp)
    )
}
