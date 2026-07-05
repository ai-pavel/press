package pdf

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateTest {

    @Test
    fun `simple placeholder replacement`() {
        val result = Template.render("Hello, {{name}}!", mapOf("name" to "World"))
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `multiple placeholders`() {
        val result = Template.render(
            "{{greeting}}, {{name}}!",
            mapOf("greeting" to "Hi", "name" to "Alice")
        )
        assertEquals("Hi, Alice!", result)
    }

    @Test
    fun `nested key resolution`() {
        val data = mapOf(
            "user" to mapOf(
                "name" to "Bob",
                "email" to "bob@example.com"
            )
        )
        val result = Template.render("Name: {{user.name}}, Email: {{user.email}}", data)
        assertEquals("Name: Bob, Email: bob@example.com", result)
    }

    @Test
    fun `missing placeholder preserved`() {
        val result = Template.render("Hello, {{unknown}}!", mapOf("name" to "World"))
        assertEquals("Hello, {{unknown}}!", result)
    }

    @Test
    fun `placeholders with spaces`() {
        val result = Template.render("Hello, {{ name }}!", mapOf("name" to "World"))
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `template placeholders method`() {
        val template = Template("{{a}} and {{b}} and {{a}}")
        val keys = template.placeholders()
        assertEquals(setOf("a", "b"), keys)
    }

    @Test
    fun `renderDocument replaces placeholders in all elements`() {
        val doc = document {
            title = "Invoice {{number}}"
            page {
                heading("Invoice {{number}}")
                paragraph("Customer: {{customer}}")
            }
        }
        val data = mapOf("number" to "001", "customer" to "Alice")
        val rendered = Template.renderDocument(doc, data)

        assertEquals("Invoice 001", rendered.metadata.title)
        val heading = rendered.pages[0].elements[0] as Heading
        assertEquals("Invoice 001", heading.text)
        val para = rendered.pages[0].elements[1] as Paragraph
        assertEquals("Customer: Alice", para.text)
    }

    @Test
    fun `renderDocument replaces placeholders in table cells`() {
        val doc = document {
            page {
                table {
                    row(TableCell("{{item}}"), TableCell("{{price}}"))
                }
            }
        }
        val data = mapOf("item" to "Widget", "price" to "$10")
        val rendered = Template.renderDocument(doc, data)

        val table = rendered.pages[0].elements[0] as Table
        assertEquals("Widget", table.rows[0][0].text)
        assertEquals("$10", table.rows[0][1].text)
    }

    @Test
    fun `renderDocument preserves Image elements`() {
        val imgData = byteArrayOf(1, 2, 3)
        val doc = document {
            page {
                element(Image.fromBytes(imgData, ImageFormat.PNG, 100f, 80f))
            }
        }
        val rendered = Template.renderDocument(doc, mapOf("x" to "y"))
        val img = rendered.pages[0].elements[0] as Image
        assertTrue(imgData.contentEquals(img.data))
    }

    @Test
    fun `renderDocument preserves HorizontalRule elements`() {
        val doc = document {
            page { horizontalRule(2f, Color.RED) }
        }
        val rendered = Template.renderDocument(doc, emptyMap())
        val hr = rendered.pages[0].elements[0] as HorizontalRule
        assertEquals(2f, hr.thickness)
        assertEquals(Color.RED, hr.color)
    }

    @Test
    fun `renderDocument preserves Spacer elements`() {
        val doc = document {
            page { spacer(25f) }
        }
        val rendered = Template.renderDocument(doc, emptyMap())
        val spacer = rendered.pages[0].elements[0] as Spacer
        assertEquals(25f, spacer.height)
    }

    @Test
    fun `renderDocument replaces metadata author and subject`() {
        val doc = document {
            title = "{{title}}"
            author = "{{author}}"
            subject = "{{subject}}"
            page { paragraph("content") }
        }
        val data = mapOf("title" to "T", "author" to "A", "subject" to "S")
        val rendered = Template.renderDocument(doc, data)
        assertEquals("T", rendered.metadata.title)
        assertEquals("A", rendered.metadata.author)
        assertEquals("S", rendered.metadata.subject)
    }

    @Test
    fun `renderDocument preserves creator`() {
        val doc = document {
            creator = "My Creator"
            page { paragraph("test") }
        }
        val rendered = Template.renderDocument(doc, emptyMap())
        assertEquals("My Creator", rendered.metadata.creator)
    }

    @Test
    fun `template with no placeholders`() {
        val result = Template.render("Hello World", emptyMap())
        assertEquals("Hello World", result)
    }

    @Test
    fun `template empty string`() {
        val result = Template.render("", emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `nested key with three levels`() {
        val data = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "deep_value"
                )
            )
        )
        val result = Template.render("{{a.b.c}}", data)
        assertEquals("deep_value", result)
    }

    @Test
    fun `nested key with missing intermediate`() {
        val data = mapOf("a" to mapOf("b" to "value"))
        val result = Template.render("{{a.c.d}}", data)
        assertEquals("{{a.c.d}}", result)
    }

    @Test
    fun `nested key with non-map intermediate`() {
        val data = mapOf("a" to "string_value")
        val result = Template.render("{{a.b}}", data)
        assertEquals("{{a.b}}", result)
    }

    @Test
    fun `template instance render method`() {
        val template = Template("Hello, {{name}}!")
        val result = template.render(mapOf("name" to "World"))
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `placeholders returns empty set for no placeholders`() {
        val template = Template("No placeholders here")
        assertEquals(emptySet(), template.placeholders())
    }

    @Test
    fun `placeholders with underscore in key`() {
        val template = Template("{{first_name}} {{last_name}}")
        assertEquals(setOf("first_name", "last_name"), template.placeholders())
    }

    @Test
    fun `renderDocument with multiple pages`() {
        val doc = document {
            page { paragraph("Page1 {{x}}") }
            page { paragraph("Page2 {{x}}") }
        }
        val rendered = Template.renderDocument(doc, mapOf("x" to "VAL"))
        val p1 = rendered.pages[0].elements[0] as Paragraph
        val p2 = rendered.pages[1].elements[0] as Paragraph
        assertEquals("Page1 VAL", p1.text)
        assertEquals("Page2 VAL", p2.text)
    }

    @Test
    fun `template with numeric value`() {
        val result = Template.render("Count: {{count}}", mapOf("count" to 42))
        assertEquals("Count: 42", result)
    }
}
