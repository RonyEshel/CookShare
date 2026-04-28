package com.cookshare.data.remote.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("cookshare-db")

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Registration failed")
            // Set display name on Firebase Auth profile so displayName is available everywhere
            val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
            user.updateProfile(profileUpdate).await()
            val userData = User(uid = user.uid, displayName = displayName, email = email)
            // Fire-and-forget: don't block registration on Firestore write
            firestore.collection("users").document(user.uid).set(userData)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() { auth.signOut() }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRecipes(): Result<List<Recipe>> {
        return try {
            val snapshot = firestore.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Recipe::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipesByUser(userId: String): Result<List<Recipe>> {
        return try {
            val snapshot = firestore.collection("recipes")
                .whereEqualTo("authorId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Recipe::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRecipe(recipe: Recipe): Result<String> {
        return try {
            val docRef = firestore.collection("recipes").document()
            val recipeWithId = recipe.copy(id = docRef.id)
            docRef.set(recipeWithId).await()
            docRef.update("serverCreatedAt", FieldValue.serverTimestamp()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            firestore.collection("recipes").document(recipe.id).set(recipe).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            firestore.collection("recipes").document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun uploadRecipeImage(context: android.content.Context, uri: Uri): Result<String> {
        return uploadProfileImage(context, uri)
    }

    fun uploadProfileImage(context: android.content.Context, uri: Uri): Result<String> {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open image")
            val original = BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) throw Exception("Cannot decode image")
            val maxSize = 400
            val scale = minOf(maxSize.toFloat() / original.width, maxSize.toFloat() / original.height, 1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
            } else original
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            Result.success(base64)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun conversationId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    suspend fun getOrCreateConversation(
        myId: String, myName: String,
        otherId: String, otherName: String
    ): Result<String> {
        return try {
            val convId = conversationId(myId, otherId)
            val ref = firestore.collection("conversations").document(convId)
            val snapshot = ref.get().await()
            if (!snapshot.exists()) {
                val data = hashMapOf(
                    "id" to convId,
                    "participants" to listOf(myId, otherId),
                    "participantNames" to mapOf(myId to myName, otherId to otherName),
                    "lastMessage" to "",
                    "lastMessageTime" to 0L
                )
                ref.set(data).await()
            }
            Result.success(convId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToConversations(userId: String, onUpdate: (List<com.cookshare.data.model.Conversation>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return firestore.collection("conversations")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            @Suppress("UNCHECKED_CAST")
                            val participants = doc.get("participants") as? List<String> ?: emptyList()
                            @Suppress("UNCHECKED_CAST")
                            val names = doc.get("participantNames") as? Map<String, String> ?: emptyMap()
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val lastTime = doc.getLong("lastMessageTime") ?: 0L
                            com.cookshare.data.model.Conversation(id, participants, names, lastMsg, lastTime)
                        } catch (_: Exception) { null }
                    }.sortedByDescending { it.lastMessageTime }
                    onUpdate(conversations)
                }
            }
    }

    fun listenToMessages(conversationId: String, onUpdate: (List<com.cookshare.data.model.Message>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return firestore.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            com.cookshare.data.model.Message(
                                id = doc.id,
                                conversationId = conversationId,
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        } catch (_: Exception) { null }
                    }
                    onUpdate(messages)
                }
            }
    }

    suspend fun followUser(targetUid: String): Result<Unit> {
        return try {
            val myId = currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            firestore.collection("users").document(myId)
                .collection("following").document(targetUid)
                .set(mapOf("followedAt" to System.currentTimeMillis())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUid: String): Result<Unit> {
        return try {
            val myId = currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            firestore.collection("users").document(myId)
                .collection("following").document(targetUid)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFollowing(targetUid: String): Boolean {
        return try {
            val myId = currentUser?.uid ?: return false
            firestore.collection("users").document(myId)
                .collection("following").document(targetUid)
                .get().await().exists()
        } catch (_: Exception) { false }
    }

    suspend fun getFollowingIds(): List<String> {
        return try {
            val myId = currentUser?.uid ?: return emptyList()
            firestore.collection("users").document(myId)
                .collection("following").get().await()
                .documents.map { it.id }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun seedFakeUsers(users: Map<String, Triple<String, String, String>>) {
        try {
            users.forEach { (uid, info) ->
                val docRef = firestore.collection("users").document(uid)
                val snapshot = docRef.get().await()
                if (!snapshot.exists()) {
                    val data = mapOf(
                        "uid" to uid,
                        "displayName" to info.first,
                        "email" to info.second,
                        "profileImageUrl" to "https://i.pravatar.cc/200?u=$uid",
                        "lastUpdated" to System.currentTimeMillis()
                    )
                    docRef.set(data).await()
                }
            }
        } catch (_: Exception) {}
    }

    fun listenToComments(recipeId: String, onUpdate: (List<com.cookshare.data.model.Comment>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return firestore.collection("recipes").document(recipeId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            com.cookshare.data.model.Comment(
                                id = doc.id,
                                recipeId = recipeId,
                                authorId = doc.getString("authorId") ?: "",
                                authorName = doc.getString("authorName") ?: "",
                                authorImage = doc.getString("authorImage") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        } catch (_: Exception) { null }
                    }
                    onUpdate(comments)
                }
            }
    }

    suspend fun addComment(comment: com.cookshare.data.model.Comment): Result<Unit> {
        return try {
            val ref = firestore.collection("recipes").document(comment.recipeId)
                .collection("comments").document()
            val data = mapOf(
                "id" to ref.id,
                "recipeId" to comment.recipeId,
                "authorId" to comment.authorId,
                "authorName" to comment.authorName,
                "authorImage" to comment.authorImage,
                "text" to comment.text,
                "timestamp" to comment.timestamp
            )
            ref.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(conversationId: String, message: com.cookshare.data.model.Message): Result<Unit> {
        return try {
            val msgRef = firestore.collection("conversations").document(conversationId)
                .collection("messages").document()
            val msgWithId = message.copy(id = msgRef.id)
            msgRef.set(mapOf(
                "id" to msgWithId.id,
                "senderId" to msgWithId.senderId,
                "senderName" to msgWithId.senderName,
                "text" to msgWithId.text,
                "timestamp" to msgWithId.timestamp,
                "serverTimestamp" to FieldValue.serverTimestamp()
            )).await()
            firestore.collection("conversations").document(conversationId)
                .update("lastMessage", message.text, "lastMessageTime", message.timestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}