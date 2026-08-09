package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentTest {

    @Test
    fun `document DSL creates document with metadata`() {
        val doc = document {
            title = "Test Doc"
            author = "Author"
            page {
                heading("Hello")
            }
        }
        assertEquals("Test Doc", doc.metadata.title)
        assertEquals("Author", doc.metadata.author)
        assertEquals(1, doc.pages.size)
    }

    @Test
    fun `document with multiple pages`() {
        val doc = document {
            page { heading("Page 1") }
            page { heading("Page 2") }
            page { heading("Page 3") }
        }
        assertEquals(3, doc.pages.size)
    }

    @Test
    fun `page content dimensions`() {
        val page = Page(
            size = PageSize.A4,
            margins = Margins(72f, 72f, 72f, 72f)
        )
        assertEquals(595f - 144f, page.contentWidth)
        assertEquals(842f - 144f, page.contentHeight)
    }

    @Test
    fun `page builder adds elements`() {
        val builder = PageBuilder()
        builder.heading("Title")
        builder.paragraph("Some text")
        builder.spacer(20f)
        builder.horizontalRule()
        val page = builder.build()
        assertEquals(4, page.elements.size)
        assertTrue(page.elements[0] is Heading)
        assertTrue(page.elements[1] is Paragraph)
        assertTrue(page.elements[2] is Spacer)
        assertTrue(page.elements[3] is HorizontalRule)
    }

    @Test
    fun `page sizes`() {
        assertEquals(595f, PageSize.A4.width)
        assertEquals(842f, PageSize.A4.height)
        assertEquals(612f, PageSize.LETTER.width)
        assertEquals(792f, PageSize.LETTER.height)
    }

    @Test
    fun `document default metadata`() {
        val doc = Document()
        assertEquals("", doc.metadata.title)
        assertEquals("", doc.metadata.author)
        assertEquals("", doc.metadata.subject)
        assertEquals("pdf-generator 1.0", doc.metadata.creator)
        assertTrue(doc.pages.isEmpty())
    }

    @Test
    fun `DocumentMetadata default creator`() {
        val meta = DocumentMetadata(title = "T", author = "A", subject = "S")
        assertEquals("pdf-generator 1.0", meta.creator)
    }

    @Test
    fun `DocumentBuilder addPage adds pages`() {
        val builder = DocumentBuilder()
        val page = Page(elements = listOf(Paragraph("Test")))
        builder.addPage(page)
        val doc = builder.build()
        assertEquals(1, doc.pages.size)
        assertEquals(1, doc.pages[0].elements.size)
    }

    @Test
    fun `DocumentBuilder sets custom creator`() {
        val doc = document {
            creator = "Custom Creator"
            page { paragraph("test") }
        }
        assertEquals("Custom Creator", doc.metadata.creator)
    }

    @Test
    fun `DocumentBuilder sets subject`() {
        val doc = document {
            subject = "Test Subject"
            page { paragraph("test") }
        }
        assertEquals("Test Subject", doc.metadata.subject)
    }

    @Test
    fun `document renderTo OutputStream`() {
        val doc = document {
            page { paragraph("Output stream test") }
        }
        val baos = java.io.ByteArrayOutputStream()
        doc.renderTo(baos)
        val text = String(baos.toByteArray(), Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
    }

    @Test
    fun `document renderTo File`(@org.junit.jupiter.api.io.TempDir tempDir: java.io.File) {
        val doc = document {
            page { paragraph("File output test") }
        }
        val file = java.io.File(tempDir, "output.pdf")
        doc.renderTo(file)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }

    @Test
    fun `document render returns ByteArray`() {
        val doc = document {
            page { paragraph("Byte array test") }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
        assertTrue(String(bytes, Charsets.ISO_8859_1).startsWith("%PDF-1.4"))
    }

    @Test
    fun `empty document renders valid PDF`() {
        val doc = document {}
        val bytes = doc.render()
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }
}
