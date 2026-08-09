package pdf

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * CLI entry point that renders a sample invoice PDF.
 */
fun main(args: Array<String>) {
    val outputPath = if (args.isNotEmpty()) args[0] else "invoice.pdf"

    println("PDF Generator - Sample Invoice")
    println("==============================")

    val invoiceData = mapOf(
        "invoice_number" to "INV-2024-001",
        "date" to LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        "company" to mapOf(
            "name" to "Acme Corporation",
            "address" to "123 Business Ave, Suite 100, San Francisco, CA 94102",
            "phone" to "(555) 123-4567",
            "email" to "billing@acme.com"
        ),
        "customer" to mapOf(
            "name" to "Jane Smith",
            "company" to "Smith Enterprises",
            "address" to "456 Oak Street, Portland, OR 97201"
        ),
        "subtotal" to "$1,750.00",
        "tax" to "$157.50",
        "total" to "$1,907.50",
        "notes" to "Payment is due within 30 days. Thank you for your business!"
    )

    val invoiceTemplate = createInvoiceTemplate()
    val renderedDoc = Template.renderDocument(invoiceTemplate, invoiceData)

    val file = File(outputPath)
    renderedDoc.renderTo(file)

    println("Invoice generated: ${file.absolutePath}")
    println("File size: ${file.length()} bytes")
}

/**
 * Creates an invoice document template with placeholders.
 */
fun createInvoiceTemplate(): Document = document {
    title = "Invoice {{invoice_number}}"
    author = "{{company.name}}"
    subject = "Invoice"

    page {
        // Company header
        heading("{{company.name}}", level = 1) {
            color = Color.hex("1a5276")
        }

        paragraph("{{company.address}}") {
            fontSize = 10f
            color = Color.GRAY
        }
        paragraph("Phone: {{company.phone}} | Email: {{company.email}}") {
            fontSize = 10f
            color = Color.GRAY
        }

        spacer(20f)
        horizontalRule(2f, Color.hex("1a5276"))
        spacer(16f)

        // Invoice title and details
        heading("INVOICE", level = 2) {
            color = Color.hex("1a5276")
        }

        spacer(8f)

        table {
            columnWidths = listOf(120f, 200f, 80f, 120f)
            borderStyle = BorderStyle(enabled = false)

            row {
                cell("Invoice Number:", style = textStyle { bold = true; fontSize = 10f })
                cell("{{invoice_number}}", style = textStyle { fontSize = 10f })
                cell("Date:", style = textStyle { bold = true; fontSize = 10f })
                cell("{{date}}", style = textStyle { fontSize = 10f })
            }
        }

        spacer(16f)

        // Bill To section
        heading("Bill To:", level = 3) {
            color = Color.DARK_GRAY
        }
        paragraph("{{customer.name}}") {
            bold = true
            fontSize = 11f
        }
        paragraph("{{customer.company}}") {
            fontSize = 10f
        }
        paragraph("{{customer.address}}") {
            fontSize = 10f
            color = Color.GRAY
        }

        spacer(20f)

        // Items table
        table {
            columnWidths = listOf(200f, 80f, 80f, 91f)
            borderStyle = BorderStyle(width = 0.5f, color = Color.GRAY)
            headerRows = 1
            headerStyle = textStyle {
                bold = true
                fontSize = 10f
                color = Color.WHITE
            }

            row {
                cell("Description", style = textStyle { bold = true; fontSize = 10f; color = Color.WHITE }, backgroundColor = Color.hex("1a5276"))
                cell("Quantity", style = textStyle { bold = true; fontSize = 10f; color = Color.WHITE }, backgroundColor = Color.hex("1a5276"))
                cell("Unit Price", style = textStyle { bold = true; fontSize = 10f; color = Color.WHITE }, backgroundColor = Color.hex("1a5276"))
                cell("Amount", style = textStyle { bold = true; fontSize = 10f; color = Color.WHITE }, backgroundColor = Color.hex("1a5276"))
            }

            row {
                cell("Web Development Services", style = textStyle { fontSize = 10f })
                cell("40 hrs", style = textStyle { fontSize = 10f })
                cell("$25.00", style = textStyle { fontSize = 10f })
                cell("$1,000.00", style = textStyle { fontSize = 10f })
            }
            row {
                cell("UI/UX Design", style = textStyle { fontSize = 10f }, backgroundColor = Color.LIGHT_GRAY)
                cell("20 hrs", style = textStyle { fontSize = 10f }, backgroundColor = Color.LIGHT_GRAY)
                cell("$30.00", style = textStyle { fontSize = 10f }, backgroundColor = Color.LIGHT_GRAY)
                cell("$600.00", style = textStyle { fontSize = 10f }, backgroundColor = Color.LIGHT_GRAY)
            }
            row {
                cell("Project Management", style = textStyle { fontSize = 10f })
                cell("10 hrs", style = textStyle { fontSize = 10f })
                cell("$15.00", style = textStyle { fontSize = 10f })
                cell("$150.00", style = textStyle { fontSize = 10f })
            }
        }

        spacer(12f)

        // Totals table
        table {
            columnWidths = listOf(280f, 91f, 80f)
            borderStyle = BorderStyle(enabled = false)

            row {
                cell("")
                cell("Subtotal:", style = textStyle { bold = true; fontSize = 10f })
                cell("{{subtotal}}", style = textStyle { fontSize = 10f })
            }
            row {
                cell("")
                cell("Tax (9%):", style = textStyle { bold = true; fontSize = 10f })
                cell("{{tax}}", style = textStyle { fontSize = 10f })
            }
            row {
                cell("")
                cell("TOTAL:", style = textStyle { bold = true; fontSize = 12f; color = Color.hex("1a5276") })
                cell("{{total}}", style = textStyle { bold = true; fontSize = 12f; color = Color.hex("1a5276") })
            }
        }

        spacer(30f)
        horizontalRule(1f, Color.LIGHT_GRAY)
        spacer(8f)

        paragraph("{{notes}}") {
            fontSize = 9f
            italic = true
            color = Color.GRAY
        }
    }
}
