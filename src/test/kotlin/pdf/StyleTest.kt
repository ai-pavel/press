package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StyleTest {

    @Test
    fun `Color hex parsing`() {
        val color = Color.hex("#1a5276")
        assertEquals(0x1a / 255f, color.r)
        assertEquals(0x52 / 255f, color.g)
        assertEquals(0x76 / 255f, color.b)
    }

    @Test
    fun `Color hex parsing without hash`() {
        val color = Color.hex("FF0000")
        assertEquals(1f, color.r)
        assertEquals(0f, color.g)
        assertEquals(0f, color.b)
    }

    @Test
    fun `Color to PDF fill color string`() {
        val s = Color.BLACK.toPdfFillColor()
        assertEquals("0.0 0.0 0.0 rg", s)
    }

    @Test
    fun `Color to PDF stroke color string`() {
        val s = Color.RED.toPdfStrokeColor()
        assertEquals("1.0 0.0 0.0 RG", s)
    }

    @Test
    fun `FontFamily resolve bold`() {
        assertEquals("Helvetica-Bold", FontFamily.HELVETICA.resolve(bold = true, italic = false))
    }

    @Test
    fun `FontFamily resolve italic`() {
        assertEquals("Times-Italic", FontFamily.TIMES.resolve(bold = false, italic = true))
    }

    @Test
    fun `FontFamily resolve bold italic`() {
        assertEquals("Courier-BoldOblique", FontFamily.COURIER.resolve(bold = true, italic = true))
    }

    @Test
    fun `FontFamily resolve normal`() {
        assertEquals("Helvetica", FontFamily.HELVETICA.resolve(bold = false, italic = false))
    }

    @Test
    fun `TextStyle resolved font name`() {
        val style = TextStyle(fontFamily = FontFamily.TIMES, bold = true, italic = true)
        assertEquals("Times-BoldItalic", style.resolvedFontName())
    }

    @Test
    fun `TextStyle text width approximation`() {
        val style = TextStyle(fontSize = 12f)
        val width = style.textWidth("Hello")
        assertEquals(30f, width) // 5 chars * 12 * 0.5
    }

    @Test
    fun `textStyle DSL builder`() {
        val style = textStyle {
            fontFamily = FontFamily.COURIER
            fontSize = 14f
            bold = true
            color = Color.BLUE
        }
        assertEquals(FontFamily.COURIER, style.fontFamily)
        assertEquals(14f, style.fontSize)
        assertEquals(true, style.bold)
        assertEquals(Color.BLUE, style.color)
    }
}
