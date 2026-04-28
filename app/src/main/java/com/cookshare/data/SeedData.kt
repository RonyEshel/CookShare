package com.cookshare.data

import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedData {

    private val sampleUserIds = mapOf(
        "seed_user_marco" to Triple("Chef Marco", "marco@cookshare.app", "Italian cuisine specialist"),
        "seed_user_priya" to Triple("Chef Priya", "priya@cookshare.app", "Indian spice master"),
        "seed_user_sarah" to Triple("Chef Sarah", "sarah@cookshare.app", "Breakfast & brunch lover"),
        "seed_user_emma" to Triple("Chef Emma", "emma@cookshare.app", "Fresh & healthy cooking"),
        "seed_user_carlos" to Triple("Chef Carlos", "carlos@cookshare.app", "Latin street food expert"),
        "seed_user_claire" to Triple("Chef Claire", "claire@cookshare.app", "Pastry & desserts"),
        "seed_user_amir" to Triple("Chef Amir", "amir@cookshare.app", "Middle-Eastern flavors"),
        "seed_user_nikos" to Triple("Chef Nikos", "nikos@cookshare.app", "Mediterranean diet advocate"),
        "seed_user_mia" to Triple("Chef Mia", "mia@cookshare.app", "Asian street food & desserts")
    )

    private val sampleRecipes = listOf(
        Recipe(
            title = "Classic Spaghetti Carbonara",
            description = "A rich and creamy Italian pasta dish made with eggs, cheese, pancetta, and pepper. No cream needed — the magic is all in the technique.",
            ingredients = "400g spaghetti\n200g pancetta or guanciale\n4 large eggs\n100g Pecorino Romano, grated\n100g Parmesan, grated\n2 cloves garlic\nBlack pepper to taste\nSalt for pasta water",
            instructions = "1. Cook spaghetti in heavily salted boiling water until al dente.\n2. Fry pancetta in a pan over medium heat until crispy. Add garlic, then remove garlic.\n3. Whisk eggs with grated cheese and lots of black pepper.\n4. Reserve 1 cup pasta water before draining.\n5. Off heat, toss hot pasta with pancetta and fat.\n6. Add egg mixture, tossing quickly and adding pasta water splash by splash until creamy.\n7. Serve immediately with extra cheese and pepper.",
            category = "Dinner",
            imageUrl = "https://www.themealdb.com/images/media/meals/llcbn01574260722.jpg",
            authorId = "seed_user_marco",
            authorName = "Chef Marco",
            cookingTime = 25
        ),
        Recipe(
            title = "Chicken Tikka Masala",
            description = "Tender marinated chicken in a velvety, spiced tomato-cream sauce. One of the most beloved dishes in the world for good reason.",
            ingredients = "700g chicken breast, cubed\n1 cup yogurt\n2 tsp garam masala\n1 tsp turmeric\n1 tsp cumin\n2 tsp coriander\n400ml tomato passata\n200ml heavy cream\n1 onion, diced\n4 cloves garlic\n1 tbsp ginger\n2 tbsp butter\nFresh cilantro to serve\nSalt to taste",
            instructions = "1. Marinate chicken in yogurt, half the spices, salt for at least 1 hour.\n2. Grill or pan-fry chicken until charred. Set aside.\n3. In a pan, melt butter and cook onion until golden.\n4. Add garlic and ginger, cook 2 minutes.\n5. Add remaining spices and cook 1 minute.\n6. Add passata and simmer 15 minutes.\n7. Blend sauce smooth, return to pan.\n8. Add cream and chicken, simmer 10 minutes.\n9. Garnish with cilantro and serve with naan.",
            category = "Dinner",
            imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=800&q=80",
            authorId = "seed_user_priya",
            authorName = "Chef Priya",
            cookingTime = 45
        ),
        Recipe(
            title = "Fluffy American Pancakes",
            description = "Thick, golden, cloud-like pancakes perfect for a lazy Sunday morning. Stack them high and drown in maple syrup.",
            ingredients = "1.5 cups all-purpose flour\n2 tbsp sugar\n2 tsp baking powder\n0.5 tsp baking soda\n0.5 tsp salt\n1.25 cups buttermilk\n2 eggs\n3 tbsp melted butter\n1 tsp vanilla extract\nButter for frying\nMaple syrup to serve",
            instructions = "1. Whisk together flour, sugar, baking powder, baking soda and salt.\n2. In another bowl, whisk buttermilk, eggs, melted butter and vanilla.\n3. Pour wet into dry and stir until just combined — lumps are fine, don't overmix.\n4. Rest batter 5 minutes.\n5. Heat a pan over medium-low heat and brush with butter.\n6. Pour 1/3 cup batter per pancake.\n7. Cook until bubbles form and edges look set, about 2-3 minutes.\n8. Flip and cook 1-2 more minutes.\n9. Serve stacked with maple syrup and butter.",
            category = "Breakfast",
            imageUrl = "https://www.themealdb.com/images/media/meals/rwuyqx1511383174.jpg",
            authorId = "seed_user_sarah",
            authorName = "Chef Sarah",
            cookingTime = 20
        ),
        Recipe(
            title = "Shakshuka",
            description = "Eggs poached in a bold, spiced tomato and pepper sauce. An Israeli/Middle-Eastern breakfast that works any time of day.",
            ingredients = "6 eggs\n400g crushed tomatoes\n1 red bell pepper, diced\n1 yellow onion, diced\n4 cloves garlic\n1 tsp cumin\n1 tsp paprika\n0.5 tsp chili flakes\n1 tsp sugar\n2 tbsp olive oil\nFresh parsley\nFeta cheese (optional)\nCrusty bread to serve",
            instructions = "1. Heat olive oil in a wide skillet over medium heat.\n2. Cook onion and pepper until softened, 8 minutes.\n3. Add garlic and spices, cook 1 minute.\n4. Pour in tomatoes, add sugar and salt. Simmer 10 minutes.\n5. Make 6 wells in the sauce with a spoon.\n6. Crack an egg into each well.\n7. Cover and cook 5-7 minutes until whites are set but yolks are still runny.\n8. Crumble feta on top, scatter parsley.\n9. Serve straight from the pan with crusty bread.",
            category = "Breakfast",
            imageUrl = "https://www.themealdb.com/images/media/meals/g373701551450225.jpg",
            authorId = "seed_user_amir",
            authorName = "Chef Amir",
            cookingTime = 30
        ),
        Recipe(
            title = "Chocolate Lava Cake",
            description = "Warm chocolate cake with a molten, flowing center. Sounds impressive, incredibly easy. Your guests will think you trained in Paris.",
            ingredients = "200g dark chocolate (70%)\n100g butter\n4 eggs + 4 yolks\n100g sugar\n60g flour\n1 tsp vanilla\nPinch of salt\nButter and cocoa powder for ramekins\nVanilla ice cream to serve",
            instructions = "1. Preheat oven to 220°C. Butter and dust 6 ramekins with cocoa powder.\n2. Melt chocolate and butter together. Let cool slightly.\n3. Whisk eggs, yolks and sugar until pale and thick.\n4. Fold chocolate mixture into egg mixture.\n5. Sift in flour and salt, fold until just combined.\n6. Fill ramekins 3/4 full.\n7. Bake 10-12 minutes — edges firm but center still jiggly.\n8. Rest 1 minute, then carefully invert onto plate.\n9. Serve immediately with ice cream.",
            category = "Dessert",
            imageUrl = "https://www.themealdb.com/images/media/meals/xvsurr1511719182.jpg",
            authorId = "seed_user_claire",
            authorName = "Chef Claire",
            cookingTime = 20
        ),
        Recipe(
            title = "Avocado Toast with Poached Egg",
            description = "The classic brunch staple elevated. Creamy avocado, perfectly poached egg, chili flakes — simple perfection.",
            ingredients = "2 slices sourdough bread\n1 ripe avocado\n2 eggs\n1 tbsp white vinegar\nChili flakes\nLemon juice\nSalt and black pepper\nFresh microgreens or arugula",
            instructions = "1. Toast sourdough until golden and crispy.\n2. Halve avocado, remove pit, scoop flesh into bowl.\n3. Mash with lemon juice, salt and pepper — leave chunky.\n4. Fill a deep pan with water, add vinegar, bring to gentle simmer.\n5. Crack each egg into a small cup.\n6. Create a gentle swirl in the water and slide in eggs.\n7. Poach 3 minutes for runny yolk, 4 for medium.\n8. Spread avocado generously on toast.\n9. Top with poached egg, chili flakes, and microgreens.",
            category = "Breakfast",
            imageUrl = "https://www.themealdb.com/images/media/meals/1550441882.jpg",
            authorId = "seed_user_emma",
            authorName = "Chef Emma",
            cookingTime = 15
        ),
        Recipe(
            title = "Beef Tacos al Pastor",
            description = "Marinated beef with pineapple, warm tortillas, fresh salsa and lime. Street food heaven that you can make at home.",
            ingredients = "600g beef sirloin, thinly sliced\n3 guajillo chilis, soaked\n2 chipotle peppers\n1/2 pineapple, sliced\n1 onion\n4 cloves garlic\n1 tsp cumin\n1 tsp oregano\nJuice of 2 oranges\nCorn tortillas\nCilantro, onion, lime to serve",
            instructions = "1. Blend chilis, chipotles, garlic, orange juice, cumin and oregano into a paste.\n2. Coat beef slices in marinade for at least 2 hours.\n3. Grill pineapple slices until caramelized. Dice.\n4. Cook marinated beef on a hot grill pan in batches, 2 minutes per side.\n5. Chop beef roughly.\n6. Warm tortillas directly over a gas flame or dry pan.\n7. Assemble: tortilla, beef, pineapple, diced onion, cilantro.\n8. Squeeze lime over everything.\n9. Serve with salsa verde.",
            category = "Dinner",
            imageUrl = "https://www.themealdb.com/images/media/meals/1520081754.jpg",
            authorId = "seed_user_carlos",
            authorName = "Chef Carlos",
            cookingTime = 35
        ),
        Recipe(
            title = "Greek Salad with Grilled Halloumi",
            description = "Crisp vegetables, briny olives, creamy feta — and golden grilled halloumi on top. Light, fresh and incredibly satisfying.",
            ingredients = "250g halloumi, sliced\n4 large tomatoes, chunked\n1 cucumber, chunked\n1 red onion, sliced\n100g kalamata olives\n200g feta cheese, cubed\n3 tbsp extra virgin olive oil\n1 tbsp red wine vinegar\n1 tsp dried oregano\nFresh mint leaves\nSalt and pepper",
            instructions = "1. Combine tomatoes, cucumber, red onion and olives in a large bowl.\n2. Add feta cubes.\n3. Drizzle with olive oil and vinegar.\n4. Season with oregano, salt and pepper. Toss gently.\n5. Heat a griddle pan over high heat.\n6. Brush halloumi with a little oil.\n7. Grill 2 minutes each side until golden with char marks.\n8. Place grilled halloumi on top of salad.\n9. Scatter mint leaves and serve immediately.",
            category = "Lunch",
            imageUrl = "https://www.themealdb.com/images/media/meals/urtwux1486983078.jpg",
            authorId = "seed_user_nikos",
            authorName = "Chef Nikos",
            cookingTime = 15
        ),
        Recipe(
            title = "Butter Chicken",
            description = "Tender chicken in a silky, mildly spiced tomato butter sauce. The ultimate comfort curry loved worldwide.",
            ingredients = "700g chicken thighs\n400ml tomato puree\n200ml heavy cream\n4 tbsp butter\n1 onion\n4 garlic cloves\n1 tbsp ginger\n2 tsp garam masala\n1 tsp cumin\n1 tsp paprika\n1 tsp sugar\nSalt to taste\nNaan to serve",
            instructions = "1. Season chicken with half the spices and salt.\n2. Grill or pan-fry until cooked through.\n3. Melt butter, sauté onion until golden.\n4. Add garlic and ginger, cook 2 minutes.\n5. Add remaining spices, cook 1 minute.\n6. Add tomato puree, simmer 15 minutes.\n7. Blend sauce until smooth.\n8. Add cream and butter, stir until silky.\n9. Add chicken, simmer 10 minutes.\n10. Serve with naan.",
            category = "Dinner",
            imageUrl = "https://www.themealdb.com/images/media/meals/qptpvt1487339892.jpg",
            authorId = "seed_user_priya",
            authorName = "Chef Priya",
            cookingTime = 40
        ),
        Recipe(
            title = "Mango Sticky Rice",
            description = "Thailand's most beloved dessert — sweet glutinous rice with fresh mango and rich coconut cream. Incredibly simple and incredibly good.",
            ingredients = "2 cups glutinous rice\n400ml coconut milk\n4 tbsp sugar\n1 tsp salt\n2 ripe mangoes, sliced\n1 tbsp sesame seeds (optional)",
            instructions = "1. Soak glutinous rice for 4 hours or overnight.\n2. Steam rice for 20-25 minutes until tender.\n3. Heat coconut milk with sugar and salt until sugar dissolves.\n4. Mix 2/3 of the coconut sauce into the cooked rice.\n5. Cover and let rice absorb sauce for 20 minutes.\n6. Slice mangoes.\n7. Serve rice with mango slices, drizzle remaining coconut sauce on top.\n8. Garnish with sesame seeds if using.",
            category = "Dessert",
            imageUrl = "https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=800&q=80",
            authorId = "seed_user_mia",
            authorName = "Chef Mia",
            cookingTime = 35
        ),
        Recipe(
            title = "Mushroom Risotto",
            description = "Creamy, comforting risotto packed with earthy mushrooms. The slow stir is meditative — the result is pure magic.",
            ingredients = "300g Arborio rice\n500g mixed mushrooms\n1.2L warm vegetable stock\n1 onion, finely diced\n3 garlic cloves\n150ml dry white wine\n80g Parmesan, grated\n3 tbsp butter\n2 tbsp olive oil\nFresh thyme\nSalt and pepper",
            instructions = "1. Heat oil and 1 tbsp butter in a wide pan. Sauté mushrooms until golden. Set aside.\n2. In same pan, cook onion until soft.\n3. Add garlic and rice, stir to coat, toast 2 minutes.\n4. Add wine, stir until absorbed.\n5. Add warm stock one ladle at a time, stirring constantly.\n6. Continue until rice is creamy and al dente, about 18 minutes.\n7. Stir in mushrooms, remaining butter, and Parmesan.\n8. Season, scatter thyme leaves, serve immediately.",
            category = "Dinner",
            imageUrl = "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=800&q=80",
            authorId = "seed_user_marco",
            authorName = "Chef Marco",
            cookingTime = 35
        ),
        Recipe(
            title = "Caesar Salad",
            description = "Crisp romaine, crunchy croutons, shaved Parmesan, and the most addictive dressing you'll ever make. A classic for a reason.",
            ingredients = "2 romaine hearts, torn\n100g Parmesan, shaved\nFor croutons: 3 slices bread, olive oil, garlic\nFor dressing: 2 garlic cloves, 2 anchovy fillets, 1 egg yolk, 2 tbsp lemon juice, 1 tsp Dijon, 1 tsp Worcestershire, 80ml olive oil",
            instructions = "1. Toss bread cubes in olive oil and garlic, bake at 200°C until golden.\n2. Mince garlic and anchovies into a paste.\n3. Whisk with egg yolk, lemon juice, Dijon, and Worcestershire.\n4. Slowly whisk in olive oil until emulsified.\n5. Season dressing generously.\n6. Toss romaine with dressing until well coated.\n7. Top with croutons and shaved Parmesan.\n8. Serve immediately.",
            category = "Lunch",
            imageUrl = "https://www.themealdb.com/images/media/meals/wvqpwt1468339226.jpg",
            authorId = "seed_user_emma",
            authorName = "Chef Emma",
            cookingTime = 20
        )
    )

    suspend fun seedIfNeeded(firebaseManager: FirebaseManager) = withContext(Dispatchers.IO) {
        try {
            if (!firebaseManager.isLoggedIn) {
                android.util.Log.w("SeedData", "Skipping seed — user not logged in")
                return@withContext
            }
            val existing = firebaseManager.getAllRecipes()
            val recipes = existing.getOrNull()
            if (recipes == null) {
                android.util.Log.e("SeedData", "getAllRecipes failed: ${existing.exceptionOrNull()?.message}")
                return@withContext
            }
            val seedRecipes = recipes.filter {
                it.authorId.startsWith("seed_user_") || it.authorId == "seed"
            }
            val correctSeedRecipes = seedRecipes.filter { it.authorId.startsWith("seed_user_") }
            if (correctSeedRecipes.size >= sampleRecipes.size) {
                android.util.Log.i("SeedData", "Seed already present (${correctSeedRecipes.size} recipes)")
                return@withContext
            }
            android.util.Log.i("SeedData", "Seeding ${sampleRecipes.size} recipes...")
            seedRecipes.forEach { firebaseManager.deleteRecipe(it.id) }
            val now = System.currentTimeMillis()
            sampleRecipes.forEachIndexed { index, recipe ->
                val withTimestamp = recipe.copy(timestamp = now - (index * 60_000L))
                val r = firebaseManager.addRecipe(withTimestamp)
                if (r.isFailure) android.util.Log.e("SeedData", "addRecipe failed: ${r.exceptionOrNull()?.message}")
            }
            firebaseManager.seedFakeUsers(sampleUserIds)
            android.util.Log.i("SeedData", "Seeding complete")
        } catch (e: Exception) {
            android.util.Log.e("SeedData", "Seeding error", e)
        }
    }
}
