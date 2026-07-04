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
}
