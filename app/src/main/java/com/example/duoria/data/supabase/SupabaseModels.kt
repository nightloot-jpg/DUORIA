package com.example.duoria.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: SupabaseUser? = null
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    @SerialName("user_metadata") val userMetadata: Map<String, String>? = null
)

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val city: String? = "Madrid",
    val status: String? = "Libre",
    @SerialName("battery_level") val batteryLevel: Int? = 100,
    @SerialName("couple_id") val coupleId: String? = null
)

@Serializable
data class NoteDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("author_id") val authorId: String? = null,
    val author: String? = null,
    val text: String,
    @SerialName("is_done") val isDone: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PostDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("author_id") val authorId: String? = null,
    val author: String? = null,
    val text: String,
    val place: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("voice_url") val voiceUrl: String? = null,
    @SerialName("voice_duration") val voiceDuration: Int? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PostLikeDto(
    @SerialName("post_id") val postId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
data class PostCommentDto(
    val id: String? = null,
    @SerialName("post_id") val postId: String,
    @SerialName("author_id") val authorId: String? = null,
    val author: String? = null,
    val text: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class MessageDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("sender_id") val senderId: String? = null,
    val author: String? = null,
    val text: String,
    val read: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class DailyAnswerDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("question_date") val questionDate: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val answer: String
)

@Serializable
data class SecretQuestionDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("author_id") val authorId: String? = null,
    val author: String? = null,
    val question: String,
    val answer: String? = null,
    @SerialName("is_answered") val isAnswered: Boolean = false
)

@Serializable
data class DesireMatchDto(
    val id: String? = null,
    @SerialName("couple_id") val coupleId: String,
    @SerialName("desire_key") val desireKey: String,
    @SerialName("user1_choice") val user1Choice: Boolean? = null,
    @SerialName("user2_choice") val user2Choice: Boolean? = null,
    @SerialName("is_match") val isMatch: Boolean = false
)
