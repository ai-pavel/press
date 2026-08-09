package pdf

/**
 * Standard page sizes in points (1 point = 1/72 inch).
 */
enum class PageSize(val width: Float, val height: Float) {
    A4(595f, 842f),
    LETTER(612f, 792f),
    LEGAL(612f, 1008f),
    A3(842f, 1190f),
    A5(420f, 595f)
}

/**
 * Page margins in points.
 */
data class Margins(
    val top: Float = 72f,
    val right: Float = 72f,
    val bottom: Float = 72f,
    val left: Float = 72f
)

/**
 * A single page in a PDF document.
 */
data class Page(
    val size: PageSize = PageSize.A4,
    val margins: Margins = Margins(),
    val elements: List<Element> = emptyList()
) {
    val contentWidth: Float get() = size.width - margins.left - margins.right
    val contentHeight: Float get() = size.height - margins.top - margins.bottom
}

/**
 * DSL builder for Page.
 */
class PageBuilder {
    var size: PageSize = PageSize.A4
    var margins: Margins = Margins()
    private val elements = mutableListOf<Element>()

    fun heading(text: String, level: Int = 1, init: TextStyleBuilder.() -> Unit = {}) {
        val styleBuilder = TextStyleBuilder()
        styleBuilder.init()
        elements.add(Heading(text, level, styleBuilder.build()))
    }

    fun paragraph(text: String, init: TextStyleBuilder.() -> Unit = {}) {
        val styleBuilder = TextStyleBuilder()
        styleBuilder.init()
        elements.add(Paragraph(text, styleBuilder.build()))
    }

    fun table(init: TableBuilder.() -> Unit) {
        val builder = TableBuilder()
        builder.init()
        elements.add(builder.build())
    }

    fun image(data: ByteArray, format: ImageFormat, width: Float, height: Float, alignment: Image.Alignment = Image.Alignment.LEFT) {
        elements.add(Image.fromBytes(data, format, width, height, alignment))
    }

    fun horizontalRule(thickness: Float = 1f, color: Color = Color.GRAY) {
        elements.add(HorizontalRule(thickness, color))
    }

    fun spacer(height: Float = 12f) {
        elements.add(Spacer(height))
    }

    fun element(el: Element) {
        elements.add(el)
    }

    fun build(): Page = Page(size, margins, elements.toList())
}
