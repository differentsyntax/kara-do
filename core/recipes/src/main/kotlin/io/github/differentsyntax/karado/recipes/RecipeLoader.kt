package io.github.differentsyntax.karado.recipes

import kotlinx.serialization.json.Json

object RecipeLoader {
    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = false
    }

    fun parse(text: String): Recipe = json.decodeFromString(Recipe.serializer(), text)
}
