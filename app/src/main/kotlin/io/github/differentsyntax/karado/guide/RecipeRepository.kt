package io.github.differentsyntax.karado.guide

import android.content.Context
import io.github.differentsyntax.karado.recipes.Recipe
import io.github.differentsyntax.karado.recipes.RecipeLoader

/** Recipes bundled in the APK under assets/recipes (ADR-0005: nothing is downloaded). */
object RecipeRepository {
    @Volatile private var cache: List<Recipe>? = null

    fun all(context: Context): List<Recipe> = cache ?: load(context).also { cache = it }

    private fun load(context: Context): List<Recipe> {
        val assets = context.assets
        fun files(dir: String): List<String> {
            val names = assets.list(dir).orEmpty()
            return names.flatMap { name ->
                val path = "$dir/$name"
                if (name.endsWith(".json")) listOf(path) else files(path)
            }
        }
        return files("recipes")
            .map { path -> assets.open(path).bufferedReader().use { RecipeLoader.parse(it.readText()) } }
            .sortedBy { it.id }
    }
}
