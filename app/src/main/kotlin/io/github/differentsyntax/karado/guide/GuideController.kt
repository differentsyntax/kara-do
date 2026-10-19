package io.github.differentsyntax.karado.guide

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.github.differentsyntax.karado.engine.Guidance
import io.github.differentsyntax.karado.engine.Mode
import io.github.differentsyntax.karado.engine.Screen
import io.github.differentsyntax.karado.engine.StepEngine
import io.github.differentsyntax.karado.recipes.Lang
import io.github.differentsyntax.karado.recipes.Recipe

/** One running guide at a time, shared by the screen UI and [GuideService]. */
object GuideController {
    private var service: GuideService? = null
    private var engine: StepEngine? = null

    @Volatile var lang: Lang = Lang.HI

    /** Apps whose screens the service may see right now. Empty when no guide runs. */
    var allowedPackages: Set<String> = emptySet()
        private set

    val isServiceRunning get() = service != null
    val isGuiding get() = engine != null

    internal fun attach(s: GuideService) {
        service = s
    }

    internal fun detach(s: GuideService) {
        if (service === s) {
            service = null
            engine = null
            allowedPackages = emptySet()
        }
    }

    /** Returns false if the accessibility service is not turned on. */
    fun start(context: Context, recipe: Recipe, mode: Mode): Boolean {
        val s = service ?: return false
        val launcher = homePackage(context)
        engine = StepEngine(recipe, mode, brand = Build.MANUFACTURER?.lowercase())
        allowedPackages = setOfNotNull(recipe.app, launcher.takeIf { mode == Mode.ROUTE })
        s.allowPackages(allowedPackages)

        val intent = when (mode) {
            Mode.ROUTE -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            Mode.SHORTCUT -> recipe.shortcut!!.let { sc ->
                sc.action?.let { Intent(it) } ?: Intent(Intent.ACTION_VIEW, Uri.parse(sc.uri))
            }
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    fun stop() {
        engine = null
        allowedPackages = emptySet()
        service?.endGuide()
    }

    internal fun onScreen(screen: Screen): Guidance? = engine?.onScreen(screen)

    internal fun confirm(): Guidance? = engine?.confirm()

    private fun homePackage(context: Context): String? {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager.resolveActivity(home, 0)?.activityInfo?.packageName
    }
}
