package io.github.differentsyntax.karado.guide

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import io.github.differentsyntax.karado.engine.Bounds
import io.github.differentsyntax.karado.recipes.Lang

/**
 * Two accessibility-overlay windows (no "draw over other apps" permission needed):
 * - a full-screen ring layer that is NOT touchable, so every tap reaches the real app;
 * - a small control bar with the spoken text and Repeat / Done / Stop buttons.
 */
internal class Overlay(private val context: Context) {
    private val wm = context.getSystemService(WindowManager::class.java)
    private val rings = RingView(context)
    private val bar = ControlBar(context)
    private var shown = false

    var onRepeat: () -> Unit = {}
    var onDone: () -> Unit = {}
    var onStop: () -> Unit = {}

    fun show(caption: String, targets: List<Bounds>, waitingForDone: Boolean, lang: Lang) {
        ensureShown()
        bar.setLang(lang)
        rings.setTargets(targets)
        bar.caption.text = caption
        bar.done.visibility = if (waitingForDone) View.VISIBLE else View.GONE
        // Keep the bar away from the thing we are pointing at.
        val targetInBottomHalf = targets.any { it.top > context.resources.displayMetrics.heightPixels / 2 }
        val lp = bar.layoutParams as WindowManager.LayoutParams
        val gravity = Gravity.CENTER_HORIZONTAL or (if (targetInBottomHalf) Gravity.TOP else Gravity.BOTTOM)
        if (lp.gravity != gravity) {
            lp.gravity = gravity
            wm.updateViewLayout(bar, lp)
        }
    }

    /** Hide the ring but keep the controls, e.g. while the person is in another app. */
    fun hideRings() = rings.setTargets(emptyList())

    fun hide() {
        if (!shown) return
        wm.removeView(rings)
        wm.removeView(bar)
        shown = false
    }

    private fun ensureShown() {
        if (shown) return
        bar.repeat.setOnClickListener { onRepeat() }
        bar.done.setOnClickListener { onDone() }
        bar.stop.setOnClickListener { onStop() }
        wm.addView(rings, params(fullScreen = true))
        wm.addView(bar, params(fullScreen = false))
        shown = true
    }

    private fun params(fullScreen: Boolean) = WindowManager.LayoutParams(
        if (fullScreen) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
        if (fullScreen) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            (if (fullScreen) WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS else 0),
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = if (fullScreen) Gravity.TOP or Gravity.START else Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        if (Build.VERSION.SDK_INT >= 28) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private class RingView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.rgb(255, 140, 0) // saffron: visible on light and dark screens
        }
        private val dp = context.resources.displayMetrics.density
        private var targets: List<Bounds> = emptyList()
        private var pulse = 0f
        private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { pulse = it.animatedValue as Float; invalidate() }
        }
        private val location = IntArray(2)

        fun setTargets(t: List<Bounds>) {
            targets = t
            if (t.isEmpty()) animator.cancel() else if (!animator.isStarted) animator.start()
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            if (targets.isEmpty()) return
            // Node bounds are in screen coordinates; this window may not start at 0,0.
            getLocationOnScreen(location)
            paint.strokeWidth = (4 + 3 * pulse) * dp
            val pad = (6 + 6 * pulse) * dp
            for (b in targets) {
                val r = RectF(
                    b.left - pad - location[0], b.top - pad - location[1],
                    b.right + pad - location[0], b.bottom + pad - location[1],
                )
                canvas.drawRoundRect(r, 16 * dp, 16 * dp, paint)
            }
        }
    }

    private class ControlBar(context: Context) : LinearLayout(context) {
        val caption = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(Color.WHITE)
        }
        val repeat = button(context)
        val done = button(context)
        val stop = button(context)

        fun setLang(lang: Lang) {
            val hi = lang == Lang.HI
            repeat.text = if (hi) "🔁 फिर से सुनिए" else "🔁 Repeat"
            done.text = if (hi) "✅ हो गया" else "✅ Done"
            stop.text = if (hi) "✖ बंद करिए" else "✖ Stop"
        }

        init {
            orientation = VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.argb(235, 20, 20, 20))
            addView(caption)
            addView(LinearLayout(context).apply {
                orientation = HORIZONTAL
                addView(repeat)
                addView(done)
                addView(stop)
            })
        }

        private fun button(context: Context) = Button(context).apply {
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
    }
}
