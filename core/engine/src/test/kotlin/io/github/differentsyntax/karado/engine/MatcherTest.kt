package io.github.differentsyntax.karado.engine

import io.github.differentsyntax.karado.recipes.Role
import io.github.differentsyntax.karado.recipes.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MatcherTest {

    private fun b(i: Int) = Bounds(0, i * 100, 1000, i * 100 + 90)
    private fun screen(vararg nodes: UiNode) = Screen("x", UiNode(bounds = Bounds(0, 0, 1000, 2000), children = nodes.toList()))

    @Test
    fun `normalize treats ampersand, case and punctuation alike`() {
        assertEquals(Matcher.normalize("Display and Brightness"), Matcher.normalize("display & brightness  >"))
        assertEquals("डिस्प्ले", Matcher.normalize(" डिस्प्ले "))
    }

    @Test
    fun `password fields are never matched or looked inside`() {
        val s = screen(
            UiNode(text = "Settings", password = true, clickable = true, bounds = b(1)),
            UiNode(password = true, bounds = b(2), children = listOf(UiNode(text = "Settings", clickable = true, bounds = b(3)))),
        )
        assertEquals(MatchResult.NotFound, Matcher.find(s, Target(text = listOf("Settings"))))
    }

    @Test
    fun `two equally good buttons are reported as ambiguous`() {
        val s = screen(
            UiNode(text = "Display", clickable = true, bounds = b(1)),
            UiNode(text = "Display", clickable = true, bounds = b(2)),
        )
        assertIs<MatchResult.Ambiguous>(Matcher.find(s, Target(text = listOf("Display"), role = Role.CLICKABLE)))
    }

    @Test
    fun `view id wildcards match`() {
        val s = screen(UiNode(viewId = "com.android.settings:id/brightness_seekbar", range = true, bounds = b(1)))
        val m = Matcher.find(s, Target(viewId = listOf("*:id/brightness*"), role = Role.SLIDER))
        assertIs<MatchResult.Found>(m)
        assertEquals(Matcher.VIEW_ID, m.score)
    }

    @Test
    fun `long summary lines do not count as the label`() {
        val s = screen(UiNode(text = "Dark theme, font size, brightness", clickable = true, bounds = b(1)))
        assertEquals(MatchResult.NotFound, Matcher.find(s, Target(text = listOf("Brightness"))))
    }

    @Test
    fun `clickable role highlights the pressable row, and skips labels with no pressable parent`() {
        val row = UiNode(clickable = true, bounds = b(1), children = listOf(UiNode(text = "Display", bounds = b(1))))
        val loose = UiNode(text = "Display", bounds = b(5))
        val m = Matcher.find(screen(row, loose), Target(text = listOf("Display"), role = Role.CLICKABLE))
        assertIs<MatchResult.Found>(m)
        assertEquals(row, m.node)
    }
}
