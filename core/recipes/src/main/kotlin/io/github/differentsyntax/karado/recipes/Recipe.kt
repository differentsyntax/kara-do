package io.github.differentsyntax.karado.recipes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Lang { EN, HI }

/** Text shown and spoken to the person. Hindi follows the Hinglish style rule in docs/design. */
@Serializable
data class Localized(val en: String, val hi: String) {
    fun get(lang: Lang): String = when (lang) {
        Lang.EN -> en
        Lang.HI -> hi
    }
}

/** A task broken into steps. Steps say *what* to tap; the live screen decides *where*. */
@Serializable
data class Recipe(
    val id: String,
    val title: Localized,
    /** Package of the app this task happens in, e.g. com.android.settings. */
    val app: String,
    /** "रास्ता सिखाइए": the full route from the home screen. The default. */
    val route: List<Step>,
    /** "सीधे ले चलिए": jump close to the goal with a deep link, then finish with steps. */
    val shortcut: Shortcut? = null,
    @SerialName("tested_on") val testedOn: List<String> = emptyList(),
)

@Serializable
data class Shortcut(
    /** An Android intent action such as android.settings.DISPLAY_SETTINGS. */
    val action: String? = null,
    /** A deep link such as https://www.youtube.com/results?search_query=yoga. */
    val uri: String? = null,
    val steps: List<Step>,
)

@Serializable
data class Step(
    val id: String,
    val say: Localized,
    val target: Target,
    /** Brand-specific overrides keyed by lowercase Build.MANUFACTURER, e.g. "samsung". */
    val variants: Map<String, Target> = emptyMap(),
    /**
     * The screen this step happens on. Apps such as Settings reopen on the last page the
     * person saw, so if this isn't on screen the guide teaches Back instead of Scroll.
     */
    @SerialName("on_screen") val onScreen: Anchor? = null,
    @SerialName("done_when") val doneWhen: DoneWhen,
)

/** Labels that identify a screen. At least [atLeast] of them must be visible (exact match). */
@Serializable
data class Anchor(
    val text: List<String>,
    @SerialName("at_least") val atLeast: Int = 1,
)

enum class Role { CLICKABLE, SWITCH, SLIDER, EDITABLE }

@Serializable
data class Target(
    /** Visible label or content description in any language, matched after normalising. */
    val text: List<String> = emptyList(),
    /** Resource IDs; `*` is a wildcard, e.g. "*:id/brightness*". */
    @SerialName("view_id") val viewId: List<String> = emptyList(),
    val role: Role? = null,
)

@Serializable
data class DoneWhen(
    @SerialName("package_is") val packageIs: String? = null,
    @SerialName("any_visible") val anyVisible: Target? = null,
    /** The person tells us they're done (e.g. after moving a slider). */
    @SerialName("user_confirms") val userConfirms: Boolean = false,
)

/** The target to use on a given brand: the brand's variant labels come first, then the generic ones. */
fun Step.targetFor(brand: String?): Target {
    val variant = brand?.lowercase()?.let { variants[it] } ?: return target
    return Target(
        text = (variant.text + target.text).distinct(),
        viewId = (variant.viewId + target.viewId).distinct(),
        role = variant.role ?: target.role,
    )
}
