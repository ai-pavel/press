package pdf

import java.io.File
import java.io.OutputStream

/**
 * Metadata for a PDF document.
 */
data class DocumentMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val creator: String = "pdf-generator 1.0"
)

/**
 * A PDF document consisting of pages and metadata.
 */
data class Document(
    val metadata: DocumentMetadata = DocumentMetadata(),
    val pages: List<Page> = emptyList()
) {
    fun render(): ByteArray = Writer().write(this)

    fun renderTo(outputStream: OutputStream) {
        outputStream.write(render())
    }

    fun renderTo(file: File) {
        file.writeBytes(render())
    }
}

/**
 * DSL builder for Document.
 */
class DocumentBuilder {
    private val pages = mutableListOf<Page>()
    var title: String = ""
    var author: String = ""
    var subject: String = ""
    var creator: String = "pdf-generator 1.0"

    fun page(init: PageBuilder.() -> Unit) {
        val builder = PageBuilder()
        builder.init()
        pages.add(builder.build())
    }

    fun addPage(page: Page) {
        pages.add(page)
    }

    fun build(): Document = Document(
        metadata = DocumentMetadata(title, author, subject, creator),
        pages = pages.toList()
    )
}

/**
 * Top-level DSL entry point for creating a PDF document.
 */
fun document(init: DocumentBuilder.() -> Unit): Document {
    val builder = DocumentBuilder()
    builder.init()
    return builder.build()
}
