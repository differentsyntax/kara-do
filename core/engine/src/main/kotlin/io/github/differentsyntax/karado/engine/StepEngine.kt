package io.github.differentsyntax.karado.engine

import io.github.differentsyntax.karado.recipes.DoneWhen
import io.github.differentsyntax.karado.recipes.Localized
import io.github.differentsyntax.karado.recipes.Recipe
import io.github.differentsyntax.karado.recipes.RecipeChecks
import io.github.differentsyntax.karado.recipes.Step
import io.github.differentsyntax.karado.recipes.targetFor

enum class Mode { ROUTE, SHORTCUT }

/** What the overlay and voice should do right now. */
sealed interface Guidance {
    /** Ring the node and say [say]. [waitingForDone] shows the "हो गया" button. */
    data class Highlight(val step: Int, val node: UiNode, val say: Localized, val waitingForDone: Boolean) : Guidance

    /** Target probably exists further down the list. */
    data class Scroll(val step: Int, val say: Localized) : Guidance

    /** Two similar buttons: ring both and ask which one the person sees. */
    data class Choose(val step: Int, val nodes: List<UiNode>, val say: Localized) : Guidance

    /** Not on any screen this recipe knows. */
    data class Lost(val step: Int, val say: Localized) : Guidance

    /** Guide stopped because the person is in an app we must never guide in. */
    data class Stopped(val say: Localized) : Guidance

    data class Finished(val say: Localized) : Guidance
}

object Messages {
    val scroll = Localized("Scroll down a little", "नीचे scroll करिए")
    val lost = Localized(
        "Looks like we are on a different screen. Tap the Back button.",
        "लगता है हम दूसरी screen पर हैं। Back button दबाइए।",
    )
    val choose = Localized("Which one do you see? Tap the right one.", "इनमें से सही वाले पर tap करिए।")
    val sensitive = Localized(
        "This app handles money or private codes, so Kara Do will stop here.",
        "यह app पैसे या private code वाला है, इसलिए Kara Do यहाँ रुक जाएगा।",
    )
    val finished = Localized("Done! You did it yourself.", "हो गया! आपने खुद कर लिया।")
}

/**
 * The state machine from docs/design/system-design.md §3.5. Feed it every new screen;
 * it answers with what to show. It never acts on the phone.
 */
class StepEngine(
    recipe: Recipe,
    val mode: Mode,
    private val brand: String?,
) {
    private val steps: List<Step> = when (mode) {
        Mode.ROUTE -> recipe.route
        Mode.SHORTCUT -> requireNotNull(recipe.shortcut) { "${recipe.id} has no shortcut" }.steps
    }
    var current = 0
        private set
    private var finished = false

    fun onScreen(screen: Screen): Guidance {
        if (finished) return Guidance.Finished(Messages.finished)
        if (screen.packageName in RecipeChecks.sensitivePackages) return Guidance.Stopped(Messages.sensitive)

        // Advance past every step whose result is already on screen (the person may be quick).
        while (current < steps.size && isDone(steps[current].doneWhen, screen)) current++
        if (current == steps.size) return finish()

        val step = steps[current]
        when (val m = Matcher.find(screen, step.targetFor(brand))) {
            is MatchResult.Found -> return highlight(current, m.node)
            is MatchResult.Ambiguous -> return Guidance.Choose(current, m.candidates, Messages.choose)
            MatchResult.NotFound -> Unit
        }

        // The person may have skipped ahead on their own: re-locate on a later step.
        for (i in current + 1 until steps.size) {
            val m = Matcher.find(screen, steps[i].targetFor(brand))
            if (m is MatchResult.Found) {
                current = i
                return highlight(i, m.node)
            }
        }

        return if (Matcher.anyScrollable(screen)) Guidance.Scroll(current, Messages.scroll) else Guidance.Lost(current, Messages.lost)
    }

    /** The person tapped "हो गया" on a step that waits for them. */
    fun confirm(): Guidance? {
        if (finished || current >= steps.size || !steps[current].doneWhen.userConfirms) return null
        current++
        return if (current == steps.size) finish() else null
    }

    private fun finish(): Guidance {
        finished = true
        return Guidance.Finished(Messages.finished)
    }

    private fun highlight(i: Int, node: UiNode) =
        Guidance.Highlight(i, node, steps[i].say, waitingForDone = steps[i].doneWhen.userConfirms)

    private fun isDone(done: DoneWhen, screen: Screen): Boolean {
        if (done.userConfirms) return false
        val pkgOk = done.packageIs?.let { it == screen.packageName } ?: true
        val visibleOk = done.anyVisible?.let { Matcher.find(screen, it, threshold = Matcher.EXACT) !is MatchResult.NotFound } ?: true
        return pkgOk && visibleOk
    }
}
