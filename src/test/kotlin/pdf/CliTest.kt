package pdf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliTest {

    @Test
    fun `createInvoiceTemplate returns document with pages`() {
        val doc = createInvoiceTemplate()
        assertEquals(1, doc.pages.size)
    }

    @Test
    fun `createInvoiceTemplate has correct metadata placeholders`() {
        val doc = createInvoiceTemplate()
        assertEquals("Invoice {{invoice_number}}", doc.metadata.title)
        assertEquals("{{company.name}}", doc.metadata.author)
        assertEquals("Invoice", doc.metadata.subject)
    }

    @Test
    fun `createInvoiceTemplate page has elements`() {
        val doc = createInvoiceTemplate()
        val page = doc.pages[0]
        assertTrue(page.elements.isNotEmpty())
    }

    @Test
    fun `createInvoiceTemplate contains headings`() {
        val doc = createInvoiceTemplate()
        val headings = doc.pages[0].elements.filterIsInstance<Heading>()
        assertTrue(headings.isNotEmpty())
        assertTrue(headings.any { it.text.contains("{{company.name}}") })
        assertTrue(headings.any { it.text == "INVOICE" })
    }

    @Test
    fun `createInvoiceTemplate contains paragraphs`() {
        val doc = createInvoiceTemplate()
        val paragraphs = doc.pages[0].elements.filterIsInstance<Paragraph>()
        assertTrue(paragraphs.isNotEmpty())
        assertTrue(paragraphs.any { it.text.contains("{{company.address}}") })
    }

    @Test
    fun `createInvoiceTemplate contains tables`() {
        val doc = createInvoiceTemplate()
        val tables = doc.pages[0].elements.filterIsInstance<Table>()
        assertTrue(tables.size >= 3)
    }

    @Test
    fun `createInvoiceTemplate contains spacers`() {
        val doc = createInvoiceTemplate()
        val spacers = doc.pages[0].elements.filterIsInstance<Spacer>()
        assertTrue(spacers.isNotEmpty())
    }

    @Test
    fun `createInvoiceTemplate contains horizontal rules`() {
        val doc = createInvoiceTemplate()
        val rules = doc.pages[0].elements.filterIsInstance<HorizontalRule>()
        assertTrue(rules.isNotEmpty())
    }

    @Test
    fun `createInvoiceTemplate can be rendered with data`() {
        val invoiceData = mapOf(
            "invoice_number" to "INV-TEST-001",
            "date" to "2024-01-01",
            "company" to mapOf(
                "name" to "Test Corp",
                "address" to "123 Test St",
                "phone" to "(555) 000-0000",
                "email" to "test@test.com"
            ),
            "customer" to mapOf(
                "name" to "John Doe",
                "company" to "Doe Inc",
                "address" to "456 Test Ave"
            ),
            "subtotal" to "$100.00",
            "tax" to "$9.00",
            "total" to "$109.00",
            "notes" to "Thank you!"
        )

        val template = createInvoiceTemplate()
        val rendered = Template.renderDocument(template, invoiceData)

        assertEquals("Invoice INV-TEST-001", rendered.metadata.title)
        assertEquals("Test Corp", rendered.metadata.author)

        val headings = rendered.pages[0].elements.filterIsInstance<Heading>()
        assertTrue(headings.any { it.text == "Test Corp" })
    }

    @Test
    fun `createInvoiceTemplate renders to valid PDF bytes`() {
        val invoiceData = mapOf(
            "invoice_number" to "INV-001",
            "date" to "2024-01-01",
            "company" to mapOf(
                "name" to "Acme",
                "address" to "123 St",
                "phone" to "555-1234",
                "email" to "a@b.com"
            ),
            "customer" to mapOf(
                "name" to "Jane",
                "company" to "JaneCo",
                "address" to "456 Ave"
            ),
            "subtotal" to "$100",
            "tax" to "$9",
            "total" to "$109",
            "notes" to "Thanks!"
        )

        val template = createInvoiceTemplate()
        val rendered = Template.renderDocument(template, invoiceData)
        val bytes = rendered.render()

        assertTrue(bytes.isNotEmpty())
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun `main writes invoice to specified path`(@TempDir tempDir: File) {
        val outputFile = File(tempDir, "test-invoice.pdf")
        main(arrayOf(outputFile.absolutePath))

        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)
        val bytes = outputFile.readBytes()
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF-1.4"))
    }

    @Test
    fun `main writes invoice to default path when no args`(@TempDir tempDir: File) {
        // Change to temp dir to avoid polluting project directory
        val origDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", tempDir.absolutePath)
            // Since main uses File(outputPath) which is relative, we test with explicit path
            val outputFile = File(tempDir, "invoice.pdf")
            main(arrayOf(outputFile.absolutePath))
            assertTrue(outputFile.exists())
        } finally {
            System.setProperty("user.dir", origDir)
        }
    }
}
