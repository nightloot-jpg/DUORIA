package com.example.duoria.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duoria.ui.components.PageHeader
import com.example.duoria.ui.theme.*
import com.example.duoria.viewmodel.DuoriaViewModel

@Composable
fun BovedaScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()

    if (isUnlocked) {
        VaultContent(viewModel = viewModel, modifier = modifier)
    } else {
        VaultLockScreen(viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun VaultLockScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    val pinError by viewModel.pinError.collectAsState()
    val isBioScanning by viewModel.isBiometricScanning.collectAsState()

    fun handleDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val next = enteredPin + digit
            enteredPin = next
            if (next.length == 4) {
                val success = viewModel.verifyPin(next)
                if (!success) {
                    enteredPin = ""
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Lock Emblem
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(RoseGradient)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = AccentPink),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Bóveda",
                tint = SurfaceDark,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bóveda Íntima",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = RosePrimary
        )

        Text(
            text = "Introduce vuestro PIN (demo: 1402)",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (pinError) DestructiveRed else RosePrimary,
                            shape = CircleShape
                        )
                        .background(if (filled) (if (pinError) DestructiveRed else RosePrimary) else Color.Transparent)
                )
            }
        }

        if (pinError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PIN incorrecto",
                fontSize = 12.sp,
                color = DestructiveRed
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Keypad (3x4)
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("bio", "0", "del")
        )

        Column(
            modifier = Modifier.width(260.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { key ->
                        when (key) {
                            "bio" -> {
                                IconButton(
                                    onClick = { viewModel.unlockVaultWithBiometrics() },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .testTag("biometric_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Biometría",
                                        tint = RosePrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            "del" -> {
                                IconButton(
                                    onClick = {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .testTag("pin_delete_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Backspace,
                                        contentDescription = "Borrar",
                                        tint = TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CardBackground)
                                        .border(1.dp, CardBorder, CircleShape)
                                        .clickable { handleDigitPress(key) }
                                        .testTag("pin_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isBioScanning) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Verificando huella…",
                fontSize = 13.sp,
                color = RosePrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VaultContent(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("match") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                title = "Bóveda",
                subtitle = "Solo vosotros dos",
                action = {
                    OutlinedButton(
                        onClick = { viewModel.lockVault() },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("lock_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Bloquear",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Bloquear", fontSize = 11.sp)
                    }
                }
            )
        }

        // Segmented Tab
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == "match") RoseGradient else SolidColor(Color.Transparent))
                        .clickable { selectedTab = "match" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_desire_match"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Desire Match",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == "match") FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selectedTab == "match") SurfaceDark else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == "recuerdos") RoseGradient else SolidColor(Color.Transparent))
                        .clickable { selectedTab = "recuerdos" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_recuerdos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Recuerdos",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == "recuerdos") FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selectedTab == "recuerdos") SurfaceDark else TextSecondary
                    )
                }
            }
        }

        if (selectedTab == "match") {
            item {
                DesireMatchSection(viewModel = viewModel)
            }
        } else {
            item {
                VoiceVaultSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun DesireMatchSection(viewModel: DuoriaViewModel) {
    val currentIndex by viewModel.currentDesireIndex.collectAsState()
    val matches by viewModel.desireMatches.collectAsState()
    val matchCelebration by viewModel.matchCelebration.collectAsState()
    val cards = viewModel.desireCards

    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Doble ciega: solo se revela si ambos decís que sí.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        // Swipeable Card Stack Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentIndex < cards.size) {
                val currentCard = cards[currentIndex]

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = dragOffset.dp)
                        .rotate(dragOffset / 20f)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(28.dp))
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), spotColor = GlowColor)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragOffset > 90) {
                                        viewModel.decideDesire(true)
                                    } else if (dragOffset < -90) {
                                        viewModel.decideDesire(false)
                                    }
                                    dragOffset = 0f
                                },
                                onDragCancel = { dragOffset = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragOffset += dragAmount
                                }
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Deseo",
                                tint = RosePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentCard.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Desliza → sí · ← no",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        // SÍ / NO Pill overlay during drag
                        if (dragOffset > 40) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, AccentSuccess, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "SÍ", color = AccentSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else if (dragOffset < -40) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, DestructiveRed, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "NO", color = DestructiveRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Has visto todas las cartas de hoy ✨",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetDesireCards() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary)
                        ) {
                            Text("Revisar de nuevo")
                        }
                    }
                }
            }

            // Match Celebration Popup
            if (matchCelebration != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xEE171214)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Match",
                            tint = AccentPink,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "¡Es un match!",
                            fontFamily = FontFamily.Serif,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = matchCelebration ?: "",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Action Buttons (X & Heart)
        if (currentIndex < cards.size) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.decideDesire(false) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("desire_reject_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "No",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.decideDesire(true) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RoseGradient)
                        .shadow(elevation = 12.dp, shape = CircleShape, spotColor = AccentPink)
                        .testTag("desire_accept_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Sí",
                        tint = SurfaceDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Matches List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Vuestros matches",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (matches.isEmpty()) {
                Text(
                    text = "Aún no hay coincidencias reveladas.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                matches.forEach { matchText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💞", fontSize = 14.sp)
                        Text(
                            text = matchText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceVaultSection(viewModel: DuoriaViewModel) {
    val memories by viewModel.voiceMemories.collectAsState()
    val playingId by viewModel.playingMemoryId.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Encryption banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDarkElevated)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = "Seguridad",
                tint = AccentSuccess,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Cifrado de extremo a extremo · solo vuestros dispositivos",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Voice Memory Cards
        memories.forEach { item ->
            val isPlaying = playingId == item.id
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleMemoryPlayback(item.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = SurfaceDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.author} · ${item.duration}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Cifrado",
                        tint = RosePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Encrypted photo tiles placeholder grid
        Text(
            text = "Álbum secreto cifrado",
            fontSize = 12.sp,
            color = TextMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until 3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Foto protegida",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until 3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Foto protegida",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
