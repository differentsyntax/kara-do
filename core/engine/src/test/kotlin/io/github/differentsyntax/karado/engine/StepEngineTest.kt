package io.github.differentsyntax.karado.engine

import io.github.differentsyntax.karado.recipes.Recipe
import io.github.differentsyntax.karado.recipes.RecipeLoader
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StepEngineTest {

    private val recipesDir = File(System.getProperty("recipesDir"))
    private val brightness: Recipe =
        RecipeLoader.parse(File(recipesDir, "settings/settings.brightness.increase.json").readText())

    private fun screen(device: String, name: String): Screen = Json.decodeFromString(
        Screen.serializer(),
        File(recipesDir, "fixtures/settings.brightness.increase/$device/$name.json").readText(),
    )

    private fun Guidance.label(): String? = (this as? Guidance.Highlight)?.node?.let { n ->
        n.text ?: n.contentDescription ?: n.children.firstNotNullOfOrNull { it.text }
    }

    @Test
    fun `pixel route goes home, settings, scroll, display, slider, done`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "google")
        val dev = "google-pixel-synthetic"

        val home = engine.onScreen(screen(dev, "01-home"))
        assertEquals("Settings", home.label())

        assertIs<Guidance.Scroll>(engine.onScreen(screen(dev, "02-settings-top")))

        val display = engine.onScreen(screen(dev, "03-settings-scrolled"))
        assertIs<Guidance.Highlight>(display)
        assertEquals("Display & touch", display.label())
        // The ring goes on the whole clickable row, not just the title text.
        assertTrue(display.node.clickable)

        val slider = engine.onScreen(screen(dev, "04-display"))
        assertIs<Guidance.Highlight>(slider)
        assertEquals("android.widget.SeekBar", slider.node.className)
        assertTrue(slider.waitingForDone)

        assertIs<Guidance.Finished>(engine.confirm())
    }

    @Test
    fun `samsung route uses the Display row, not the Brightness summary`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "samsung")
        val dev = "samsung-oneui-synthetic"
        assertEquals("Settings", engine.onScreen(screen(dev, "01-home")).label())
        assertEquals("Display", engine.onScreen(screen(dev, "02-settings")).label())
        val slider = engine.onScreen(screen(dev, "03-display"))
        assertIs<Guidance.Highlight>(slider)
        assertEquals(2, slider.step)
    }

    @Test
    fun `phone set to Hindi is matched by Hindi labels`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "xiaomi")
        val dev = "xiaomi-hindi-synthetic"
        assertEquals("सेटिंग्स", engine.onScreen(screen(dev, "01-home")).label())
        assertEquals("डिस्प्ले", engine.onScreen(screen(dev, "02-settings")).label())
        assertIs<Guidance.Highlight>(engine.onScreen(screen(dev, "03-display")))
    }

    @Test
    fun `skipping ahead is followed instead of failing`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "google")
        engine.onScreen(screen("google-pixel-synthetic", "01-home"))
        // The person found the Display page on their own.
        val g = engine.onScreen(screen("google-pixel-synthetic", "04-display"))
        assertIs<Guidance.Highlight>(g)
        assertEquals(2, engine.current)
    }

    @Test
    fun `shortcut mode starts at the slider`() {
        val engine = StepEngine(brightness, Mode.SHORTCUT, brand = "samsung")
        val g = engine.onScreen(screen("samsung-oneui-synthetic", "03-display"))
        assertIs<Guidance.Highlight>(g)
        assertTrue(g.waitingForDone)
    }

    @Test
    fun `an unrelated screen with nothing to scroll means lost`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "google")
        engine.onScreen(screen("google-pixel-synthetic", "01-home"))
        val elsewhere = Screen("com.whatsapp", UiNode(bounds = Bounds(0, 0, 1080, 2400), children = listOf(
            UiNode(text = "Chats", bounds = Bounds(0, 100, 300, 200)),
        )))
        val g = engine.onScreen(elsewhere)
        // Still in step 0 (open Settings) and no Settings icon here.
        assertIs<Guidance.Lost>(g)
    }

    @Test
    fun `payment apps stop the guide`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "google")
        val pay = Screen("com.phonepe.app", UiNode(bounds = Bounds(0, 0, 10, 10)))
        assertIs<Guidance.Stopped>(engine.onScreen(pay))
    }

    @Test
    fun `confirm only works on steps that wait for the person`() {
        val engine = StepEngine(brightness, Mode.ROUTE, brand = "google")
        engine.onScreen(screen("google-pixel-synthetic", "01-home"))
        assertNull(engine.confirm())
        assertEquals(0, engine.current)
    }
}
