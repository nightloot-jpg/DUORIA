package com.example.duoria.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duoria.ui.components.PageHeader
import com.example.duoria.ui.theme.*
import com.example.duoria.viewmodel.DuoriaViewModel

@Composable
fun HomeScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    val timeState by viewModel.timeState.collectAsState()
    val isHoldingHeart by viewModel.isHeartbeatActive.collectAsState()
    val ripples by viewModel.heartbeatRipples.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var newNoteText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                title = "Nosotros",
                subtitle = "Espacio compartido",
                action = {
                    IconButton(
                        onClick = { viewModel.openAuthDialog() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BgCard)
                            .testTag("btn_open_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil y Autenticación",
                            tint = AccentGold
                        )
                    }
                }
            )
        }

        // People Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Alberto Card
                PersonCard(
                    name = "Alberto",
                    city = "Madrid",
                    time = timeState.madridTime,
                    temp = "22°",
                    weatherIcon = Icons.Filled.WbSunny,
                    battery = 78,
                    status = "Libre",
                    statusColor = AccentSuccess,
                    modifier = Modifier.weight(1f)
                )

                // Yuki Card
                PersonCard(
                    name = "Yuki",
                    city = "Tokio",
                    time = timeState.tokyoTime,
                    temp = "19°",
                    weatherIcon = Icons.Filled.Cloud,
                    battery = 23,
                    status = "Durmiendo",
                    statusColor = TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Reunion Countdown
        item {
            CountdownCard(
                days = timeState.countdownDays,
                hours = timeState.countdownHours,
                minutes = timeState.countdownMinutes,
                seconds = timeState.countdownSeconds
            )
        }

        // Heartbeat Section
        item {
            HeartbeatSection(
                isHolding = isHoldingHeart,
                ripples = ripples,
                onStartHolding = { viewModel.startHeartbeat(context) },
                onStopHolding = { viewModel.stopHeartbeat() }
            )
        }

        // Pizarrón / Notes
        item {
            NotesSection(
                notes = notes,
                inputText = newNoteText,
                onInputChange = { newNoteText = it },
                onAddNote = {
                    viewModel.addNote(newNoteText)
                    newNoteText = ""
                },
                onToggleNote = { viewModel.toggleNote(it) },
                onDeleteNote = { viewModel.deleteNote(it) }
            )
        }
    }
}

@Composable
private fun PersonCard(
    name: String,
    city: String,
    time: String,
    temp: String,
    weatherIcon: androidx.compose.ui.graphics.vector.ImageVector,
    battery: Int,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RoseGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SurfaceDark
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = status,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = time,
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = city,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = "Clima",
                        tint = RosePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = temp,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (battery < 25) Icons.Filled.BatteryAlert else Icons.Filled.BatteryFull,
                        contentDescription = "Batería",
                        tint = if (battery < 25) AccentPink else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "$battery%",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownCard(
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRÓXIMO REENCUENTRO · 18 OCT",
                style = Typography.labelSmall.copy(letterSpacing = 2.sp, color = TextMuted)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = days.toString(),
                fontFamily = FontFamily.Serif,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = RosePrimary,
                lineHeight = 58.sp
            )
            Text(
                text = "días para volver a vernos",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${hours}h", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Text(text = "${minutes}m", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Text(text = "${seconds}s", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun HeartbeatSection(
    isHolding: Boolean,
    ripples: List<Long>,
    onStartHolding: () -> Unit,
    onStopHolding: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated ripples
            ripples.forEach { _ ->
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(2.dp, RosePrimary.copy(alpha = 0.6f), CircleShape)
                )
            }

            // Main Heart Button
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(if (isHolding) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(RoseGradient)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = AccentPink)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartHolding()
                                tryAwaitRelease()
                                onStopHolding()
                            }
                        )
                    }
                    .testTag("heartbeat_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Enviar latido",
                    tint = SurfaceDark,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isHolding) "Yuki siente tu latido…" else "Mantén pulsado para enviar tu latido",
            fontSize = 13.sp,
            color = if (isHolding) RosePrimary else TextSecondary,
            fontWeight = if (isHolding) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun NotesSection(
    notes: List<com.example.duoria.model.NoteItem>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onAddNote: () -> Unit,
    onToggleNote: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pizarrón",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Add note bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("Nota o recordatorio…", fontSize = 13.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("note_input"),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddNote() })
                )
                IconButton(
                    onClick = onAddNote,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RoseGradient)
                        .testTag("add_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir",
                        tint = SurfaceDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notes.forEach { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkElevated.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleNote(note.id) },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, RosePrimary, CircleShape)
                                .background(if (note.isDone) RosePrimary else Color.Transparent)
                        ) {
                            if (note.isDone) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completado",
                                    tint = SurfaceDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.text,
                                fontSize = 13.sp,
                                color = if (note.isDone) TextMuted else TextPrimary,
                                textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                text = note.author,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(
                            onClick = { onDeleteNote(note.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Eliminar",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
