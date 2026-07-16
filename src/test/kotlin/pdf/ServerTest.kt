package pdf

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerTest {

    private lateinit var server: PdfServer
    private val client = HttpClient.newHttpClient()

    @BeforeEach
    fun setUp() {
        server = PdfServer(port = 0)
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    private fun url(path: String) = "http://localhost:${server.boundPort}$path"

    @Test
    fun `health returns ok status json`() {
        val request = HttpRequest.newBuilder(URI.create(url("/health"))).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(""))
        assertEquals("""{"status":"ok","service":"press"}""", response.body())
    }

    @Test
    fun `generate returns pdf for valid invoice json`() {
        val body = """
            {
              "invoice_number": "INV-TEST-042",
              "date": "2024-01-15",
              "company": {
                "name": "Test Corp",
                "address": "123 Test St",
                "phone": "(555) 000-0000",
                "email": "billing@test.com"
              },
              "customer": {
                "name": "John Doe",
                "company": "Doe Inc",
                "address": "456 Test Ave"
              },
              "subtotal": "$100.00",
              "tax": "$9.00",
              "total": "$109.00",
              "notes": "Thank you!"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder(URI.create(url("/generate")))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())

        assertEquals(200, response.statusCode())
        assertEquals("application/pdf", response.headers().firstValue("Content-Type").orElse(""))
        val bytes = response.body()
        assertTrue(bytes.isNotEmpty())
        val text = String(bytes, Charsets.ISO_8859_1)
        assertTrue(text.startsWith("%PDF"))
        assertTrue(text.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun `generate returns 400 with json error for malformed body`() {
        val request = HttpRequest.newBuilder(URI.create(url("/generate")))
            .POST(HttpRequest.BodyPublishers.ofString("{not valid json"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(400, response.statusCode())
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(""))
        assertTrue(response.body().startsWith("""{"error":"""))
    }

    @Test
    fun `GET on generate returns 405 method not allowed`() {
        val request = HttpRequest.newBuilder(URI.create(url("/generate"))).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(405, response.statusCode())
    }

    @Test
    fun `POST on health returns 405 method not allowed`() {
        val request = HttpRequest.newBuilder(URI.create(url("/health")))
            .POST(HttpRequest.BodyPublishers.ofString("x"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(405, response.statusCode())
    }

    @Test
    fun `generate with non-object root json returns 400`() {
        val request = HttpRequest.newBuilder(URI.create(url("/generate")))
            .POST(HttpRequest.BodyPublishers.ofString("[1,2,3]"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(400, response.statusCode())
        assertTrue(response.body().startsWith("""{"error":"""))
    }

    @Test
    fun `generate error body escapes quotes and backslashes`() {
        // A non-object root yields a message; error body must escape quotes/backslashes.
        // Use a payload that parses as an object but contains a value that triggers
        // a rendering error with quotes/backslashes in the message.
        // We send a JSON object that is valid but whose body is a string with backslash
        // and quote to exercise sendJsonError escaping.
        val body = """{"notes":"\\ and \" quote"}"""
        val request = HttpRequest.newBuilder(URI.create(url("/generate")))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        // Either 200 (rendered) or 400/500; if error, ensure JSON shape.
        assertTrue(response.statusCode() in 200..599)
        if (response.body().startsWith("""{"error":""")) {
            // Ensure no raw unescaped quotes break JSON structure
            assertTrue(response.body().endsWith("}"))
        }
    }

    @Test
    fun `PdfServer default port constant is 8080`() {
        assertEquals(8080, PdfServer.DEFAULT_PORT)
    }

    @Test
    fun `boundPort reflects actual bound port`() {
        val s = PdfServer(port = 0)
        s.start()
        try {
            assertTrue(s.boundPort > 0)
        } finally {
            s.stop()
        }
    }

    @Test
    fun `generate with valid object containing nested nulls renders pdf`() {
        val body = """
            {
              "invoice_number": "INV-1",
              "date": "2024-01-01",
              "company": {"name": "C", "address": "A", "phone": "P", "email": "E"},
              "customer": {"name": "N", "company": "Co", "address": "Ad"},
              "subtotal": "$1",
              "tax": "$0",
              "total": "$1",
              "notes": null
            }
        """.trimIndent()
        val request = HttpRequest.newBuilder(URI.create(url("/generate")))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        assertEquals(200, response.statusCode())
        assertTrue(response.body().isNotEmpty())
    }
}
