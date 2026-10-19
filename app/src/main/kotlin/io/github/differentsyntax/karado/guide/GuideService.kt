package io.github.differentsyntax.karado.guide

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import io.github.differentsyntax.karado.engine.Bounds
import io.github.differentsyntax.karado.engine.Guidance
import io.github.differentsyntax.karado.engine.Screen

/**
 * The only part of Kara Do that can see other apps. It is read-only (see
 * res/xml/accessibility_service_config.xml): it looks, rings and speaks, and never taps.
 *
 * It only receives events from [GuideController.allowedPackages], which is empty unless
 * a guide is running.
 */
class GuideService : AccessibilityService() {
    private val main = Handler(Looper.getMainLooper())
    private lateinit var overlay: Overlay
    private lateinit var speaker: Speaker
    private var lastSaid: String? = null

    private val refresh = Runnable { readScreen() }

    /**
     * Events stop arriving when the person leaves the guided app, so check once a second
     * that the window in front still belongs to an allowed app. Only the package name of
     * that window is read here.
     */
    private val watchForeground = object : Runnable {
        override fun run() {
            if (!GuideController.isGuiding) return
            val root = rootInActiveWindow
            val pkg = root?.packageName?.toString()
            root?.let(NodeSnapshot::recycle)
            if (pkg != null && pkg != packageName && pkg !in GuideController.allowedPackages) overlay.hideRings()
            main.postDelayed(this, 1000)
        }
    }

    override fun onServiceConnected() {
        overlay = Overlay(this).apply {
            onRepeat = { lastSaid?.let { speaker.say(it, GuideController.lang) } }
            onDone = { GuideController.confirm()?.let(::render) ?: readScreen() }
            onStop = { GuideController.stop() }
        }
        speaker = Speaker(this)
        GuideController.attach(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!GuideController.isGuiding) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in GuideController.allowedPackages) return
        // Screens fire many content events while they settle; wait for a quiet moment.
        main.removeCallbacks(refresh)
        main.postDelayed(refresh, 300)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (::overlay.isInitialized) endGuide()
        if (::speaker.isInitialized) speaker.shutdown()
        GuideController.detach(this)
        super.onDestroy()
    }

    /** Restrict which apps' events this service receives. */
    internal fun allowPackages(packages: Set<String>) {
        serviceInfo = serviceInfo.apply {
            // Our own package as the only entry means "nothing else".
            packageNames = (packages.ifEmpty { setOf(packageName) }).toTypedArray()
        }
        main.removeCallbacks(watchForeground)
        if (packages.isNotEmpty()) main.post(watchForeground)
    }

    internal fun endGuide() {
        main.removeCallbacks(refresh)
        main.removeCallbacks(watchForeground)
        allowPackages(emptySet())
        overlay.hide()
        speaker.stop()
        lastSaid = null
    }

    private fun readScreen() {
        val root = rootInActiveWindow ?: return
        val pkg = root.packageName?.toString()
        val guidance = try {
            if (pkg == null || pkg !in GuideController.allowedPackages) return
            GuideController.onScreen(Screen(pkg, NodeSnapshot.of(root)))
        } finally {
            NodeSnapshot.recycle(root)
        }
        guidance?.let(::render)
    }

    private fun render(g: Guidance) {
        val lang = GuideController.lang
        when (g) {
            is Guidance.Highlight -> show(g.say.get(lang), listOf(g.node.bounds), g.waitingForDone)
            is Guidance.Choose -> show(g.say.get(lang), g.nodes.map { it.bounds }, false)
            is Guidance.Scroll -> show(g.say.get(lang), emptyList(), false)
            is Guidance.Lost -> show(g.say.get(lang), emptyList(), false)
            is Guidance.Stopped -> finish(g.say.get(lang))
            is Guidance.Finished -> finish(g.say.get(lang))
        }
    }

    private fun finish(text: String) {
        GuideController.stop()
        speaker.say(text, GuideController.lang)
    }

    private fun show(text: String, rings: List<Bounds>, waitingForDone: Boolean) {
        overlay.show(text, rings, waitingForDone, GuideController.lang)
        // Speak only when the instruction changes, not on every screen refresh.
        if (text != lastSaid) {
            lastSaid = text
            speaker.say(text, GuideController.lang)
        }
    }
}
