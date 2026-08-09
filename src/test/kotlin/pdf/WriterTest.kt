package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class WriterTest {

    @Test
    fun `generated PDF starts with PDF header`() {
        val doc = document {
            page { paragraph("Hello") }
        }
        val bytes = doc.render()
        val header = String(bytes.sliceArray(0..7), Charsets.ISO_8859_1)
        assertTrue(header.startsWith("%PDF-1.4"))
    }

    @Test
    fun `generated PDF ends with EOF`() {
        val doc = document {
            page { paragraph("Hello") }
        }
        val bytes = doc.render()
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun `generated PDF contains xref table`() {
        val doc = document {
            page { paragraph("Test") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("xref"))
        assertTrue(text.contains("startxref"))
        assertTrue(text.contains("trailer"))
    }

    @Test
    fun `generated PDF contains font resources`() {
        val doc = document {
            page {
                paragraph("Hello") {
                    fontFamily = FontFamily.TIMES
                    bold = true
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Type /Font"))
        assertTrue(text.contains("Times-Bold"))
    }

    @Test
    fun `generated PDF contains page content`() {
        val doc = document {
            page { paragraph("UniqueTestString12345") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("UniqueTestString12345"))
    }

    @Test
    fun `multi-page document has correct page count`() {
        val doc = document {
            page { paragraph("Page 1") }
            page { paragraph("Page 2") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Count 2"))
    }

    @Test
    fun `escPdf escapes parentheses`() {
        assertEquals("hello \\(world\\)", Writer.escPdf("hello (world)"))
    }

    @Test
    fun `escPdf escapes backslashes`() {
        assertEquals("path\\\\file", Writer.escPdf("path\\file"))
    }

    @Test
    fun `document with heading renders larger text`() {
        val doc = document {
            page {
                heading("Big Title", level = 1)
            }
        }
        val bytes = doc.render()
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.contains("24")) // font size 24
        assertTrue(text.contains("Big Title"))
    }

    @Test
    fun `document with table renders borders`() {
        val doc = document {
            page {
                table {
                    row(TableCell("A"), TableCell("B"))
                    row(TableCell("C"), TableCell("D"))
                }
            }
        }
        val bytes = doc.render()
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.contains("re S")) // rectangle stroke for borders
    }

    @Test
    fun `document metadata is written`() {
        val doc = document {
            title = "My Document"
            author = "Test Author"
            page { paragraph("Content") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("My Document"))
        assertTrue(text.contains("Test Author"))
    }
}
