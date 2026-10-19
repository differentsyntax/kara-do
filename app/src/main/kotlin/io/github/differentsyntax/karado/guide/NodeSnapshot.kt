package io.github.differentsyntax.karado.guide

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import io.github.differentsyntax.karado.engine.Bounds
import io.github.differentsyntax.karado.engine.UiNode

/**
 * Copies the live accessibility tree into engine [UiNode]s. The copy lives in memory for
 * one step only and is never logged or stored. Password fields are copied without text
 * and without children.
 */
internal object NodeSnapshot {
    private const val MAX_DEPTH = 40
    private const val MAX_NODES = 3000

    fun of(root: AccessibilityNodeInfo): UiNode {
        var budget = MAX_NODES
        fun copy(node: AccessibilityNodeInfo, depth: Int): UiNode {
            budget--
            val rect = Rect().also(node::getBoundsInScreen)
            val password = node.isPassword
            val children = if (password || depth >= MAX_DEPTH) {
                emptyList()
            } else {
                (0 until node.childCount).mapNotNull { i ->
                    if (budget <= 0) return@mapNotNull null
                    val child = node.getChild(i) ?: return@mapNotNull null
                    try {
                        copy(child, depth + 1)
                    } finally {
                        recycle(child)
                    }
                }
            }
            return UiNode(
                text = if (password) null else node.text?.toString(),
                contentDescription = if (password) null else node.contentDescription?.toString(),
                viewId = node.viewIdResourceName,
                className = node.className?.toString(),
                clickable = node.isClickable,
                checkable = node.isCheckable,
                editable = node.isEditable,
                scrollable = node.isScrollable,
                range = node.rangeInfo != null,
                password = password,
                bounds = Bounds(rect.left, rect.top, rect.right, rect.bottom),
                children = children,
            )
        }
        return copy(root, 0)
    }

    fun recycle(node: AccessibilityNodeInfo) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < 33) node.recycle()
    }
}
