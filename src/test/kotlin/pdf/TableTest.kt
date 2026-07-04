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
}
