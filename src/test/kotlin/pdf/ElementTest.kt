package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElementTest {

    @Test
    fun `Heading effective style sets bold and correct size for level 1`() {
        val heading = Heading("Title", level = 1)
        val style = heading.effectiveStyle()
        assertTrue(style.bold)
        assertEquals(24f, style.fontSize)
    }

    @Test
    fun `Heading effective style for level 2`() {
        val heading = Heading("Subtitle", level = 2)
        val style = heading.effectiveStyle()
        assertEquals(20f, style.fontSize)
    }

    @Test
    fun `Heading effective style for level 3`() {
        val heading = Heading("Section", level = 3)
        val style = heading.effectiveStyle()
        assertEquals(16f, style.fontSize)
    }

    @Test
    fun `Heading effective style for level 4+`() {
        val heading = Heading("Subsection", level = 4)
        val style = heading.effectiveStyle()
        assertEquals(14f, style.fontSize)
    }

    @Test
    fun `Paragraph holds text and style`() {
        val style = TextStyle(fontSize = 14f, bold = true)
        val p = Paragraph("Hello world", style)
        assertEquals("Hello world", p.text)
        assertEquals(14f, p.style.fontSize)
    }

    @Test
    fun `HorizontalRule defaults`() {
        val hr = HorizontalRule()
        assertEquals(1f, hr.thickness)
        assertEquals(Color.GRAY, hr.color)
    }

    @Test
    fun `Spacer default height`() {
        assertEquals(12f, Spacer().height)
    }

    @Test
    fun `Spacer custom height`() {
        val spacer = Spacer(50f)
        assertEquals(50f, spacer.height)
    }

    @Test
    fun `Heading effective style preserves custom color`() {
        val heading = Heading("Title", level = 1, style = TextStyle(color = Color.RED))
        val effective = heading.effectiveStyle()
        assertEquals(Color.RED, effective.color)
        assertTrue(effective.bold)
        assertEquals(24f, effective.fontSize)
    }

    @Test
    fun `Heading effective style for level 5+ defaults to 14f`() {
        val heading = Heading("Deep", level = 10)
        assertEquals(14f, heading.effectiveStyle().fontSize)
    }

    @Test
    fun `Heading default values`() {
        val heading = Heading("Test")
        assertEquals("Test", heading.text)
        assertEquals(1, heading.level)
        assertEquals(TextStyle(), heading.style)
    }

    @Test
    fun `Paragraph default style`() {
        val p = Paragraph("text")
        assertEquals(TextStyle(), p.style)
    }

    @Test
    fun `HorizontalRule custom values`() {
        val hr = HorizontalRule(3f, Color.BLUE)
        assertEquals(3f, hr.thickness)
        assertEquals(Color.BLUE, hr.color)
    }

    @Test
    fun `Heading effective style preserves fontFamily`() {
        val heading = Heading("Title", level = 2, style = TextStyle(fontFamily = FontFamily.COURIER))
        val effective = heading.effectiveStyle()
        assertEquals(FontFamily.COURIER, effective.fontFamily)
        assertEquals(20f, effective.fontSize)
        assertTrue(effective.bold)
    }

    @Test
    fun `Heading effective style preserves lineSpacing`() {
        val heading = Heading("Title", level = 3, style = TextStyle(lineSpacing = 2.0f))
        val effective = heading.effectiveStyle()
        assertEquals(2.0f, effective.lineSpacing)
    }
}
