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
}
