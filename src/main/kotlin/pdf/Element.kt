package pdf

/**
 * Base sealed interface for all PDF document elements.
 */
sealed interface Element

/**
 * A paragraph of text with a style.
 */
data class Paragraph(
    val text: String,
    val style: TextStyle = TextStyle()
) : Element

/**
 * A heading element (rendered as bold, larger text).
 */
data class Heading(
    val text: String,
    val level: Int = 1,
    val style: TextStyle = TextStyle()
) : Element {
    fun effectiveStyle(): TextStyle {
        val size = when (level) {
            1 -> 24f
            2 -> 20f
            3 -> 16f
            else -> 14f
        }
        return style.copy(fontSize = size, bold = true)
    }
}

/**
 * A horizontal rule / separator.
 */
data class HorizontalRule(
    val thickness: Float = 1f,
    val color: Color = Color.GRAY
) : Element

/**
 * Vertical spacing element.
 */
data class Spacer(val height: Float = 12f) : Element
