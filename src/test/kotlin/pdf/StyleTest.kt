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

    @Test
    fun `textStyle DSL builder with italic and lineSpacing`() {
        val style = textStyle {
            italic = true
            lineSpacing = 2.0f
        }
        assertEquals(true, style.italic)
        assertEquals(2.0f, style.lineSpacing)
    }

    @Test
    fun `TextStyle default values`() {
        val style = TextStyle()
        assertEquals(FontFamily.HELVETICA, style.fontFamily)
        assertEquals(12f, style.fontSize)
        assertEquals(Color.BLACK, style.color)
        assertEquals(false, style.bold)
        assertEquals(false, style.italic)
        assertEquals(1.4f, style.lineSpacing)
    }

    @Test
    fun `TextStyle charWidth`() {
        val style = TextStyle(fontSize = 10f)
        assertEquals(5f, style.charWidth())
    }

    @Test
    fun `TextStyle charWidth with different font size`() {
        val style = TextStyle(fontSize = 20f)
        assertEquals(10f, style.charWidth())
    }

    @Test
    fun `TextStyle textWidth for empty string`() {
        val style = TextStyle(fontSize = 12f)
        assertEquals(0f, style.textWidth(""))
    }

    @Test
    fun `TextStyleBuilder default values`() {
        val builder = TextStyleBuilder()
        val style = builder.build()
        assertEquals(FontFamily.HELVETICA, style.fontFamily)
        assertEquals(12f, style.fontSize)
        assertEquals(Color.BLACK, style.color)
        assertEquals(false, style.bold)
        assertEquals(false, style.italic)
        assertEquals(1.4f, style.lineSpacing)
    }

    @Test
    fun `Color predefined constants`() {
        assertEquals(Color(0f, 0f, 0f), Color.BLACK)
        assertEquals(Color(1f, 1f, 1f), Color.WHITE)
        assertEquals(Color(1f, 0f, 0f), Color.RED)
        assertEquals(Color(0f, 0.5f, 0f), Color.GREEN)
        assertEquals(Color(0f, 0f, 1f), Color.BLUE)
        assertEquals(Color(0.5f, 0.5f, 0.5f), Color.GRAY)
        assertEquals(Color(0.85f, 0.85f, 0.85f), Color.LIGHT_GRAY)
        assertEquals(Color(0.3f, 0.3f, 0.3f), Color.DARK_GRAY)
    }

    @Test
    fun `Color hex with full white`() {
        val color = Color.hex("FFFFFF")
        assertEquals(1f, color.r)
        assertEquals(1f, color.g)
        assertEquals(1f, color.b)
    }

    @Test
    fun `Color hex with full black`() {
        val color = Color.hex("000000")
        assertEquals(0f, color.r)
        assertEquals(0f, color.g)
        assertEquals(0f, color.b)
    }

    @Test
    fun `Color toPdfFillColor for white`() {
        assertEquals("1.0 1.0 1.0 rg", Color.WHITE.toPdfFillColor())
    }

    @Test
    fun `Color toPdfStrokeColor for blue`() {
        assertEquals("0.0 0.0 1.0 RG", Color.BLUE.toPdfStrokeColor())
    }

    @Test
    fun `FontFamily TIMES resolve normal`() {
        assertEquals("Times-Roman", FontFamily.TIMES.resolve(bold = false, italic = false))
    }

    @Test
    fun `FontFamily TIMES resolve bold`() {
        assertEquals("Times-Bold", FontFamily.TIMES.resolve(bold = true, italic = false))
    }

    @Test
    fun `FontFamily COURIER resolve normal`() {
        assertEquals("Courier", FontFamily.COURIER.resolve(bold = false, italic = false))
    }

    @Test
    fun `FontFamily COURIER resolve italic`() {
        assertEquals("Courier-Oblique", FontFamily.COURIER.resolve(bold = false, italic = true))
    }

    @Test
    fun `FontFamily COURIER resolve bold`() {
        assertEquals("Courier-Bold", FontFamily.COURIER.resolve(bold = true, italic = false))
    }

    @Test
    fun `FontFamily pdfName properties`() {
        assertEquals("Helvetica", FontFamily.HELVETICA.pdfName)
        assertEquals("Helvetica-Bold", FontFamily.HELVETICA.boldPdfName)
        assertEquals("Helvetica-Oblique", FontFamily.HELVETICA.italicPdfName)
        assertEquals("Helvetica-BoldOblique", FontFamily.HELVETICA.boldItalicPdfName)
    }

    @Test
    fun `FontFamily TIMES pdfName properties`() {
        assertEquals("Times-Roman", FontFamily.TIMES.pdfName)
        assertEquals("Times-Bold", FontFamily.TIMES.boldPdfName)
        assertEquals("Times-Italic", FontFamily.TIMES.italicPdfName)
        assertEquals("Times-BoldItalic", FontFamily.TIMES.boldItalicPdfName)
    }

    @Test
    fun `FontFamily COURIER pdfName properties`() {
        assertEquals("Courier", FontFamily.COURIER.pdfName)
        assertEquals("Courier-Bold", FontFamily.COURIER.boldPdfName)
        assertEquals("Courier-Oblique", FontFamily.COURIER.italicPdfName)
        assertEquals("Courier-BoldOblique", FontFamily.COURIER.boldItalicPdfName)
    }

    @Test
    fun `TextStyle resolvedFontName for all combinations`() {
        assertEquals("Helvetica", TextStyle().resolvedFontName())
        assertEquals("Helvetica-Bold", TextStyle(bold = true).resolvedFontName())
        assertEquals("Helvetica-Oblique", TextStyle(italic = true).resolvedFontName())
        assertEquals("Helvetica-BoldOblique", TextStyle(bold = true, italic = true).resolvedFontName())
    }
}
