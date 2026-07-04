package pdf

/**
 * Simple template engine that fills placeholders in text from a data map.
 *
 * Placeholders use the format `{{key}}` and are replaced with the
 * corresponding value from the data map. Nested keys are supported
 * with dot notation: `{{customer.name}}`.
 *
 * Usage:
 * ```kotlin
 * val template = Template("Hello, {{name}}!")
 * val result = template.render(mapOf("name" to "World"))
 * // result == "Hello, World!"
 * ```
 */
class Template(private val templateText: String) {

    companion object {
        private val PLACEHOLDER_REGEX = Regex("\\{\\{\\s*([a-zA-Z0-9_.]+)\\s*}}")

        /**
         * Renders a template string with the given data map.
         */
        fun render(template: String, data: Map<String, Any>): String {
            return Template(template).render(data)
        }

        /**
         * Processes all text elements in a document, replacing placeholders.
         */
        fun renderDocument(document: Document, data: Map<String, Any>): Document {
            val renderedPages = document.pages.map { page ->
                val renderedElements = page.elements.map { element ->
                    renderElement(element, data)
                }
                page.copy(elements = renderedElements)
            }
            return document.copy(
                metadata = DocumentMetadata(
                    title = render(document.metadata.title, data),
                    author = render(document.metadata.author, data),
                    subject = render(document.metadata.subject, data),
                    creator = document.metadata.creator
                ),
                pages = renderedPages
            )
        }

        private fun renderElement(element: Element, data: Map<String, Any>): Element {
            return when (element) {
                is Paragraph -> element.copy(text = render(element.text, data))
                is Heading -> element.copy(text = render(element.text, data))
                is Table -> {
                    val renderedRows = element.rows.map { row ->
                        row.map { cell ->
                            cell.copy(text = render(cell.text, data))
                        }
                    }
                    element.copy(rows = renderedRows)
                }
                is Image -> element
                is HorizontalRule -> element
                is Spacer -> element
            }
        }
    }

    /**
     * Renders the template with the given data map.
     */
    fun render(data: Map<String, Any>): String {
        return PLACEHOLDER_REGEX.replace(templateText) { matchResult ->
            val key = matchResult.groupValues[1]
            resolveValue(key, data)?.toString() ?: matchResult.value
        }
    }

    /**
     * Returns the set of placeholder keys found in the template.
     */
    fun placeholders(): Set<String> {
        return PLACEHOLDER_REGEX.findAll(templateText).map { it.groupValues[1] }.toSet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveValue(key: String, data: Map<String, Any>): Any? {
        if (key.contains('.')) {
            val parts = key.split('.')
            var current: Any? = data
            for (part in parts) {
                current = when (current) {
                    is Map<*, *> -> (current as Map<String, Any>)[part]
                    else -> return null
                }
            }
            return current
        }
        return data[key]
    }
}
