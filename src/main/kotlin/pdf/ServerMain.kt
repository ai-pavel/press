package pdf

/**
 * Server entry point: starts the PDF generator HTTP service.
 *
 * The port defaults to 8080 and can be overridden with the HTTP_PORT
 * environment variable.
 */
fun main() {
    val port = System.getenv("HTTP_PORT")?.toIntOrNull() ?: PdfServer.DEFAULT_PORT
    val server = PdfServer(port)
    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
    server.start()
}
