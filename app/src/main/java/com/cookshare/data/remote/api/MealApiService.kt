package com.cookshare.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealResponse
}

data class MealResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal") val id: String = "",
    @SerializedName("strMeal") val name: String = "",
    @SerializedName("strCategory") val category: String = "",
    @SerializedName("strArea") val area: String = "",
    @SerializedName("strInstructions") val instructions: String = "",
    @SerializedName("strMealThumb") val thumbnailUrl: String = "",
    @SerializedName("strIngredient1") val ingredient1: String? = null,
    @SerializedName("strIngredient2") val ingredient2: String? = null,
    @SerializedName("strIngredient3") val ingredient3: String? = null,
    @SerializedName("strIngredient4") val ingredient4: String? = null,
    @SerializedName("strIngredient5") val ingredient5: String? = null,
    @SerializedName("strIngredient6") val ingredient6: String? = null,
    @SerializedName("strIngredient7") val ingredient7: String? = null,
    @SerializedName("strIngredient8") val ingredient8: String? = null,
    @SerializedName("strMeasure1") val measure1: String? = null,
    @SerializedName("strMeasure2") val measure2: String? = null,
    @SerializedName("strMeasure3") val measure3: String? = null,
    @SerializedName("strMeasure4") val measure4: String? = null,
    @SerializedName("strMeasure5") val measure5: String? = null,
    @SerializedName("strMeasure6") val measure6: String? = null,
    @SerializedName("strMeasure7") val measure7: String? = null,
    @SerializedName("strMeasure8") val measure8: String? = null
) {
    fun getIngredientsList(): List<String> {
        return listOfNotNull(
            ingredient1, ingredient2, ingredient3, ingredient4,
            ingredient5, ingredient6, ingredient7, ingredient8
        ).filter { it.isNotBlank() }
    }

    fun getMeasuresList(): List<String> {
        return listOfNotNull(
            measure1, measure2, measure3, measure4,
            measure5, measure6, measure7, measure8
        ).filter { it.isNotBlank() }
    }
}