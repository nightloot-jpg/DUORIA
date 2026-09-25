package com.example.duoria.data.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseClient(context: Context) {

    companion object {
        const val SUPABASE_URL = "https://racesoxdciwmrlctkpns.supabase.co"
        const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJhY2Vzb3hkY2l3bXJsY3RrcG5zIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzNTczNjksImV4cCI6MjEwNTkzMzM2OX0.mBenFeiwFpTka3Wd-haoXIdjfqlWvuwkXfdDi8niX3o"
        private const val PREFS_NAME = "duoria_supabase_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_COUPLE_ID = "couple_id"
        private const val TAG = "SupabaseClient"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        private set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var currentUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        private set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var currentUserName: String
        get() = prefs.getString(KEY_USER_NAME, "Alberto") ?: "Alberto"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var coupleId: String
        get() = prefs.getString(KEY_COUPLE_ID, "00000000-0000-0000-0000-000000000001") ?: "00000000-0000-0000-0000-000000000001"
        set(value) = prefs.edit().putString(KEY_COUPLE_ID, value).apply()

    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank()

    private fun newRequestBuilder(url: String): Request.Builder {
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Accept", "application/json")
        
        val token = accessToken ?: SUPABASE_ANON_KEY
        builder.addHeader("Authorization", "Bearer $token")
        return builder
    }

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    suspend fun signUp(email: String, pass: String, username: String, fullName: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/signup"
            val payload = AuthRequest(
                email = email.trim(),
                password = pass,
                data = mapOf("username" to username.trim(), "full_name" to fullName.trim())
            )
            val body = json.encodeToString(payload).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "SignUp failed: $resStr")
                return@withContext Result.failure(Exception("Error al registrar: $resStr"))
            }

            val authRes = json.decodeFromString<AuthResponse>(resStr)
            authRes.accessToken?.let { accessToken = it }
            authRes.user?.id?.let { currentUserId = it }
            currentUserName = username.ifBlank { fullName.ifBlank { "Tú" } }
            Result.success(authRes)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp exception", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=password"
            val payload = AuthRequest(email = email.trim(), password = pass)
            val body = json.encodeToString(payload).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "SignIn failed: $resStr")
                return@withContext Result.failure(Exception("Credenciales inválidas o error de servidor: $resStr"))
            }

            val authRes = json.decodeFromString<AuthResponse>(resStr)
            authRes.accessToken?.let { accessToken = it }
            authRes.user?.id?.let { currentUserId = it }
            authRes.user?.userMetadata?.get("username")?.let { currentUserName = it }
            Result.success(authRes)
        } catch (e: Exception) {
            Log.e(TAG, "SignIn exception", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/logout"
            val request = newRequestBuilder(url)
                .post("{}".toRequestBody(jsonMediaType))
                .build()
            okHttpClient.newCall(request).execute()
            accessToken = null
            currentUserId = null
            Result.success(Unit)
        } catch (e: Exception) {
            accessToken = null
            Result.success(Unit)
        }
    }

    // ==========================================
    // NOTAS (CRUD)
    // ==========================================

    suspend fun getNotes(): Result<List<NoteDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/notes?select=*&order=created_at.desc"
            val request = newRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: "[]"
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al leer notas: $resStr"))
            }
            val list = json.decodeFromString<List<NoteDto>>(resStr)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNote(text: String, author: String): Result<NoteDto> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/notes"
            val note = NoteDto(
                coupleId = coupleId,
                author = author,
                text = text.trim(),
                isDone = false
            )
            val body = json.encodeToString(note).toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al crear nota: $resStr"))
            }
            val created = json.decodeFromString<List<NoteDto>>(resStr).firstOrNull()
                ?: note
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleNote(noteId: String, isDone: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/notes?id=eq.$noteId"
            val body = "{\"is_done\":$isDone}".toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .patch(body)
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al actualizar nota"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/notes?id=eq.$noteId"
            val request = newRequestBuilder(url).delete().build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al borrar nota"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // FEED & PUBLICACIONES
    // ==========================================

    suspend fun getPosts(): Result<List<PostDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/posts?select=*&order=created_at.desc"
            val request = newRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: "[]"
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al obtener feed: $resStr"))
            }
            val list = json.decodeFromString<List<PostDto>>(resStr)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(text: String, author: String, place: String, imageUrl: String, voiceUrl: String? = null, voiceSeconds: Int? = null): Result<PostDto> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/posts"
            val post = PostDto(
                coupleId = coupleId,
                author = author,
                text = text.trim(),
                place = place.trim(),
                imageUrl = imageUrl,
                voiceUrl = voiceUrl,
                voiceDuration = voiceSeconds
            )
            val body = json.encodeToString(post).toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error al publicar post: $resStr"))
            }
            val created = json.decodeFromString<List<PostDto>>(resStr).firstOrNull() ?: post
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(postId: String): Result<List<PostCommentDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/post_comments?post_id=eq.$postId&select=*&order=created_at.asc"
            val request = newRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: "[]"
            val list = json.decodeFromString<List<PostCommentDto>>(resStr)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, author: String, text: String): Result<PostCommentDto> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/post_comments"
            val comment = PostCommentDto(
                postId = postId,
                author = author,
                text = text.trim()
            )
            val body = json.encodeToString(comment).toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            val created = json.decodeFromString<List<PostCommentDto>>(resStr).firstOrNull() ?: comment
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // MENSAJES & CHAT
    // ==========================================

    suspend fun getMessages(): Result<List<MessageDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/messages?select=*&order=created_at.asc"
            val request = newRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: "[]"
            val list = json.decodeFromString<List<MessageDto>>(resStr)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(text: String, author: String): Result<MessageDto> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/messages"
            val msg = MessageDto(
                coupleId = coupleId,
                author = author,
                text = text.trim()
            )
            val body = json.encodeToString(msg).toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            val created = json.decodeFromString<List<MessageDto>>(resStr).firstOrNull() ?: msg
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // PREGUNTAS SECRETAS & RETOS
    // ==========================================

    suspend fun getSecretQuestions(): Result<List<SecretQuestionDto>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/secret_questions?select=*&order=created_at.desc"
            val request = newRequestBuilder(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: "[]"
            val list = json.decodeFromString<List<SecretQuestionDto>>(resStr)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSecretQuestion(question: String, author: String): Result<SecretQuestionDto> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/secret_questions"
            val sq = SecretQuestionDto(
                coupleId = coupleId,
                author = author,
                question = question.trim(),
                isAnswered = false
            )
            val body = json.encodeToString(sq).toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            val created = json.decodeFromString<List<SecretQuestionDto>>(resStr).firstOrNull() ?: sq
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun answerSecretQuestion(id: String, answer: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/secret_questions?id=eq.$id"
            val body = "{\"is_answered\":true,\"answer\":${json.encodeToString(answer.trim())}}".toRequestBody(jsonMediaType)
            val request = newRequestBuilder(url).patch(body).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Error al responder pregunta"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
