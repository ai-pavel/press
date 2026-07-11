package pdf

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * Minimal HTTP server exposing the PDF generator as a microservice.
 * Uses the JDK built-in HttpServer so no extra dependencies are required.
 *
 * Routes:
 * - `GET /health`    -> 200 with a JSON status body.
 * - `POST /generate` -> renders the invoice template with the JSON data
 *   from the request body and returns the PDF bytes.
 *
 * The /generate request body is a JSON object matching the data the
 * invoice template consumes (all values are strings; nested objects are
 * addressed by the template with dot notation):
 * ```json
 * {
 *   "invoice_number": "INV-2024-001",
 *   "date": "2024-01-15",
 *   "company": {"name": "...", "address": "...", "phone": "...", "email": "..."},
 *   "customer": {"name": "...", "company": "...", "address": "..."},
 *   "subtotal": "$1,750.00",
 *   "tax": "$157.50",
 *   "total": "$1,907.50",
 *   "notes": "..."
 * }
 * ```
 * Missing keys leave their `{{placeholder}}` untouched in the output.
 */
class PdfServer(port: Int = DEFAULT_PORT) {

    companion object {
        const val DEFAULT_PORT = 8080
    }

    private val healthBody = """{"status":"ok","service":"pdf-generator"}""".toByteArray(Charsets.UTF_8)

    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/health") { exchange ->
            try {
                if (exchange.requestMethod == "GET") {
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, healthBody.size.toLong())
                    exchange.responseBody.write(healthBody)
                } else {
                    exchange.sendResponseHeaders(405, -1)
                }
            } finally {
                exchange.close()
            }
        }

        createContext("/generate") { exchange ->
            try {
                if (exchange.requestMethod == "POST") {
                    handleGenerate(exchange)
                } else {
                    exchange.sendResponseHeaders(405, -1)
                }
            } finally {
                exchange.close()
            }
        }
    }

    private fun handleGenerate(exchange: HttpExchange) {
        val body = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
        val data: Map<String, Any> = try {
            sanitize(Json.parseObject(body))
        } catch (e: JsonParseException) {
            sendJsonError(exchange, 400, e.message ?: "Malformed JSON")
            return
        }

        val pdfBytes = try {
            Template.renderDocument(createInvoiceTemplate(), data).render()
        } catch (e: Exception) {
            sendJsonError(exchange, 500, e.message ?: "PDF rendering failed")
            return
        }

        exchange.responseHeaders.add("Content-Type", "application/pdf")
        exchange.sendResponseHeaders(200, pdfBytes.size.toLong())
        exchange.responseBody.write(pdfBytes)
    }

    /** Drops null values (recursively) so the data matches Template's Map<String, Any> input. */
    private fun sanitize(raw: Map<String, Any?>): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()
        for ((key, value) in raw) {
            when (value) {
                null -> Unit
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    result[key] = sanitize(value as Map<String, Any?>)
                }
                else -> result[key] = value
            }
        }
        return result
    }

    private fun sendJsonError(exchange: HttpExchange, status: Int, message: String) {
        val escaped = message.replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"error":"$escaped"}""".toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, body.size.toLong())
        exchange.responseBody.write(body)
    }

    /** Actual port the server is bound to (useful when constructed with port 0). */
    val boundPort: Int
        get() = server.address.port

    fun start() {
        server.start()
        println("pdf-generator HTTP server started on port $boundPort")
    }

    fun stop() {
        server.stop(0)
    }
}
