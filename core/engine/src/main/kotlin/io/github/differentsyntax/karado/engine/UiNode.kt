package io.github.differentsyntax.karado.engine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width get() = right - left
    val height get() = bottom - top
    val isEmpty get() = width <= 0 || height <= 0
}

/**
 * A platform-free copy of one accessibility node. The Android service converts the live
 * tree into these, so the engine can be tested with recorded fixtures and never touches
 * Android classes. Text of password fields is never copied.
 */
@Serializable
data class UiNode(
    val text: String? = null,
    @SerialName("desc") val contentDescription: String? = null,
    @SerialName("id") val viewId: String? = null,
    @SerialName("class") val className: String? = null,
    val clickable: Boolean = false,
    val checkable: Boolean = false,
    val editable: Boolean = false,
    val scrollable: Boolean = false,
    /** True when the node reports a RangeInfo (sliders, seek bars). */
    val range: Boolean = false,
    val password: Boolean = false,
    val bounds: Bounds,
    val children: List<UiNode> = emptyList(),
)

@Serializable
data class Screen(
    @SerialName("package") val packageName: String,
    val root: UiNode,
)
