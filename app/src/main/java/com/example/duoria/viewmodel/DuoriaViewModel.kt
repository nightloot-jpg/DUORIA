package com.example.duoria.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.duoria.R
import com.example.duoria.data.supabase.SupabaseClient
import com.example.duoria.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class TimeState(
    val madridTime: String = "--:--",
    val tokyoTime: String = "--:--",
    val countdownDays: Long = 0,
    val countdownHours: Long = 0,
    val countdownMinutes: Long = 0,
    val countdownSeconds: Long = 0
)

data class FloatingEmoji(
    val id: Long,
    val emoji: String,
    val xPercent: Float
)

class DuoriaViewModel(application: Application) : AndroidViewModel(application) {

    val supabaseClient = SupabaseClient(application.applicationContext)

    // Auth State
    private val _isAuthenticated = MutableStateFlow(supabaseClient.isAuthenticated)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow(supabaseClient.currentUserName)
    val currentUser: StateFlow<String> = _currentUser.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    // Clocks
    private val _timeState = MutableStateFlow(TimeState())
    val timeState: StateFlow<TimeState> = _timeState.asStateFlow()

    private val targetReunion = ZonedDateTime.of(2026, 10, 18, 10, 0, 0, 0, ZoneId.of("Europe/Madrid"))

    // Heartbeat
    private val _isHeartbeatActive = MutableStateFlow(false)
    val isHeartbeatActive: StateFlow<Boolean> = _isHeartbeatActive.asStateFlow()

    private val _heartbeatRipples = MutableStateFlow<List<Long>>(emptyList())
    val heartbeatRipples: StateFlow<List<Long>> = _heartbeatRipples.asStateFlow()

    private var heartbeatJob: Job? = null

    // Notes
    private val _notes = MutableStateFlow(
        listOf(
            NoteItem(1, "Videollamada el domingo a las 11h (mi hora) 💛", "Yuki", false),
            NoteItem(2, "Comprar billetes de tren Kioto", "Alberto", true),
            NoteItem(3, "Mándame la receta del ramen", "Alberto", false)
        )
    )
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    // Conexión
    private val _dailyAnswer = MutableStateFlow("")
    val dailyAnswer: StateFlow<String> = _dailyAnswer.asStateFlow()

    private val _isDailyAnswerSent = MutableStateFlow(false)
    val isDailyAnswerSent: StateFlow<Boolean> = _isDailyAnswerSent.asStateFlow()

    private val _dailyChat = MutableStateFlow(
        listOf(
            ChatMessage("Yuki", "Jajaja sabía que dirías eso 🥹")
        )
    )
    val dailyChat: StateFlow<List<ChatMessage>> = _dailyChat.asStateFlow()

    private val _challenges = MutableStateFlow(
        listOf(
            ChallengeItem(1, "Cocinar lo mismo", "Preparad la misma receta y cenad en videollamada.", "chef", completedByMe = true, completedByPartner = false),
            ChallengeItem(2, "Foto espontánea", "Mandad una foto de lo que veis ahora mismo, sin filtros.", "camera", completedByMe = true, completedByPartner = true),
            ChallengeItem(3, "Playlist cruzada", "Añadid 5 canciones que os recuerden al otro.", "music", completedByMe = false, completedByPartner = true),
            ChallengeItem(4, "Misma luna", "Fotografiad la luna desde vuestra ciudad esta noche.", "moon", completedByMe = false, completedByPartner = false)
        )
    )
    val challenges: StateFlow<List<ChallengeItem>> = _challenges.asStateFlow()

    private val _secretQuestions = MutableStateFlow(
        listOf(
            SecretQuestion(1, "¿Cuál fue el primer momento en que supiste que me querías?", "Yuki", false),
            SecretQuestion(2, "Si pudieras revivir un día conmigo, ¿cuál sería?", "Tú", true, "El día que nos quedamos atrapados por la lluvia en el templo.")
        )
    )
    val secretQuestions: StateFlow<List<SecretQuestion>> = _secretQuestions.asStateFlow()

    // Feed
    private val _feedPosts = MutableStateFlow(
        listOf(
            FeedPost(
                id = 1,
                author = "Yuki",
                imageRes = R.drawable.feed2,
                text = "Shibuya bajo la lluvia. Pensé en ti en cada paraguas.",
                place = "Shibuya, Tokio",
                timeAgo = "hace 2 h",
                voiceSeconds = 14,
                isLiked = true,
                comments = listOf(CommentItem("Alberto", "Quiero pasear ahí contigo 🌧️"))
            ),
            FeedPost(
                id = 2,
                author = "Alberto",
                imageRes = R.drawable.feed3,
                text = "El atardecer de hoy te pertenece.",
                place = "La Latina, Madrid",
                timeAgo = "ayer",
                isLiked = false,
                comments = emptyList()
            ),
            FeedPost(
                id = 3,
                author = "Yuki",
                imageRes = R.drawable.feed1,
                text = "Nuestro café favorito, versión a distancia ☕",
                place = "Kioto",
                timeAgo = "hace 3 días",
                voiceSeconds = 22,
                isLiked = true,
                comments = emptyList()
            )
        )
    )
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()

    private val _feedViewMode = MutableStateFlow("feed")
    val feedViewMode: StateFlow<String> = _feedViewMode.asStateFlow()

    private val _activeStory = MutableStateFlow<StoryItem?>(null)
    val activeStory: StateFlow<StoryItem?> = _activeStory.asStateFlow()

    // Cine
    val videoLibrary = listOf(
        VideoLibraryItem("Big Buck Bunny", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
        VideoLibraryItem("Sintel", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
        VideoLibraryItem("Tears of Steel", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
    )

    private val _currentVideoUrl = MutableStateFlow(videoLibrary[0].url)
    val currentVideoUrl: StateFlow<String> = _currentVideoUrl.asStateFlow()

    private val _isVoiceChannelActive = MutableStateFlow(false)
    val isVoiceChannelActive: StateFlow<Boolean> = _isVoiceChannelActive.asStateFlow()

    private val _cineFloatingEmojis = MutableStateFlow<List<FloatingEmoji>>(emptyList())
    val cineFloatingEmojis: StateFlow<List<FloatingEmoji>> = _cineFloatingEmojis.asStateFlow()

    private val _cineChat = MutableStateFlow(
        listOf(
            ChatMessage("Yuki", "¡Lista! Dale al play cuando quieras 🍿")
        )
    )
    val cineChat: StateFlow<List<ChatMessage>> = _cineChat.asStateFlow()

    // Bóveda
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    private val _isBiometricScanning = MutableStateFlow(false)
    val isBiometricScanning: StateFlow<Boolean> = _isBiometricScanning.asStateFlow()

    val desireCards = listOf(
        DesireCard(1, "Baño a la luz de las velas", partnerAgreed = true),
        DesireCard(2, "Fin de semana sin móviles", partnerAgreed = true),
        DesireCard(3, "Carta erótica escrita a mano", partnerAgreed = false),
        DesireCard(4, "Masaje con aceites al reencontrarnos", partnerAgreed = true),
        DesireCard(5, "Desayuno en la cama… y quedarnos", partnerAgreed = true)
    )

    private val _currentDesireIndex = MutableStateFlow(0)
    val currentDesireIndex: StateFlow<Int> = _currentDesireIndex.asStateFlow()

    private val _desireMatches = MutableStateFlow<List<String>>(emptyList())
    val desireMatches: StateFlow<List<String>> = _desireMatches.asStateFlow()

    private val _matchCelebration = MutableStateFlow<String?>(null)
    val matchCelebration: StateFlow<String?> = _matchCelebration.asStateFlow()

    private val _voiceMemories = MutableStateFlow(
        listOf(
            VoiceMemory(1, "Buenas noches, mi amor", "Yuki", "0:48"),
            VoiceMemory(2, "Lo que no te dije en el aeropuerto", "Alberto", "2:12"),
            VoiceMemory(3, "Nuestro primer aniversario", "Yuki", "1:05")
        )
    )
    val voiceMemories: StateFlow<List<VoiceMemory>> = _voiceMemories.asStateFlow()

    private val _playingMemoryId = MutableStateFlow<Int?>(null)
    val playingMemoryId: StateFlow<Int?> = _playingMemoryId.asStateFlow()

    init {
        startTimeUpdater()
        loadDataFromSupabase()
    }

    private fun loadDataFromSupabase() {
        viewModelScope.launch {
            // Notes from Supabase
            val notesResult = supabaseClient.getNotes()
            notesResult.onSuccess { dtoList ->
                if (dtoList.isNotEmpty()) {
                    _notes.value = dtoList.mapIndexed { idx, dto ->
                        NoteItem(
                            id = dto.id?.hashCode()?.toLong() ?: idx.toLong(),
                            text = dto.text,
                            author = dto.author ?: "Yuki",
                            isDone = dto.isDone
                        )
                    }
                }
            }

            // Posts from Supabase
            val postsResult = supabaseClient.getPosts()
            postsResult.onSuccess { dtoList ->
                if (dtoList.isNotEmpty()) {
                    _feedPosts.value = dtoList.mapIndexed { idx, dto ->
                        FeedPost(
                            id = dto.id?.hashCode()?.toLong() ?: idx.toLong(),
                            author = dto.author ?: "Yuki",
                            imageRes = when (idx % 3) {
                                0 -> R.drawable.feed2
                                1 -> R.drawable.feed3
                                else -> R.drawable.feed1
                            },
                            text = dto.text,
                            place = dto.place ?: "Tokio",
                            timeAgo = "reciente",
                            voiceSeconds = dto.voiceDuration,
                            isLiked = false,
                            comments = emptyList()
                        )
                    }
                }
            }

            // Messages from Supabase
            val msgsResult = supabaseClient.getMessages()
            msgsResult.onSuccess { dtoList ->
                if (dtoList.isNotEmpty()) {
                    _dailyChat.value = dtoList.map { dto ->
                        ChatMessage(dto.author ?: "Yuki", dto.text)
                    }
                }
            }

            // Secret questions
            val sqResult = supabaseClient.getSecretQuestions()
            sqResult.onSuccess { dtoList ->
                if (dtoList.isNotEmpty()) {
                    _secretQuestions.value = dtoList.mapIndexed { idx, dto ->
                        SecretQuestion(
                            id = dto.id?.hashCode()?.toLong() ?: idx.toLong(),
                            question = dto.question,
                            author = dto.author ?: "Tú",
                            isAnswered = dto.isAnswered,
                            answer = dto.answer
                        )
                    }
                }
            }
        }
    }

    private fun startTimeUpdater() {
        viewModelScope.launch {
            val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            while (isActive) {
                val nowUtc = ZonedDateTime.now(ZoneId.of("UTC"))
                val madrid = nowUtc.withZoneSameInstant(ZoneId.of("Europe/Madrid"))
                val tokyo = nowUtc.withZoneSameInstant(ZoneId.of("Asia/Tokyo"))

                val target = targetReunion.withZoneSameInstant(ZoneId.of("UTC"))
                val totalSeconds = ChronoUnit.SECONDS.between(nowUtc, target).coerceAtLeast(0)

                val days = totalSeconds / (24 * 3600)
                val hours = (totalSeconds % (24 * 3600)) / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                _timeState.value = TimeState(
                    madridTime = madrid.format(timeFmt),
                    tokyoTime = tokyo.format(timeFmt),
                    countdownDays = days,
                    countdownHours = hours,
                    countdownMinutes = minutes,
                    countdownSeconds = seconds
                )
                delay(1000)
            }
        }
    }

    // ==========================================
    // AUTH ACTIONS
    // ==========================================

    fun openAuthDialog() {
        _showAuthDialog.value = true
        _authError.value = null
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
        _authError.value = null
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Por favor ingresa correo y contraseña"
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = supabaseClient.signIn(email, pass)
            _authLoading.value = false
            result.onSuccess {
                _isAuthenticated.value = true
                _currentUser.value = supabaseClient.currentUserName
                _showAuthDialog.value = false
                loadDataFromSupabase()
            }.onFailure { err ->
                _authError.value = err.message ?: "Error al iniciar sesión"
            }
        }
    }

    fun signUp(email: String, pass: String, username: String, fullName: String) {
        if (email.isBlank() || pass.isBlank() || username.isBlank()) {
            _authError.value = "Por favor completa todos los campos"
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = supabaseClient.signUp(email, pass, username, fullName)
            _authLoading.value = false
            result.onSuccess {
                _isAuthenticated.value = true
                _currentUser.value = username
                _showAuthDialog.value = false
                loadDataFromSupabase()
            }.onFailure { err ->
                _authError.value = err.message ?: "Error al registrar cuenta"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.signOut()
            _isAuthenticated.value = false
            _currentUser.value = "Alberto"
        }
    }

    // ==========================================
    // HEARTBEAT
    // ==========================================

    fun startHeartbeat(context: Context) {
        _isHeartbeatActive.value = true
        triggerPulse(context)
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive && _isHeartbeatActive.value) {
                delay(900)
                if (_isHeartbeatActive.value) {
                    triggerPulse(context)
                }
            }
        }
    }

    fun stopHeartbeat() {
        _isHeartbeatActive.value = false
        heartbeatJob?.cancel()
    }

    private fun triggerPulse(context: Context) {
        val id = System.currentTimeMillis()
        _heartbeatRipples.update { it + id }
        viewModelScope.launch {
            delay(1600)
            _heartbeatRipples.update { list -> list.filter { it != id } }
        }

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 40, 80, 40)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // NOTES (CRUD & SUPABASE PERSISTENCE)
    // ==========================================

    fun addNote(text: String) {
        if (text.isBlank()) return
        val authorName = _currentUser.value
        val newNote = NoteItem(
            id = System.currentTimeMillis(),
            text = text.trim(),
            author = authorName,
            isDone = false
        )
        _notes.update { listOf(newNote) + it }
        viewModelScope.launch {
            supabaseClient.createNote(text, authorName)
        }
    }

    fun toggleNote(id: Long) {
        val current = _notes.value.firstOrNull { it.id == id }
        val newDone = !(current?.isDone ?: false)
        _notes.update { list ->
            list.map { if (it.id == id) it.copy(isDone = newDone) else it }
        }
        viewModelScope.launch {
            supabaseClient.toggleNote(id.toString(), newDone)
        }
    }

    fun deleteNote(id: Long) {
        _notes.update { list -> list.filter { it.id != id } }
        viewModelScope.launch {
            supabaseClient.deleteNote(id.toString())
        }
    }

    // ==========================================
    // CONEXIÓN & PREGUNTAS
    // ==========================================

    fun setDailyAnswerText(text: String) {
        _dailyAnswer.value = text
    }

    fun submitDailyAnswer() {
        if (_dailyAnswer.value.isNotBlank()) {
            _isDailyAnswerSent.value = true
        }
    }

    fun addDailyChatMessage(text: String) {
        if (text.isBlank()) return
        val myName = _currentUser.value
        val newMsg = ChatMessage(myName, text.trim())
        _dailyChat.update { it + newMsg }
        viewModelScope.launch {
            supabaseClient.sendMessage(text, myName)
            delay(1200)
            _dailyChat.update { it + ChatMessage("Yuki", "Te quiero infinito ❤️") }
        }
    }

    fun toggleChallenge(id: Int) {
        _challenges.update { list ->
            list.map {
                if (it.id == id) it.copy(completedByMe = !it.completedByMe) else it
            }
        }
    }

    fun addSecretQuestion(question: String) {
        if (question.isBlank()) return
        val myName = _currentUser.value
        val item = SecretQuestion(
            id = System.currentTimeMillis(),
            question = question.trim(),
            author = myName,
            isAnswered = false
        )
        _secretQuestions.update { listOf(item) + it }
        viewModelScope.launch {
            supabaseClient.addSecretQuestion(question, myName)
        }
    }

    fun answerSecretQuestion(id: Long, answer: String) {
        if (answer.isBlank()) return
        _secretQuestions.update { list ->
            list.map {
                if (it.id == id) it.copy(isAnswered = true, answer = answer.trim()) else it
            }
        }
        viewModelScope.launch {
            supabaseClient.answerSecretQuestion(id.toString(), answer)
        }
    }

    // ==========================================
    // FEED & PUBLICACIONES
    // ==========================================

    fun setFeedView(mode: String) {
        _feedViewMode.value = mode
    }

    fun toggleLikePost(postId: Long) {
        _feedPosts.update { list ->
            list.map {
                if (it.id == postId) it.copy(isLiked = !it.isLiked) else it
            }
        }
    }

    fun likePost(postId: Long) {
        _feedPosts.update { list ->
            list.map {
                if (it.id == postId) it.copy(isLiked = true) else it
            }
        }
    }

    fun addPostComment(postId: Long, text: String) {
        if (text.isBlank()) return
        val myName = _currentUser.value
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + CommentItem(myName, text.trim()))
                } else post
            }
        }
        viewModelScope.launch {
            supabaseClient.addComment(postId.toString(), myName, text)
        }
    }

    fun addPost(text: String, place: String) {
        if (text.isBlank()) return
        val myName = _currentUser.value
        val newPost = FeedPost(
            id = System.currentTimeMillis(),
            author = myName,
            imageRes = R.drawable.feed3,
            text = text.trim(),
            place = place.ifBlank { "Madrid" },
            timeAgo = "Ahora",
            isLiked = false,
            comments = emptyList()
        )
        _feedPosts.update { listOf(newPost) + it }
        viewModelScope.launch {
            supabaseClient.createPost(
                text = text,
                author = myName,
                place = place,
                imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb"
            )
        }
    }

    fun openStory(story: StoryItem) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    // ==========================================
    // CINE
    // ==========================================

    fun selectVideo(url: String) {
        _currentVideoUrl.value = url
    }

    fun toggleVoiceChannel() {
        _isVoiceChannelActive.value = !_isVoiceChannelActive.value
    }

    fun triggerCineReaction(emoji: String) {
        val id = System.currentTimeMillis() + (0..1000).random()
        val x = (15..85).random().toFloat()
        val item = FloatingEmoji(id, emoji, x)
        _cineFloatingEmojis.update { it + item }
        viewModelScope.launch {
            delay(2400)
            _cineFloatingEmojis.update { list -> list.filter { it.id != id } }
        }
    }

    fun sendCineChatMessage(text: String) {
        if (text.isBlank()) return
        val myName = _currentUser.value
        val msg = ChatMessage(myName, text.trim())
        _cineChat.update { it + msg }
        viewModelScope.launch {
            delay(1200)
            _cineChat.update { it + ChatMessage("Yuki", "💕") }
            triggerCineReaction("❤️")
        }
    }

    // ==========================================
    // BÓVEDA
    // ==========================================

    fun verifyPin(pin: String): Boolean {
        if (pin == "1402") {
            _isVaultUnlocked.value = true
            _pinError.value = false
            return true
        } else {
            _pinError.value = true
            viewModelScope.launch {
                delay(800)
                _pinError.value = false
            }
            return false
        }
    }

    fun unlockVaultWithBiometrics() {
        _isBiometricScanning.value = true
        viewModelScope.launch {
            delay(1200)
            _isBiometricScanning.value = false
            _isVaultUnlocked.value = true
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun decideDesire(yes: Boolean) {
        val idx = _currentDesireIndex.value
        if (idx < desireCards.size) {
            val card = desireCards[idx]
            if (yes && card.partnerAgreed) {
                _desireMatches.update { it + card.title }
                _matchCelebration.value = card.title
                viewModelScope.launch {
                    delay(2000)
                    _matchCelebration.value = null
                }
            }
            _currentDesireIndex.value = idx + 1
        }
    }

    fun resetDesireCards() {
        _currentDesireIndex.value = 0
    }

    fun toggleMemoryPlayback(id: Int) {
        _playingMemoryId.value = if (_playingMemoryId.value == id) null else id
    }
}
