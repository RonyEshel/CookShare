package com.cookshare.ui.search

data class RecipeCategory(val name: String, val colorHex: String, val emoji: String)

object CommunityCategories {
    val list = listOf(
        RecipeCategory("Breakfast",    "#FF8F00", "🍳"),
        RecipeCategory("Lunch",        "#43A047", "🥗"),
        RecipeCategory("Dinner",       "#E8572A", "🍽️"),
        RecipeCategory("Dessert",      "#D81B60", "🍰"),
        RecipeCategory("Healthy",      "#2E7D32", "🥦"),
        RecipeCategory("Quick Meals",  "#1565C0", "⚡"),
        RecipeCategory("Italian",      "#C62828", "🍝"),
        RecipeCategory("Asian",        "#6A1B9A", "🍜"),
        RecipeCategory("Mexican",      "#E65100", "🌮"),
        RecipeCategory("Indian",       "#F57F17", "🍛"),
        RecipeCategory("Mediterranean","#00695C", "🫒"),
        RecipeCategory("Snacks",       "#0277BD", "🍿")
    )
}

object GlobalCategories {
    val list = listOf(
        RecipeCategory("Italian",   "#C62828", "🍝"),
        RecipeCategory("American",  "#1565C0", "🍔"),
        RecipeCategory("British",   "#283593", "🫖"),
        RecipeCategory("Chinese",   "#B71C1C", "🥟"),
        RecipeCategory("French",    "#4A148C", "🥐"),
        RecipeCategory("Indian",    "#E65100", "🍛"),
        RecipeCategory("Japanese",  "#880E4F", "🍱"),
        RecipeCategory("Mexican",   "#1B5E20", "🌮"),
        RecipeCategory("Spanish",   "#F57F17", "🥘"),
        RecipeCategory("Greek",     "#0D47A1", "🫙"),
        RecipeCategory("Thai",      "#2E7D32", "🍜"),
        RecipeCategory("Moroccan",  "#BF360C", "🫕")
    )
}
