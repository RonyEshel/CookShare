package com.cookshare.data.remote.firebase

import android.net.Uri
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Registration failed")
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
            try { storage.reference.child("recipes/$recipeId.jpg").delete().await() } catch (_: Exception) {}
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRecipeImage(uri: Uri, recipeId: String): Result<String> {
        return try {
            val ref = storage.reference.child("recipes/$recipeId.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await()
            Result.success(url.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(uri: Uri, userId: String): Result<String> {
        return try {
            val ref = storage.reference.child("users/$userId.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await()
            Result.success(url.toString())
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
}