package com.cookshare.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.model.SavedRecipe
import com.cookshare.data.remote.firebase.FirebaseManager
import kotlinx.coroutines.launch

class SavedRecipesViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()
    private val savedRecipeDao = CookShareApp.instance.database.savedRecipeDao()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""

    val savedRecipes: LiveData<List<SavedRecipe>> get() = savedRecipeDao.getSavedByUser(currentUserId)

    fun unsave(recipeId: String) {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch { savedRecipeDao.unsave(uid, recipeId) }
    }
}
