package io.github.differentsyntax.karado.recipes

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class RecipeChecksTest {

    private val recipesDir = File(System.getProperty("recipesDir"))

    @Test
    fun `every shipped recipe parses and passes the checks`() {
        val files = recipesDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" && "fixtures" !in it.path }
            .toList()
        assertTrue(files.isNotEmpty(), "no recipes found in $recipesDir")
        val failures = files.flatMap { file ->
            val recipe = try {
                RecipeLoader.parse(file.readText())
            } catch (e: Exception) {
                return@flatMap listOf("${file.name}: does not parse: ${e.message}")
            }
            val problems = RecipeChecks.problems(recipe).map { "${file.name}: $it" }
            if (file.nameWithoutExtension != recipe.id) problems + "${file.name}: file name must equal id" else problems
        }
        if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
    }

    @Test
    fun `payment and code labels are rejected`() {
        val recipe = brightness().let { r ->
            r.copy(route = r.route + r.route.last().copy(id = "pay", target = Target(text = listOf("Pay now"))))
        }
        assertTrue(RecipeChecks.problems(recipe).any { "Pay now" in it })
        val otp = brightness().copy(title = Localized("Enter the OTP", "OTP डालिए"))
        assertEquals(2, RecipeChecks.problems(otp).count { "OTP" in it })
    }

    @Test
    fun `sensitive words only match whole words`() {
        // "Display" contains "pay" and "Spinner" contains "pin"; neither may be flagged.
        val recipe = brightness().let { r ->
            r.copy(route = r.route + r.route.last().copy(id = "spin", target = Target(text = listOf("Display", "Spinner"))))
        }
        assertEquals(emptyList(), RecipeChecks.problems(recipe))
    }

    @Test
    fun `payment apps are rejected`() {
        assertTrue(RecipeChecks.problems(brightness().copy(app = "com.phonepe.app")).isNotEmpty())
    }

    @Test
    fun `brand variant labels come before the generic ones`() {
        val step = brightness().route.first { it.id == "open_display" }
        assertEquals("Display and brightness", step.targetFor("vivo").text.first())
        assertEquals(step.target, step.targetFor("nokia"))
        assertEquals(step.target, step.targetFor(null))
    }

    private fun brightness() =
        RecipeLoader.parse(File(recipesDir, "settings/settings.brightness.increase.json").readText())
}
