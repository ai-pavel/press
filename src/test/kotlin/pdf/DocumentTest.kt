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
}
