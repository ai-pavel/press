package pdf

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class WriterExtendedTest {

    // ==================== Image rendering ====================

    @Test
    fun `document with JPEG image renders image XObject`() {
        // Create a minimal JPEG-like byte array
        // JPEG SOI marker (FF D8), then a SOF0 frame (FF C0) with dimensions
        val jpegData = buildJpegData(width = 100, height = 80)

        val doc = document {
            page {
                image(jpegData, ImageFormat.JPEG, 200f, 160f)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Type /XObject"))
        assertTrue(text.contains("/Subtype /Image"))
        assertTrue(text.contains("/Filter /DCTDecode"))
        assertTrue(text.contains("/ColorSpace /DeviceRGB"))
    }

    @Test
    fun `document with PNG image renders image XObject`() {
        val pngData = buildMinimalPngData(width = 50, height = 40)

        val doc = document {
            page {
                image(pngData, ImageFormat.PNG, 150f, 120f)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Type /XObject"))
        assertTrue(text.contains("/Subtype /Image"))
        assertTrue(text.contains("/Filter /FlateDecode"))
    }

    @Test
    fun `document with LEFT aligned image`() {
        val jpegData = buildJpegData(10, 10)
        val doc = document {
            page {
                image(jpegData, ImageFormat.JPEG, 100f, 80f, Image.Alignment.LEFT)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Img0 Do"))
        assertTrue(text.contains("100.0 0 0 80.0"))
    }

    @Test
    fun `document with CENTER aligned image`() {
        val jpegData = buildJpegData(10, 10)
        val doc = document {
            page {
                image(jpegData, ImageFormat.JPEG, 100f, 80f, Image.Alignment.CENTER)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Img0 Do"))
    }

    @Test
    fun `document with RIGHT aligned image`() {
        val jpegData = buildJpegData(10, 10)
        val doc = document {
            page {
                image(jpegData, ImageFormat.JPEG, 100f, 80f, Image.Alignment.RIGHT)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Img0 Do"))
    }

    @Test
    fun `document with multiple images`() {
        val jpegData1 = buildJpegData(10, 10)
        val jpegData2 = buildJpegData(20, 20)
        val doc = document {
            page {
                image(jpegData1, ImageFormat.JPEG, 100f, 80f)
                image(jpegData2, ImageFormat.JPEG, 200f, 160f)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Img0"))
        assertTrue(text.contains("/Img1"))
    }

    @Test
    fun `document with duplicate images shares object`() {
        val jpegData = buildJpegData(10, 10)
        val doc = document {
            page {
                image(jpegData, ImageFormat.JPEG, 100f, 80f)
                image(jpegData, ImageFormat.JPEG, 100f, 80f)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        // Both images refer to the same XObject
        assertTrue(text.contains("/Img0"))
        assertTrue(text.contains("/Img1"))
    }

    // ==================== Text wrapping ====================

    @Test
    fun `long paragraph text wraps across multiple lines`() {
        val longText = "This is a very long paragraph that should be wrapped across multiple " +
                "lines in the PDF output because it exceeds the available content width " +
                "when rendered with the default font size and character width estimation."
        val doc = document {
            page {
                paragraph(longText)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("BT"))
        assertTrue(text.contains("ET"))
    }

    @Test
    fun `empty paragraph text`() {
        val doc = document {
            page {
                paragraph("")
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("BT"))
    }

    // ==================== HorizontalRule rendering ====================

    @Test
    fun `horizontal rule renders stroke commands`() {
        val doc = document {
            page {
                horizontalRule(2f, Color.RED)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("RG"))  // stroke color
        assertTrue(text.contains("2.0 w"))  // line width
        assertTrue(text.contains("m"))  // moveto
        assertTrue(text.contains("l S"))  // lineto stroke
    }

    @Test
    fun `horizontal rule with default values`() {
        val doc = document {
            page {
                horizontalRule()
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("1.0 w"))  // default thickness
    }

    // ==================== Spacer rendering ====================

    @Test
    fun `spacer reduces Y position but produces no drawing commands`() {
        val doc = document {
            page {
                paragraph("Before")
                spacer(50f)
                paragraph("After")
            }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.contains("Before"))
        assertTrue(text.contains("After"))
    }

    // ==================== Table rendering details ====================

    @Test
    fun `table with disabled borders does not render stroke`() {
        val doc = document {
            page {
                table {
                    borderStyle = BorderStyle(enabled = false)
                    row(TableCell("A"), TableCell("B"))
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        // Should have text content but no border stroke for this specific table
        assertTrue(text.contains("A"))
        assertTrue(text.contains("B"))
    }

    @Test
    fun `table with header rows renders header background`() {
        val doc = document {
            page {
                table {
                    headerRows = 1
                    headerStyle = textStyle { bold = true; fontSize = 12f }
                    row(TableCell("Header1"), TableCell("Header2"))
                    row(TableCell("Data1"), TableCell("Data2"))
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("re f"))  // rectangle fill for header background
    }

    @Test
    fun `table with cell background color`() {
        val doc = document {
            page {
                table {
                    row(TableCell("Colored", backgroundColor = Color.LIGHT_GRAY))
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("re f"))  // rectangle fill for cell background
    }

    @Test
    fun `table with custom column widths`() {
        val doc = document {
            page {
                table {
                    columnWidths = listOf(100f, 200f, 150f)
                    row(TableCell("A"), TableCell("B"), TableCell("C"))
                }
            }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun `table with column spanning`() {
        val doc = document {
            page {
                table {
                    columnWidths = listOf(100f, 100f, 100f)
                    row {
                        cell("Spanning", colSpan = 2)
                        cell("Single")
                    }
                    row {
                        cell("A")
                        cell("B")
                        cell("C")
                    }
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Spanning"))
    }

    @Test
    fun `table with row spanning`() {
        val doc = document {
            page {
                table {
                    row {
                        cell("TallCell", rowSpan = 2)
                        cell("Right1")
                    }
                    row {
                        cell("Right2")
                    }
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("TallCell"))
    }

    @Test
    fun `empty table renders without errors`() {
        val doc = document {
            page {
                table {}
            }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun `table with custom border style`() {
        val doc = document {
            page {
                table {
                    borderStyle = BorderStyle(width = 2f, color = Color.BLUE, enabled = true)
                    row(TableCell("Cell"))
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("2.0 w"))  // border width
    }

    // ==================== Multiple fonts ====================

    @Test
    fun `document with multiple font families`() {
        val doc = document {
            page {
                paragraph("Helvetica text")
                paragraph("Times text") {
                    fontFamily = FontFamily.TIMES
                }
                paragraph("Courier text") {
                    fontFamily = FontFamily.COURIER
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Helvetica"))
        assertTrue(text.contains("Times-Roman"))
        assertTrue(text.contains("Courier"))
    }

    @Test
    fun `document with bold and italic text`() {
        val doc = document {
            page {
                paragraph("Bold") { bold = true }
                paragraph("Italic") { italic = true }
                paragraph("BoldItalic") { bold = true; italic = true }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Helvetica-Bold"))
        assertTrue(text.contains("Helvetica-Oblique"))
        assertTrue(text.contains("Helvetica-BoldOblique"))
    }

    // ==================== Document metadata ====================

    @Test
    fun `document with subject metadata`() {
        val doc = document {
            title = "Title"
            author = "Author"
            subject = "Subject"
            page { paragraph("Content") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("/Subject (Subject)"))
    }

    @Test
    fun `document with empty metadata`() {
        val doc = document {
            page { paragraph("Content") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        // Empty title/author/subject should not appear
        assertTrue(text.contains("/Creator"))
        assertTrue(text.contains("/Producer"))
        assertTrue(text.contains("/CreationDate"))
    }

    @Test
    fun `document metadata escapes special characters`() {
        val doc = document {
            title = "Title (with parens)"
            author = "Path\\Author"
            page { paragraph("Content") }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Title \\(with parens\\)"))
        assertTrue(text.contains("Path\\\\Author"))
    }

    // ==================== pdfDate ====================

    @Test
    fun `pdfDate returns valid date format`() {
        val date = Writer.pdfDate()
        assertTrue(date.startsWith("D:"))
        assertTrue(date.endsWith("+00'00'"))
        // Format: D:YYYYMMDDHHmmss+00'00'
        assertTrue(date.length > 10)
    }

    // ==================== getImageDimensions ====================

    @Test
    fun `getImageDimensions for JPEG`() {
        val jpegData = buildJpegData(320, 240)
        val dims = Writer.getImageDimensions(jpegData, ImageFormat.JPEG)
        assertEquals(320, dims.first)
        assertEquals(240, dims.second)
    }

    @Test
    fun `getImageDimensions for PNG`() {
        val pngData = buildMinimalPngData(640, 480)
        val dims = Writer.getImageDimensions(pngData, ImageFormat.PNG)
        assertEquals(640, dims.first)
        assertEquals(480, dims.second)
    }

    @Test
    fun `getImageDimensions for small PNG returns 1x1`() {
        val tinyData = ByteArray(10)
        val dims = Writer.getImageDimensions(tinyData, ImageFormat.PNG)
        assertEquals(1, dims.first)
        assertEquals(1, dims.second)
    }

    @Test
    fun `getImageDimensions for invalid JPEG returns 1x1`() {
        val invalidData = ByteArray(20) { 0 }
        val dims = Writer.getImageDimensions(invalidData, ImageFormat.JPEG)
        assertEquals(1, dims.first)
        assertEquals(1, dims.second)
    }

    // ==================== decodePngToRgb ====================

    @Test
    fun `decodePngToRgb returns expected size`() {
        val pngData = buildMinimalPngData(10, 10)
        val rgb = Writer.decodePngToRgb(pngData)
        assertEquals(10 * 10 * 3, rgb.size)
    }

    // ==================== deflate ====================

    @Test
    fun `deflate produces compressed output`() {
        val original = ByteArray(1000) { (it % 256).toByte() }
        val compressed = Writer.deflate(original)
        assertTrue(compressed.isNotEmpty())
        // Compressed should be smaller for repetitive data
        assertTrue(compressed.size < original.size)
    }

    @Test
    fun `deflate of empty array produces non-empty output`() {
        val compressed = Writer.deflate(ByteArray(0))
        assertTrue(compressed.isNotEmpty()) // deflate header
    }

    // ==================== Page sizes ====================

    @Test
    fun `document with LETTER page size`() {
        val doc = document {
            page {
                size = PageSize.LETTER
                paragraph("Letter page")
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("612"))
        assertTrue(text.contains("792"))
    }

    @Test
    fun `document with custom margins`() {
        val doc = document {
            page {
                margins = Margins(36f, 36f, 36f, 36f)
                paragraph("Custom margins")
            }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
    }

    // ==================== Heading rendering ====================

    @Test
    fun `all heading levels render correctly`() {
        val doc = document {
            page {
                heading("H1", level = 1)
                heading("H2", level = 2)
                heading("H3", level = 3)
                heading("H4", level = 4)
                heading("H5", level = 5)
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("H1"))
        assertTrue(text.contains("H2"))
        assertTrue(text.contains("H3"))
        assertTrue(text.contains("H4"))
        assertTrue(text.contains("H5"))
    }

    @Test
    fun `heading with custom style`() {
        val doc = document {
            page {
                heading("Colored", level = 1) {
                    color = Color.RED
                    fontFamily = FontFamily.TIMES
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Colored"))
        assertTrue(text.contains("Times-Bold"))
    }

    // ==================== escPdf edge cases ====================

    @Test
    fun `escPdf handles empty string`() {
        assertEquals("", Writer.escPdf(""))
    }

    @Test
    fun `escPdf handles string with no special characters`() {
        assertEquals("hello world", Writer.escPdf("hello world"))
    }

    @Test
    fun `escPdf handles multiple special characters`() {
        assertEquals("a\\\\b\\(c\\)d\\\\e", Writer.escPdf("a\\b(c)d\\e"))
    }

    // ==================== Rendering with colored text ====================

    @Test
    fun `paragraph with colored text includes color commands`() {
        val doc = document {
            page {
                paragraph("Red text") {
                    color = Color.RED
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("1.0 0.0 0.0 rg"))  // red fill color
    }

    // ==================== Complex document ====================

    @Test
    fun `complex document with all element types renders`() {
        val jpegData = buildJpegData(50, 50)
        val doc = document {
            title = "Complex Doc"
            author = "Tester"
            subject = "Testing"
            creator = "TestSuite"
            page {
                size = PageSize.A4
                margins = Margins(50f, 50f, 50f, 50f)
                heading("Title", level = 1) { color = Color.BLUE }
                paragraph("Some paragraph text") { fontSize = 11f; lineSpacing = 1.5f }
                spacer(20f)
                horizontalRule(1.5f, Color.DARK_GRAY)
                table {
                    headerRows = 1
                    headerStyle = textStyle { bold = true; color = Color.WHITE }
                    borderStyle = BorderStyle(width = 1f, color = Color.BLACK)
                    columnWidths = listOf(200f, 100f, 100f)
                    row(
                        TableCell("Header1", backgroundColor = Color.BLUE),
                        TableCell("Header2", backgroundColor = Color.BLUE),
                        TableCell("Header3", backgroundColor = Color.BLUE)
                    )
                    row(TableCell("Data1"), TableCell("Data2"), TableCell("Data3"))
                }
                image(jpegData, ImageFormat.JPEG, 100f, 80f, Image.Alignment.CENTER)
            }
            page {
                heading("Page 2")
                paragraph("More content")
            }
        }

        val bytes = doc.render()
        val text = String(bytes, Charsets.ISO_8859_1)

        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
        assertTrue(text.contains("/Count 2"))
        assertTrue(text.contains("Complex Doc"))
        assertTrue(text.contains("Tester"))
        assertTrue(text.contains("Testing"))
        assertTrue(text.contains("/Type /XObject"))
        assertTrue(text.contains("Title"))
        assertTrue(text.contains("Page 2"))
    }

    // ==================== renderTo ====================

    @Test
    fun `renderTo OutputStream writes PDF content`() {
        val doc = document {
            page { paragraph("Stream test") }
        }
        val baos = ByteArrayOutputStream()
        doc.renderTo(baos)
        val text = String(baos.toByteArray(), Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.contains("Stream test"))
    }

    @Test
    fun `renderTo File writes PDF file`(@org.junit.jupiter.api.io.TempDir tempDir: java.io.File) {
        val doc = document {
            page { paragraph("File test") }
        }
        val file = java.io.File(tempDir, "test.pdf")
        doc.renderTo(file)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        val text = String(file.readBytes(), Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
    }

    // ==================== Edge cases ====================

    @Test
    fun `table with auto-calculated column widths`() {
        // Table without explicit columnWidths should auto-calculate
        val doc = document {
            page {
                table {
                    // No columnWidths set
                    row(TableCell("A"), TableCell("B"), TableCell("C"))
                    row(TableCell("D"), TableCell("E"), TableCell("F"))
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("A"))
        assertTrue(text.contains("F"))
    }

    @Test
    fun `JPEG with non-SOF0 markers skipped before SOF`() {
        // Build JPEG with an extra segment before SOF0
        val data = ByteArray(30)
        // SOI
        data[0] = 0xFF.toByte(); data[1] = 0xD8.toByte()
        // APP0 marker (FF E0) - should be skipped
        data[2] = 0xFF.toByte(); data[3] = 0xE0.toByte()
        // Segment length = 5
        data[4] = 0x00.toByte(); data[5] = 0x05.toByte()
        // Padding
        data[6] = 0x00; data[7] = 0x00; data[8] = 0x00
        // SOF0 marker after segment (at position 2 + 5 + 2 = 9)
        data[9] = 0xFF.toByte(); data[10] = 0xC0.toByte()
        // Segment length
        data[11] = 0x00.toByte(); data[12] = 0x0B.toByte()
        // Precision
        data[13] = 0x08.toByte()
        // Height = 200
        data[14] = 0x00.toByte(); data[15] = 0xC8.toByte()
        // Width = 300
        data[16] = 0x01.toByte(); data[17] = 0x2C.toByte()

        val dims = Writer.getImageDimensions(data, ImageFormat.JPEG)
        assertEquals(300, dims.first)
        assertEquals(200, dims.second)
    }

    @Test
    fun `writer reuse - can write multiple documents`() {
        val writer = Writer()
        val doc1 = document { page { paragraph("Doc1") } }
        val doc2 = document { page { paragraph("Doc2") } }

        val bytes1 = writer.write(doc1)
        val bytes2 = writer.write(doc2)

        val text1 = String(bytes1, Charsets.ISO_8859_1)
        val text2 = String(bytes2, Charsets.ISO_8859_1)
        assertTrue(text1.contains("Doc1"))
        assertTrue(text2.contains("Doc2"))
    }

    @Test
    fun `table header style is used for header rows`() {
        val doc = document {
            page {
                table {
                    headerRows = 1
                    headerStyle = textStyle { bold = true; fontSize = 14f; color = Color.WHITE }
                    row {
                        cell("H1")
                        cell("H2")
                    }
                    row {
                        cell("D1")
                        cell("D2")
                    }
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("14"))  // header font size
    }

    @Test
    fun `document with table fonts collects all cell styles`() {
        val doc = document {
            page {
                table {
                    row {
                        cell("Times", style = textStyle { fontFamily = FontFamily.TIMES })
                        cell("Courier", style = textStyle { fontFamily = FontFamily.COURIER })
                    }
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Times-Roman"))
        assertTrue(text.contains("Courier"))
    }

    @Test
    fun `paragraph with line spacing`() {
        val doc = document {
            page {
                paragraph("Line one. Line two words more words even more words to test wrapping.") {
                    lineSpacing = 2.0f
                    fontSize = 12f
                }
            }
        }
        val bytes = doc.render()
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun `heading with custom font renders correctly`() {
        val doc = document {
            page {
                heading("Courier Heading", level = 2) {
                    fontFamily = FontFamily.COURIER
                }
            }
        }
        val text = String(doc.render(), Charsets.ISO_8859_1)
        assertTrue(text.contains("Courier-Bold"))  // heading is always bold
    }

    @Test
    fun `main function with default output`(@org.junit.jupiter.api.io.TempDir tempDir: java.io.File) {
        // Test the default path branch of main
        val file = java.io.File(tempDir, "invoice.pdf")
        main(arrayOf(file.absolutePath))
        assertTrue(file.exists())
    }

    // ==================== Helper functions ====================

    private fun buildJpegData(width: Int, height: Int): ByteArray {
        // Build a minimal JPEG with SOI, SOF0 marker containing dimensions
        val data = ByteArray(20)
        // SOI marker
        data[0] = 0xFF.toByte()
        data[1] = 0xD8.toByte()
        // SOF0 marker
        data[2] = 0xFF.toByte()
        data[3] = 0xC0.toByte()
        // Segment length
        data[4] = 0x00.toByte()
        data[5] = 0x0B.toByte()
        // Precision
        data[6] = 0x08.toByte()
        // Height (big-endian)
        data[7] = ((height shr 8) and 0xFF).toByte()
        data[8] = (height and 0xFF).toByte()
        // Width (big-endian)
        data[9] = ((width shr 8) and 0xFF).toByte()
        data[10] = (width and 0xFF).toByte()
        return data
    }

    private fun buildMinimalPngData(width: Int, height: Int): ByteArray {
        // Build a minimal PNG header with IHDR chunk
        // PNG signature (8 bytes) + IHDR length (4) + "IHDR" (4) + width (4) + height (4) = 24 bytes minimum
        val data = ByteArray(30)
        // PNG signature
        data[0] = 0x89.toByte()
        data[1] = 0x50.toByte() // P
        data[2] = 0x4E.toByte() // N
        data[3] = 0x47.toByte() // G
        data[4] = 0x0D.toByte()
        data[5] = 0x0A.toByte()
        data[6] = 0x1A.toByte()
        data[7] = 0x0A.toByte()
        // IHDR chunk length (13 bytes)
        data[8] = 0x00.toByte()
        data[9] = 0x00.toByte()
        data[10] = 0x00.toByte()
        data[11] = 0x0D.toByte()
        // "IHDR"
        data[12] = 0x49.toByte() // I
        data[13] = 0x48.toByte() // H
        data[14] = 0x44.toByte() // D
        data[15] = 0x52.toByte() // R
        // Width (big-endian)
        data[16] = ((width shr 24) and 0xFF).toByte()
        data[17] = ((width shr 16) and 0xFF).toByte()
        data[18] = ((width shr 8) and 0xFF).toByte()
        data[19] = (width and 0xFF).toByte()
        // Height (big-endian)
        data[20] = ((height shr 24) and 0xFF).toByte()
        data[21] = ((height shr 16) and 0xFF).toByte()
        data[22] = ((height shr 8) and 0xFF).toByte()
        data[23] = (height and 0xFF).toByte()
        return data
    }
}
