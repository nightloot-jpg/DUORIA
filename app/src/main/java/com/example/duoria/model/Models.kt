package com.example.duoria.model

data class PersonInfo(
    val name: String,
    val city: String,
    val timeZone: String,
    val temperature: String,
    val weatherType: WeatherType,
    val battery: Int,
    val status: String,
    val isOnline: Boolean
)

enum class WeatherType {
    SUNNY, RAINY, CLOUDY
}

data class NoteItem(
    val id: Long,
    val text: String,
    val author: String,
    val isDone: Boolean
)

data class CommentItem(
    val author: String,
    val text: String
)

data class FeedPost(
    val id: Long,
    val author: String,
    val imageRes: Int,
    val text: String,
    val place: String,
    val timeAgo: String,
    val voiceSeconds: Int? = null,
    val isLiked: Boolean = false,
    val comments: List<CommentItem> = emptyList()
)

data class StoryItem(
    val name: String,
    val imageRes: Int,
    val timeAgo: String
)

data class ChallengeItem(
    val id: Int,
    val title: String,
    val description: String,
    val iconType: String,
    val completedByMe: Boolean,
    val completedByPartner: Boolean
)

data class SecretQuestion(
    val id: Long,
    val question: String,
    val author: String,
    val isAnswered: Boolean,
    val answer: String? = null
)

data class VideoLibraryItem(
    val title: String,
    val url: String
)

data class ChatMessage(
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DesireCard(
    val id: Int,
    val title: String,
    val partnerAgreed: Boolean
)

data class VoiceMemory(
    val id: Int,
    val title: String,
    val author: String,
    val duration: String
)
