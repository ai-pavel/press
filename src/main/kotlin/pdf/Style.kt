package pdf

/**
 * Represents a color with RGB components (0.0 to 1.0).
 */
data class Color(val r: Float, val g: Float, val b: Float) {
    companion object {
        val BLACK = Color(0f, 0f, 0f)
        val WHITE = Color(1f, 1f, 1f)
        val RED = Color(1f, 0f, 0f)
        val GREEN = Color(0f, 0.5f, 0f)
        val BLUE = Color(0f, 0f, 1f)
        val GRAY = Color(0.5f, 0.5f, 0.5f)
        val LIGHT_GRAY = Color(0.85f, 0.85f, 0.85f)
        val DARK_GRAY = Color(0.3f, 0.3f, 0.3f)

        fun hex(hex: String): Color {
            val h = hex.removePrefix("#")
            val r = h.substring(0, 2).toInt(16) / 255f
            val g = h.substring(2, 4).toInt(16) / 255f
            val b = h.substring(4, 6).toInt(16) / 255f
            return Color(r, g, b)
        }
    }

    fun toPdfStrokeColor(): String = "$r $g $b RG"
    fun toPdfFillColor(): String = "$r $g $b rg"
}

/**
 * Font families supported by the PDF writer (PDF standard 14 fonts).
 */
enum class FontFamily(val pdfName: String, val boldPdfName: String, val italicPdfName: String, val boldItalicPdfName: String) {
    HELVETICA("Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique"),
    TIMES("Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic"),
    COURIER("Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique");

    fun resolve(bold: Boolean, italic: Boolean): String = when {
        bold && italic -> boldItalicPdfName
        bold -> boldPdfName
        italic -> italicPdfName
        else -> pdfName
    }
}

/**
 * Text style configuration.
 */
data class TextStyle(
    val fontFamily: FontFamily = FontFamily.HELVETICA,
    val fontSize: Float = 12f,
    val color: Color = Color.BLACK,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val lineSpacing: Float = 1.4f
) {
    fun resolvedFontName(): String = fontFamily.resolve(bold, italic)

    /** Approximate width of a character in the current font (simplified metric). */
    fun charWidth(): Float = fontSize * 0.5f

    /** Approximate width of a string. */
    fun textWidth(text: String): Float = text.length * charWidth()
}

/**
 * DSL builder for TextStyle.
 */
class TextStyleBuilder {
    var fontFamily: FontFamily = FontFamily.HELVETICA
    var fontSize: Float = 12f
    var color: Color = Color.BLACK
    var bold: Boolean = false
    var italic: Boolean = false
    var lineSpacing: Float = 1.4f

    fun build(): TextStyle = TextStyle(fontFamily, fontSize, color, bold, italic, lineSpacing)
}

fun textStyle(init: TextStyleBuilder.() -> Unit): TextStyle {
    val builder = TextStyleBuilder()
    builder.init()
    return builder.build()
}
