package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PageTest {

    @Test
    fun `PageBuilder image method adds Image element`() {
        val builder = PageBuilder()
        val data = byteArrayOf(1, 2, 3, 4)
        builder.image(data, ImageFormat.JPEG, 100f, 80f)
        val page = builder.build()
        assertEquals(1, page.elements.size)
        assertTrue(page.elements[0] is Image)
        val img = page.elements[0] as Image
        assertEquals(ImageFormat.JPEG, img.format)
        assertEquals(100f, img.width)
        assertEquals(80f, img.height)
        assertEquals(Image.Alignment.LEFT, img.alignment)
    }

    @Test
    fun `PageBuilder image method with alignment`() {
        val builder = PageBuilder()
        builder.image(byteArrayOf(1), ImageFormat.PNG, 50f, 40f, Image.Alignment.CENTER)
        val page = builder.build()
        val img = page.elements[0] as Image
        assertEquals(Image.Alignment.CENTER, img.alignment)
    }

    @Test
    fun `PageBuilder element method adds arbitrary element`() {
        val builder = PageBuilder()
        val spacer = Spacer(30f)
        builder.element(spacer)
        val page = builder.build()
        assertEquals(1, page.elements.size)
        assertTrue(page.elements[0] is Spacer)
        assertEquals(30f, (page.elements[0] as Spacer).height)
    }

    @Test
    fun `PageBuilder table method`() {
        val builder = PageBuilder()
        builder.table {
            row(TableCell("A"), TableCell("B"))
        }
        val page = builder.build()
        assertEquals(1, page.elements.size)
        assertTrue(page.elements[0] is Table)
    }

    @Test
    fun `PageBuilder heading with custom style`() {
        val builder = PageBuilder()
        builder.heading("Title", level = 2) {
            color = Color.RED
            fontFamily = FontFamily.TIMES
            fontSize = 18f
        }
        val page = builder.build()
        val heading = page.elements[0] as Heading
        assertEquals("Title", heading.text)
        assertEquals(2, heading.level)
        assertEquals(Color.RED, heading.style.color)
        assertEquals(FontFamily.TIMES, heading.style.fontFamily)
    }

    @Test
    fun `PageBuilder paragraph with style`() {
        val builder = PageBuilder()
        builder.paragraph("Text") {
            fontSize = 14f
            bold = true
            italic = true
            lineSpacing = 1.8f
        }
        val page = builder.build()
        val para = page.elements[0] as Paragraph
        assertEquals("Text", para.text)
        assertEquals(14f, para.style.fontSize)
        assertTrue(para.style.bold)
        assertTrue(para.style.italic)
        assertEquals(1.8f, para.style.lineSpacing)
    }

    @Test
    fun `PageBuilder size and margins`() {
        val builder = PageBuilder()
        builder.size = PageSize.LEGAL
        builder.margins = Margins(36f, 36f, 36f, 36f)
        builder.paragraph("Content")
        val page = builder.build()
        assertEquals(PageSize.LEGAL, page.size)
        assertEquals(36f, page.margins.top)
    }

    @Test
    fun `Page contentWidth with various sizes`() {
        val page = Page(size = PageSize.LETTER, margins = Margins(50f, 50f, 50f, 50f))
        assertEquals(612f - 100f, page.contentWidth)
    }

    @Test
    fun `Page contentHeight`() {
        val page = Page(size = PageSize.A4, margins = Margins(72f, 72f, 72f, 72f))
        assertEquals(842f - 144f, page.contentHeight)
    }

    @Test
    fun `Page contentWidth with A3 size`() {
        val page = Page(size = PageSize.A3, margins = Margins(72f, 72f, 72f, 72f))
        assertEquals(842f - 144f, page.contentWidth)
        assertEquals(1190f - 144f, page.contentHeight)
    }

    @Test
    fun `Page contentWidth with A5 size`() {
        val page = Page(size = PageSize.A5, margins = Margins(36f, 36f, 36f, 36f))
        assertEquals(420f - 72f, page.contentWidth)
        assertEquals(595f - 72f, page.contentHeight)
    }

    @Test
    fun `all PageSize enum values`() {
        assertEquals(5, PageSize.values().size)
        assertEquals(PageSize.A4, PageSize.valueOf("A4"))
        assertEquals(PageSize.LETTER, PageSize.valueOf("LETTER"))
        assertEquals(PageSize.LEGAL, PageSize.valueOf("LEGAL"))
        assertEquals(PageSize.A3, PageSize.valueOf("A3"))
        assertEquals(PageSize.A5, PageSize.valueOf("A5"))
    }

    @Test
    fun `Margins default values`() {
        val margins = Margins()
        assertEquals(72f, margins.top)
        assertEquals(72f, margins.right)
        assertEquals(72f, margins.bottom)
        assertEquals(72f, margins.left)
    }

    @Test
    fun `Margins custom values`() {
        val margins = Margins(10f, 20f, 30f, 40f)
        assertEquals(10f, margins.top)
        assertEquals(20f, margins.right)
        assertEquals(30f, margins.bottom)
        assertEquals(40f, margins.left)
    }

    @Test
    fun `Page default values`() {
        val page = Page()
        assertEquals(PageSize.A4, page.size)
        assertEquals(Margins(), page.margins)
        assertTrue(page.elements.isEmpty())
    }

    @Test
    fun `PageSize dimensions`() {
        assertEquals(612f, PageSize.LEGAL.width)
        assertEquals(1008f, PageSize.LEGAL.height)
        assertEquals(842f, PageSize.A3.width)
        assertEquals(1190f, PageSize.A3.height)
        assertEquals(420f, PageSize.A5.width)
        assertEquals(595f, PageSize.A5.height)
    }
}
