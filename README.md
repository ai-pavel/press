# PDF Generator

[![CI](https://github.com/ai-pavel/pdf-generator/actions/workflows/ci.yml/badge.svg)](https://github.com/ai-pavel/pdf-generator/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/ai-pavel/pdf-generator/branch/main/graph/badge.svg)](https://codecov.io/gh/ai-pavel/pdf-generator)

A Kotlin library for generating PDF 1.4 documents with a clean DSL.

## Features

- **DSL API** for building PDF documents with pages, paragraphs, headings, tables, images, and styled text
- **Table support** with borders, cell background colors, column/row spanning
- **Image embedding** for PNG and JPEG formats
- **Styled text** with font family (Helvetica, Times, Courier), size, color, bold, italic
- **Template engine** with `{{placeholder}}` syntax and nested key support
- **Valid PDF 1.4** output with proper cross-reference tables
- **CLI** that renders a sample invoice PDF

## Quick Start

```kotlin
import pdf.*

val doc = document {
    title = "My Document"
    author = "Author Name"

    page {
        heading("Hello, PDF!", level = 1) {
            color = Color.BLUE
        }

        paragraph("This is a paragraph with styled text.") {
            fontFamily = FontFamily.TIMES
            fontSize = 14f
            italic = true
        }

        table {
            columnWidths = listOf(200f, 150f, 100f)
            headerRow("Name", "Role", "Score")
            row(TableCell("Alice"), TableCell("Engineer"), TableCell("95"))
            row(TableCell("Bob"), TableCell("Designer"), TableCell("88"))
        }

        horizontalRule()
        spacer(20f)

        paragraph("Footer text") {
            fontSize = 9f
            color = Color.GRAY
        }
    }
}

doc.renderTo(File("output.pdf"))
```

## Template Engine

```kotlin
val doc = document {
    page {
        heading("Invoice {{number}}")
        paragraph("Customer: {{customer.name}}")
    }
}

val data = mapOf(
    "number" to "INV-001",
    "customer" to mapOf("name" to "Jane Smith")
)

val rendered = Template.renderDocument(doc, data)
rendered.renderTo(File("invoice.pdf"))
```

## CLI

Generate a sample invoice:

```bash
./gradlew run
# or with custom output path:
./gradlew run --args="my-invoice.pdf"
```

## Build

```bash
./gradlew build        # Build and run tests
./gradlew test         # Run tests only
./gradlew run          # Generate sample invoice
```

## Project Structure

```
src/main/kotlin/pdf/
  Document.kt   - Document DSL and metadata
  Page.kt       - Page sizes, margins, and page builder
  Element.kt    - Base element types (Paragraph, Heading, Spacer, HorizontalRule)
  Table.kt      - Table with borders, cell spanning, and DSL
  Image.kt      - Embedded PNG/JPEG image support
  Style.kt      - Font families, colors, and text styles
  Writer.kt     - PDF 1.4 file generator with cross-reference tables
  Template.kt   - Template engine with placeholder substitution
  Cli.kt        - CLI entry point for sample invoice generation
```
