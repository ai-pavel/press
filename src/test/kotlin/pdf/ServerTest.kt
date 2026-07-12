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
}
