package com.example.duoria.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duoria.model.ChallengeItem
import com.example.duoria.model.ChatMessage
import com.example.duoria.model.SecretQuestion
import com.example.duoria.ui.components.PageHeader
import com.example.duoria.ui.theme.*
import com.example.duoria.viewmodel.DuoriaViewModel

@Composable
fun ConexionScreen(
    viewModel: DuoriaViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Diaria") }
    val tabs = listOf("Diaria", "Retos", "Secretas")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                title = "Conexión",
                subtitle = "Día 412 juntos"
            )
        }

        // Tabs Segmented Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) RoseGradient else SolidColor(Color.Transparent))
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp)
                            .testTag("conexion_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) SurfaceDark else TextSecondary
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            "Diaria" -> {
                item {
                    DailySection(viewModel = viewModel)
                }
            }
            "Retos" -> {
                item {
                    ChallengesSection(viewModel = viewModel)
                }
            }
            "Secretas" -> {
                item {
                    SecretQuestionsSection(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun DailySection(viewModel: DuoriaViewModel) {
    val dailyAnswer by viewModel.dailyAnswer.collectAsState()
    val isSent by viewModel.isDailyAnswerSent.collectAsState()
    val chatMessages by viewModel.dailyChat.collectAsState()
    var commentInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PREGUNTA DEL DÍA",
                    style = Typography.labelSmall.copy(color = RosePrimary, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¿Qué pequeño gesto mío te hizo sentir más querida esta semana?",
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 28.sp
                )
            }
        }

        // Your Answer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tu respuesta",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isSent) {
                    Text(
                        text = dailyAnswer,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                } else {
                    OutlinedTextField(
                        value = dailyAnswer,
                        onValueChange = { viewModel.setDailyAnswerText(it) },
                        placeholder = { Text("Escribe con el corazón…", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("daily_answer_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDarkElevated,
                            unfocusedContainerColor = SurfaceDarkElevated,
                            focusedBorderColor = RosePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.submitDailyAnswer() },
                        enabled = dailyAnswer.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_daily_answer_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RosePrimary,
                            contentColor = SurfaceDark,
                            disabledContainerColor = SurfaceDarkElevated,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = "Enviar y revelar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Yuki's Answer Card (Locked / Unlocked)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Respuesta de Yuki",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cuando me mandaste el audio cantando a las 3 de la mañana solo para que me durmiera. Me hizo llorar de lo bonito.",
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp,
                        modifier = if (!isSent) Modifier.blur(10.dp) else Modifier
                    )
                }

                if (!isSent) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x77171214)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDarkElevated)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Bloqueado",
                                tint = RosePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Responde para desbloquear",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Chat conversation if revealed
        if (isSent) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Comentad vuestras respuestas",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Mensaje…", fontSize = 13.sp, color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("daily_chat_input"),
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
                                viewModel.addDailyChatMessage(commentInput)
                                commentInput = ""
                            })
                        )
                        IconButton(
                            onClick = {
                                viewModel.addDailyChatMessage(commentInput)
                                commentInput = ""
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
private fun ChallengesSection(viewModel: DuoriaViewModel) {
    val challenges by viewModel.challenges.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        challenges.forEach { item ->
            val isDone = item.completedByMe && item.completedByPartner
            val icon = when (item.iconType) {
                "chef" -> Icons.Filled.Restaurant
                "camera" -> Icons.Filled.CameraAlt
                "music" -> Icons.Filled.MusicNote
                else -> Icons.Filled.Nightlight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        if (isDone) RosePrimary else CardBorder,
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.title,
                                tint = RosePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PillBadge(
                                text = "Tú ${if (item.completedByMe) "✓" else "…"}",
                                active = item.completedByMe
                            )
                            PillBadge(
                                text = "Yuki ${if (item.completedByPartner) "✓" else "…"}",
                                active = item.completedByPartner
                            )
                        }

                        if (isDone) {
                            Text(
                                text = "¡Completado! 💞",
                                fontSize = 12.sp,
                                color = RosePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.toggleChallenge(item.id) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (item.completedByMe) "Deshacer" else "Hecho",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
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
private fun PillBadge(text: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) RosePrimary else SurfaceDarkElevated)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) SurfaceDark else TextMuted
        )
    }
}

@Composable
private fun SecretQuestionsSection(viewModel: DuoriaViewModel) {
    val questions by viewModel.secretQuestions.collectAsState()
    var newQuestionText by remember { mutableStateOf("") }
    var answeringQuestion by remember { mutableStateOf<SecretQuestion?>(null) }
    var answerInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Create secret question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = "Secreto",
                        tint = RosePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Crea una pregunta secreta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newQuestionText,
                        onValueChange = { newQuestionText = it },
                        placeholder = { Text("Solo Yuki podrá verla…", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("secret_question_input"),
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
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addSecretQuestion(newQuestionText)
                            newQuestionText = ""
                        })
                    )
                    IconButton(
                        onClick = {
                            viewModel.addSecretQuestion(newQuestionText)
                            newQuestionText = ""
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Añadir",
                            tint = SurfaceDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Questions List
        questions.forEach { q ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                    .clickable {
                        if (!q.isAnswered && q.author != "Tú") {
                            answeringQuestion = q
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DE ${q.author.uppercase()}",
                        style = Typography.labelSmall.copy(letterSpacing = 1.5.sp, color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = q.question,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        lineHeight = 24.sp
                    )

                    if (!q.answer.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDarkElevated)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Respuesta: ${q.answer}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            q.isAnswered -> "Respondida ✓"
                            q.author == "Tú" -> "Esperando respuesta…"
                            else -> "Toca para responder"
                        },
                        fontSize = 11.sp,
                        color = RosePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Dialog to answer secret question
    if (answeringQuestion != null) {
        AlertDialog(
            onDismissRequest = { answeringQuestion = null },
            title = {
                Text(
                    text = "Responder pregunta",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = answeringQuestion?.question ?: "", fontSize = 14.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { answerInput = it },
                        placeholder = { Text("Escribe tu respuesta sincera…", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDarkElevated,
                            unfocusedContainerColor = SurfaceDarkElevated,
                            focusedBorderColor = RosePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        answeringQuestion?.let {
                            viewModel.answerSecretQuestion(it.id, answerInput)
                        }
                        answerInput = ""
                        answeringQuestion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary, contentColor = SurfaceDark)
                ) {
                    Text("Responder")
                }
            },
            dismissButton = {
                TextButton(onClick = { answeringQuestion = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
