package io.github.differentsyntax.karado.recipes

/**
 * Checks every recipe must pass before it ships (ADR-0006).
 * The safety rules keep Kara Do from ever walking someone through money, codes or permissions.
 */
object RecipeChecks {

    /** Apps a guide must never run in: payments and UPI. Banking apps are excluded the same way. */
    val sensitivePackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",
        "net.one97.paytm",
        "in.org.npci.upiapp", // BHIM
        "in.amazon.mShop.android.shopping", // also hosts Amazon Pay
    )

    /** Whole words a recipe may not target or say. Guides stop before these steps. */
    private val sensitiveWords = listOf(
        "pay", "payment", "upi", "otp", "pin", "cvv", "password", "allow", "permit",
        "भुगतान", "पेमेंट", "पासवर्ड", "अनुमति",
    )
    private val sensitiveRegex = Regex(
        sensitiveWords.joinToString("|", prefix = "(?<![\\p{L}\\p{M}])(", postfix = ")(?![\\p{L}\\p{M}])") { Regex.escape(it) },
        RegexOption.IGNORE_CASE,
    )

    fun problems(recipe: Recipe): List<String> = buildList {
        if (!Regex("[a-z0-9_]+(\\.[a-z0-9_]+)+").matches(recipe.id)) add("id '${recipe.id}' must look like area.task.detail")
        if (recipe.app in sensitivePackages) add("guides may not run in payment app ${recipe.app}")
        if (recipe.route.isEmpty()) add("route has no steps")
        checkText("title", recipe.title)
        val allSteps = recipe.route.map { "route" to it } + (recipe.shortcut?.steps.orEmpty().map { "shortcut" to it })
        if (recipe.shortcut != null && recipe.shortcut.action == null && recipe.shortcut.uri == null) {
            add("shortcut needs an action or a uri")
        }
        for ((mode, step) in allSteps) {
            val where = "$mode/${step.id}"
            checkText("$where say", step.say)
            val targets = listOf(step.target) + step.variants.values + listOfNotNull(step.doneWhen.anyVisible)
            if (step.target.text.isEmpty() && step.target.viewId.isEmpty() && step.target.role == null) {
                add("$where target matches nothing")
            }
            for (t in targets) for (label in t.text) {
                sensitiveRegex.find(label)?.let { add("$where targets sensitive label '$label'") }
            }
            step.onScreen?.let { a ->
                if (a.text.isEmpty() || a.atLeast !in 1..a.text.size) add("$where on_screen needs 1..${a.text.size} of its labels")
            }
            if (step.doneWhen.packageIs == null && step.doneWhen.anyVisible == null && !step.doneWhen.userConfirms) {
                add("$where has no done_when condition")
            }
        }
        for (dup in allSteps.groupBy { it.first + "/" + it.second.id }.filterValues { it.size > 1 }.keys) {
            add("duplicate step id $dup")
        }
    }

    private fun MutableList<String>.checkText(where: String, text: Localized) {
        if (text.en.isBlank()) add("$where: English text missing")
        if (text.hi.isBlank()) add("$where: Hindi text missing")
        sensitiveRegex.find(text.en)?.let { add("$where: mentions sensitive word '${it.value}'") }
        sensitiveRegex.find(text.hi)?.let { add("$where: mentions sensitive word '${it.value}'") }
    }
}
