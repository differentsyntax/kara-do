package io.github.differentsyntax.karado.engine

import io.github.differentsyntax.karado.recipes.Role
import io.github.differentsyntax.karado.recipes.Target
import java.text.Normalizer

/** Where a step's target is on the current screen, if anywhere. */
sealed interface MatchResult {
    data class Found(val node: UiNode, val score: Int) : MatchResult

    /** Two different nodes scored almost the same: ask the person instead of guessing. */
    data class Ambiguous(val candidates: List<UiNode>) : MatchResult

    data object NotFound : MatchResult
}

/**
 * Layer 1 of ADR-0004: deterministic matching of a recipe target against the screen.
 * No AI. Scores are deliberately simple so a recipe author can predict them.
 */
object Matcher {
    const val EXACT = 100
    const val VIEW_ID = 95
    const val PREFIX = 70
    const val THRESHOLD = 70
    private const val AMBIGUITY_MARGIN = 5

    fun find(screen: Screen, target: Target, threshold: Int = THRESHOLD): MatchResult {
        val scored = mutableListOf<Pair<UiNode, Int>>()
        walk(screen.root, ancestors = emptyList()) { node, ancestors ->
            if (node.password || node.bounds.isEmpty) return@walk
            val score = score(node, target, ancestors)
            if (score >= threshold) {
                // Highlight the thing the person can actually press: labels usually sit
                // inside a clickable row.
                val pressable = if (target.role == Role.CLICKABLE) {
                    (listOf(node) + ancestors.asReversed()).firstOrNull { it.clickable } ?: return@walk
                } else {
                    node
                }
                scored += pressable to score
            }
        }
        val best = scored.groupBy({ it.first }, { it.second })
            .map { (node, scores) -> node to scores.max() }
            .sortedByDescending { it.second }
        if (best.isEmpty()) return MatchResult.NotFound
        val (top, topScore) = best[0]
        val close = best.drop(1).filter { topScore - it.second < AMBIGUITY_MARGIN }.map { it.first }
        return if (close.isEmpty()) MatchResult.Found(top, topScore) else MatchResult.Ambiguous(listOf(top) + close)
    }

    fun anyScrollable(screen: Screen): Boolean {
        var found = false
        walk(screen.root, emptyList()) { node, _ -> if (node.scrollable) found = true }
        return found
    }

    private fun score(node: UiNode, target: Target, ancestors: List<UiNode>): Int {
        if (!roleMatches(node, target.role)) return 0
        val idScore = if (node.viewId != null && target.viewId.any { glob(it).matches(node.viewId) }) VIEW_ID else 0
        val labels = when (target.role) {
            // A slider rarely has its own label; the label is a sibling inside the same row.
            Role.SLIDER, Role.SWITCH -> listOfNotNull(node.text, node.contentDescription) +
                ancestors.lastOrNull()?.let { labelsIn(it) }.orEmpty()
            else -> listOfNotNull(node.text, node.contentDescription)
        }
        val textScore = labels.maxOfOrNull { label -> target.text.maxOfOrNull { textScore(label, it) } ?: 0 } ?: 0
        val roleOnly = if (target.text.isEmpty() && target.viewId.isEmpty() && target.role != null) THRESHOLD else 0
        return maxOf(idScore, textScore, roleOnly)
    }

    private fun roleMatches(node: UiNode, role: Role?): Boolean = when (role) {
        null, Role.CLICKABLE -> true // clickability is resolved through ancestors
        Role.SLIDER -> node.range || node.className.orEmpty().let { "SeekBar" in it || "Slider" in it }
        Role.SWITCH -> node.checkable
        Role.EDITABLE -> node.editable
    }

    private fun textScore(label: String, wanted: String): Int {
        val a = normalize(label)
        val b = normalize(wanted)
        if (a.isEmpty() || b.isEmpty()) return 0
        if (a == b) return EXACT
        // "Display" should still find "Display & brightness >", but not "Brightness" inside a
        // long summary line like "Dark theme, font size, brightness".
        if (a.startsWith(b) && b.length * 2 >= a.length) return PREFIX
        return 0
    }

    /** Lowercase, unify "&"/"and", drop punctuation and arrows, collapse spaces. Keeps Devanagari. */
    fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .lowercase()
            .replace("&", " and ")
            .replace(Regex("[^\\p{L}\\p{M}\\p{N} ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun labelsIn(node: UiNode): List<String> {
        val out = mutableListOf<String>()
        walk(node, emptyList()) { n, _ -> if (!n.password) listOfNotNull(n.text, n.contentDescription).forEach(out::add) }
        return out
    }

    private fun glob(pattern: String) =
        Regex(pattern.split("*").joinToString(".*") { Regex.escape(it) })

    private fun walk(node: UiNode, ancestors: List<UiNode>, visit: (UiNode, List<UiNode>) -> Unit) {
        visit(node, ancestors)
        // Never look inside a password field.
        if (node.password) return
        val path = ancestors + node
        node.children.forEach { walk(it, path, visit) }
    }
}
