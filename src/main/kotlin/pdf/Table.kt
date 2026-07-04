package pdf

/**
 * Configuration for table borders.
 */
data class BorderStyle(
    val width: Float = 0.5f,
    val color: Color = Color.BLACK,
    val enabled: Boolean = true
)

/**
 * A single table cell, supporting column and row spanning.
 */
data class TableCell(
    val text: String = "",
    val style: TextStyle = TextStyle(),
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val backgroundColor: Color? = null,
    val padding: Float = 4f
)

/**
 * A table element with rows, columns, borders, and cell spanning.
 */
data class Table(
    val rows: List<List<TableCell>>,
    val columnWidths: List<Float>? = null,
    val borderStyle: BorderStyle = BorderStyle(),
    val headerRows: Int = 0,
    val headerStyle: TextStyle = TextStyle(bold = true)
) : Element

/**
 * DSL builder for Table.
 */
class TableBuilder {
    private val rows = mutableListOf<List<TableCell>>()
    var columnWidths: List<Float>? = null
    var borderStyle: BorderStyle = BorderStyle()
    var headerRows: Int = 0
    var headerStyle: TextStyle = TextStyle(bold = true)

    fun row(vararg cells: TableCell) {
        rows.add(cells.toList())
    }

    fun row(init: RowBuilder.() -> Unit) {
        val builder = RowBuilder()
        builder.init()
        rows.add(builder.cells)
    }

    fun headerRow(vararg texts: String) {
        headerRows = maxOf(headerRows, rows.size + 1)
        rows.add(texts.map { TableCell(it, headerStyle) })
    }

    fun build(): Table = Table(rows, columnWidths, borderStyle, headerRows, headerStyle)
}

class RowBuilder {
    internal val cells = mutableListOf<TableCell>()

    fun cell(
        text: String,
        style: TextStyle = TextStyle(),
        colSpan: Int = 1,
        rowSpan: Int = 1,
        backgroundColor: Color? = null,
        padding: Float = 4f
    ) {
        cells.add(TableCell(text, style, colSpan, rowSpan, backgroundColor, padding))
    }
}

fun table(init: TableBuilder.() -> Unit): Table {
    val builder = TableBuilder()
    builder.init()
    return builder.build()
}
