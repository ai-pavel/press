package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableTest {

    @Test
    fun `table DSL creates rows`() {
        val t = table {
            row(TableCell("A"), TableCell("B"))
            row(TableCell("C"), TableCell("D"))
        }
        assertEquals(2, t.rows.size)
        assertEquals("A", t.rows[0][0].text)
        assertEquals("D", t.rows[1][1].text)
    }

    @Test
    fun `table DSL with row builder`() {
        val t = table {
            row {
                cell("X", colSpan = 2)
                cell("Y")
            }
        }
        assertEquals(1, t.rows.size)
        assertEquals(2, t.rows[0][0].colSpan)
        assertEquals("Y", t.rows[0][1].text)
    }

    @Test
    fun `table header row`() {
        val t = table {
            headerRow("Name", "Age")
            row(TableCell("Alice"), TableCell("30"))
        }
        assertEquals(1, t.headerRows)
        assertEquals(2, t.rows.size)
    }

    @Test
    fun `cell spanning`() {
        val cell = TableCell("Merged", colSpan = 3, rowSpan = 2)
        assertEquals(3, cell.colSpan)
        assertEquals(2, cell.rowSpan)
    }

    @Test
    fun `border style defaults`() {
        val border = BorderStyle()
        assertEquals(0.5f, border.width)
        assertTrue(border.enabled)
        assertEquals(Color.BLACK, border.color)
    }

    @Test
    fun `table custom column widths`() {
        val t = table {
            columnWidths = listOf(100f, 200f, 150f)
            row(TableCell("A"), TableCell("B"), TableCell("C"))
        }
        assertEquals(listOf(100f, 200f, 150f), t.columnWidths)
    }

    @Test
    fun `cell background color`() {
        val cell = TableCell("Shaded", backgroundColor = Color.LIGHT_GRAY)
        assertEquals(Color.LIGHT_GRAY, cell.backgroundColor)
    }

    @Test
    fun `TableCell default values`() {
        val cell = TableCell()
        assertEquals("", cell.text)
        assertEquals(TextStyle(), cell.style)
        assertEquals(1, cell.colSpan)
        assertEquals(1, cell.rowSpan)
        assertEquals(null, cell.backgroundColor)
        assertEquals(4f, cell.padding)
    }

    @Test
    fun `TableCell custom padding`() {
        val cell = TableCell("Text", padding = 8f)
        assertEquals(8f, cell.padding)
    }

    @Test
    fun `TableBuilder custom header style`() {
        val style = textStyle { bold = true; fontSize = 16f; color = Color.WHITE }
        val t = table {
            headerStyle = style
            headerRow("H1", "H2")
            row(TableCell("D1"), TableCell("D2"))
        }
        assertEquals(style, t.headerStyle)
    }

    @Test
    fun `Table default values`() {
        val t = Table(rows = emptyList())
        assertEquals(null, t.columnWidths)
        assertEquals(BorderStyle(), t.borderStyle)
        assertEquals(0, t.headerRows)
        assertEquals(TextStyle(bold = true), t.headerStyle)
    }

    @Test
    fun `RowBuilder with multiple cells and custom properties`() {
        val t = table {
            row {
                cell("A", style = textStyle { bold = true }, colSpan = 1, rowSpan = 1, backgroundColor = Color.RED, padding = 6f)
                cell("B", style = textStyle { italic = true }, colSpan = 2, rowSpan = 3, backgroundColor = Color.BLUE, padding = 10f)
            }
        }
        val row = t.rows[0]
        assertEquals(2, row.size)
        assertEquals("A", row[0].text)
        assertTrue(row[0].style.bold)
        assertEquals(Color.RED, row[0].backgroundColor)
        assertEquals(6f, row[0].padding)
        assertEquals("B", row[1].text)
        assertTrue(row[1].style.italic)
        assertEquals(2, row[1].colSpan)
        assertEquals(3, row[1].rowSpan)
        assertEquals(Color.BLUE, row[1].backgroundColor)
        assertEquals(10f, row[1].padding)
    }

    @Test
    fun `multiple header rows`() {
        val t = table {
            headerRow("Group1", "Group2")
            headerRow("Sub1", "Sub2")
            row(TableCell("Data1"), TableCell("Data2"))
        }
        assertEquals(2, t.headerRows)
        assertEquals(3, t.rows.size)
    }

    @Test
    fun `table with no rows`() {
        val t = table {}
        assertTrue(t.rows.isEmpty())
    }

    @Test
    fun `BorderStyle custom values`() {
        val bs = BorderStyle(width = 2f, color = Color.RED, enabled = false)
        assertEquals(2f, bs.width)
        assertEquals(Color.RED, bs.color)
        assertEquals(false, bs.enabled)
    }

    @Test
    fun `TableBuilder borderStyle can be set`() {
        val t = table {
            borderStyle = BorderStyle(width = 3f, color = Color.GREEN, enabled = true)
            row(TableCell("X"))
        }
        assertEquals(3f, t.borderStyle.width)
        assertEquals(Color.GREEN, t.borderStyle.color)
        assertTrue(t.borderStyle.enabled)
    }
}
