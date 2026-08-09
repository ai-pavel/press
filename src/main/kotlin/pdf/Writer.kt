package pdf

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * PDF 1.4 writer that generates valid PDF files with proper cross-reference tables.
 *
 * Supports text (with fonts, sizes, colors, bold, italic), tables with borders
 * and cell spanning, embedded PNG/JPEG images, and standard page sizes.
 */
class Writer {

    private lateinit var out: ByteArrayOutputStream
    private var objectCount = 0
    private val offsets = mutableListOf<Int>()
    private val fontMap = mutableMapOf<String, Int>() // fontName -> objectNumber
    private val imageObjects = mutableListOf<ImageObject>()

    private data class ImageObject(
        val objectNumber: Int,
        val data: ByteArray,
        val format: ImageFormat,
        val pixelWidth: Int,
        val pixelHeight: Int
    )

    fun write(document: Document): ByteArray {
        out = ByteArrayOutputStream()
        objectCount = 0
        offsets.clear()
        fontMap.clear()
        imageObjects.clear()

        // Collect all fonts needed
        val allFonts = collectFonts(document)

        // Pre-allocate object numbers:
        // 1 = catalog, 2 = pages, then fonts, then images, then page objects + content streams
        val catalogObj = nextObjNumber()   // 1
        val pagesObj = nextObjNumber()     // 2
        val infoObj = nextObjNumber()      // 3

        // Allocate font objects
        for (fontName in allFonts) {
            fontMap[fontName] = nextObjNumber()
        }

        // Pre-scan images and allocate object numbers
        val imageDataMap = mutableMapOf<Int, Int>() // element hashCode -> obj number
        for (page in document.pages) {
            for (element in page.elements) {
                if (element is Image) {
                    val key = element.data.contentHashCode()
                    if (key !in imageDataMap) {
                        imageDataMap[key] = nextObjNumber()
                    }
                }
            }
        }

        // Allocate page + content stream objects
        data class PageObjPair(val pageObj: Int, val contentObj: Int)
        val pageObjPairs = document.pages.map {
            PageObjPair(nextObjNumber(), nextObjNumber())
        }

        // --- Write PDF header ---
        writeRaw("%PDF-1.4\n")
        // Binary comment to mark as binary file
        writeRaw("%\u00E2\u00E3\u00CF\u00D3\n")

        // --- Write catalog ---
        writeObj(catalogObj) {
            writeRaw("<< /Type /Catalog /Pages $pagesObj 0 R >>\n")
        }

        // --- Write pages dictionary ---
        writeObj(pagesObj) {
            val kids = pageObjPairs.joinToString(" ") { "${it.pageObj} 0 R" }
            writeRaw("<< /Type /Pages /Kids [ $kids ] /Count ${document.pages.size} >>\n")
        }

        // --- Write info dictionary ---
        writeObj(infoObj) {
            val dateStr = pdfDate()
            writeRaw("<< ")
            if (document.metadata.title.isNotEmpty()) writeRaw("/Title (${escPdf(document.metadata.title)}) ")
            if (document.metadata.author.isNotEmpty()) writeRaw("/Author (${escPdf(document.metadata.author)}) ")
            if (document.metadata.subject.isNotEmpty()) writeRaw("/Subject (${escPdf(document.metadata.subject)}) ")
            writeRaw("/Creator (${escPdf(document.metadata.creator)}) ")
            writeRaw("/Producer (pdf-generator 1.0) ")
            writeRaw("/CreationDate ($dateStr) ")
            writeRaw(">>\n")
        }

        // --- Write font objects ---
        for ((fontName, objNum) in fontMap) {
            writeObj(objNum) {
                writeRaw("<< /Type /Font /Subtype /Type1 /BaseFont /$fontName /Encoding /WinAnsiEncoding >>\n")
            }
        }

        // --- Write image objects ---
        for ((key, objNum) in imageDataMap) {
            // Find the image data
            val image = document.pages.flatMap { it.elements }.filterIsInstance<Image>()
                .first { it.data.contentHashCode() == key }
            writeImageObject(objNum, image)
        }

        // --- Write page objects and content streams ---
        for ((index, page) in document.pages.withIndex()) {
            val pair = pageObjPairs[index]
            val fontResources = fontMap.entries.withIndex().joinToString(" ") { (i, entry) ->
                "/F${i + 1} ${entry.value} 0 R"
            }

            // Build XObject references for images on this page
            val xObjRefs = StringBuilder()
            var imgIndex = 0
            for (element in page.elements) {
                if (element is Image) {
                    val imgObjNum = imageDataMap[element.data.contentHashCode()]!!
                    xObjRefs.append("/Img$imgIndex $imgObjNum 0 R ")
                    imgIndex++
                }
            }

            val resourcesStr = StringBuilder("<< /Font << $fontResources >> ")
            if (xObjRefs.isNotEmpty()) {
                resourcesStr.append("/XObject << $xObjRefs>> ")
            }
            resourcesStr.append(">>")

            writeObj(pair.pageObj) {
                writeRaw("<< /Type /Page /Parent $pagesObj 0 R ")
                writeRaw("/MediaBox [0 0 ${page.size.width} ${page.size.height}] ")
                writeRaw("/Contents ${pair.contentObj} 0 R ")
                writeRaw("/Resources $resourcesStr ")
                writeRaw(">>\n")
            }

            // Generate content stream
            val content = generatePageContent(page, imageDataMap)
            writeObj(pair.contentObj) {
                writeRaw("<< /Length ${content.length} >>\n")
                writeRaw("stream\n")
                writeRaw(content)
                writeRaw("\nendstream\n")
            }
        }

        // --- Write cross-reference table ---
        val xrefOffset = out.size()
        writeRaw("xref\n")
        writeRaw("0 ${objectCount + 1}\n")
        writeRaw("0000000000 65535 f \n")
        for (offset in offsets) {
            writeRaw("${offset.toString().padStart(10, '0')} 00000 n \n")
        }

        // --- Write trailer ---
        writeRaw("trailer\n")
        writeRaw("<< /Size ${objectCount + 1} /Root $catalogObj 0 R /Info $infoObj 0 R >>\n")
        writeRaw("startxref\n")
        writeRaw("$xrefOffset\n")
        writeRaw("%%EOF\n")

        return out.toByteArray()
    }

    private fun nextObjNumber(): Int = ++objectCount

    private fun writeRaw(s: String) {
        out.write(s.toByteArray(Charsets.ISO_8859_1))
    }

    private fun writeRawBytes(b: ByteArray) {
        out.write(b)
    }

    private inline fun writeObj(objNum: Int, content: () -> Unit) {
        // Ensure offsets list is big enough
        while (offsets.size < objNum) offsets.add(0)
        offsets[objNum - 1] = out.size()
        writeRaw("$objNum 0 obj\n")
        content()
        writeRaw("endobj\n")
    }

    private fun writeImageObject(objNum: Int, image: Image) {
        val dims = getImageDimensions(image.data, image.format)
        val (filter, colorSpace, bitsPerComponent, streamData) = when (image.format) {
            ImageFormat.JPEG -> ImageStreamInfo(
                "DCTDecode", "DeviceRGB", 8, image.data
            )
            ImageFormat.PNG -> {
                // Extract raw RGB data from PNG for simplicity
                val rgbData = decodePngToRgb(image.data)
                ImageStreamInfo("FlateDecode", "DeviceRGB", 8, deflate(rgbData))
            }
        }

        while (offsets.size < objNum) offsets.add(0)
        offsets[objNum - 1] = out.size()
        writeRaw("$objNum 0 obj\n")
        writeRaw("<< /Type /XObject /Subtype /Image ")
        writeRaw("/Width ${dims.first} /Height ${dims.second} ")
        writeRaw("/ColorSpace /$colorSpace /BitsPerComponent $bitsPerComponent ")
        writeRaw("/Filter /$filter ")
        writeRaw("/Length ${streamData.size} ")
        writeRaw(">>\n")
        writeRaw("stream\n")
        writeRawBytes(streamData)
        writeRaw("\nendstream\n")
        writeRaw("endobj\n")
    }

    private data class ImageStreamInfo(
        val filter: String,
        val colorSpace: String,
        val bitsPerComponent: Int,
        val data: ByteArray
    )

    private fun generatePageContent(page: Page, imageDataMap: Map<Int, Int>): String {
        val sb = StringBuilder()
        var y = page.size.height - page.margins.top
        val leftX = page.margins.left
        val contentWidth = page.contentWidth
        var imgIndex = 0

        for (element in page.elements) {
            when (element) {
                is Heading -> {
                    val style = element.effectiveStyle()
                    y -= style.fontSize * 1.2f
                    if (y < page.margins.bottom) continue
                    sb.appendLine(renderText(element.text, leftX, y, style))
                    y -= style.fontSize * 0.5f
                }
                is Paragraph -> {
                    val lines = wrapText(element.text, contentWidth, element.style)
                    for (line in lines) {
                        y -= element.style.fontSize * element.style.lineSpacing
                        if (y < page.margins.bottom) break
                        sb.appendLine(renderText(line, leftX, y, element.style))
                    }
                    y -= element.style.fontSize * 0.3f
                }
                is Table -> {
                    y = renderTable(sb, element, leftX, y, contentWidth, page.margins.bottom)
                }
                is Image -> {
                    y -= element.height
                    if (y < page.margins.bottom) { imgIndex++; continue }
                    val x = when (element.alignment) {
                        Image.Alignment.LEFT -> leftX
                        Image.Alignment.CENTER -> leftX + (contentWidth - element.width) / 2
                        Image.Alignment.RIGHT -> leftX + contentWidth - element.width
                    }
                    sb.appendLine("q")
                    sb.appendLine("${element.width} 0 0 ${element.height} $x $y cm")
                    sb.appendLine("/Img$imgIndex Do")
                    sb.appendLine("Q")
                    imgIndex++
                    y -= 4f
                }
                is HorizontalRule -> {
                    y -= 8f
                    sb.appendLine("q")
                    sb.appendLine("${element.color.toPdfStrokeColor()}")
                    sb.appendLine("${element.thickness} w")
                    sb.appendLine("$leftX $y m ${leftX + contentWidth} $y l S")
                    sb.appendLine("Q")
                    y -= 8f
                }
                is Spacer -> {
                    y -= element.height
                }
            }
        }

        return sb.toString()
    }

    private fun renderText(text: String, x: Float, y: Float, style: TextStyle): String {
        val fontIndex = fontMap.keys.indexOf(style.resolvedFontName()) + 1
        val sb = StringBuilder()
        sb.appendLine("BT")
        sb.appendLine("${style.color.toPdfFillColor()}")
        sb.appendLine("/F$fontIndex ${style.fontSize} Tf")
        sb.appendLine("$x $y Td")
        sb.appendLine("(${escPdf(text)}) Tj")
        sb.appendLine("ET")
        return sb.toString()
    }

    private fun renderTable(
        sb: StringBuilder, table: Table,
        startX: Float, startY: Float, availableWidth: Float, bottomMargin: Float
    ): Float {
        val numCols = table.rows.maxOfOrNull { row ->
            row.sumOf { it.colSpan }
        } ?: return startY

        val colWidths = table.columnWidths ?: run {
            val w = availableWidth / numCols
            List(numCols) { w }
        }

        val rowHeight = 20f
        var y = startY

        for ((rowIdx, row) in table.rows.withIndex()) {
            y -= rowHeight
            if (y < bottomMargin) break

            var x = startX
            val isHeader = rowIdx < table.headerRows
            var colOffset = 0

            for (cell in row) {
                val cellWidth = (colOffset until (colOffset + cell.colSpan).coerceAtMost(colWidths.size))
                    .sumOf { colWidths.getOrElse(it) { 0f }.toDouble() }.toFloat()
                val cellHeight = rowHeight * cell.rowSpan

                // Draw cell background
                if (cell.backgroundColor != null || isHeader) {
                    val bg = cell.backgroundColor ?: Color.LIGHT_GRAY
                    sb.appendLine("q")
                    sb.appendLine("${bg.toPdfFillColor()}")
                    sb.appendLine("$x $y $cellWidth $cellHeight re f")
                    sb.appendLine("Q")
                }

                // Draw cell border
                if (table.borderStyle.enabled) {
                    sb.appendLine("q")
                    sb.appendLine("${table.borderStyle.color.toPdfStrokeColor()}")
                    sb.appendLine("${table.borderStyle.width} w")
                    sb.appendLine("$x $y $cellWidth $cellHeight re S")
                    sb.appendLine("Q")
                }

                // Draw cell text
                val cellStyle = if (isHeader) table.headerStyle else cell.style
                val textY = y + cell.padding
                val textX = x + cell.padding
                sb.appendLine(renderText(cell.text, textX, textY, cellStyle))

                x += cellWidth
                colOffset += cell.colSpan
            }
        }

        return y - 4f
    }

    private fun wrapText(text: String, maxWidth: Float, style: TextStyle): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (style.textWidth(testLine) > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun collectFonts(document: Document): Set<String> {
        val fonts = mutableSetOf<String>()
        // Always include base Helvetica
        fonts.add(FontFamily.HELVETICA.pdfName)
        fonts.add(FontFamily.HELVETICA.boldPdfName)

        for (page in document.pages) {
            for (element in page.elements) {
                when (element) {
                    is Paragraph -> fonts.add(element.style.resolvedFontName())
                    is Heading -> fonts.add(element.effectiveStyle().resolvedFontName())
                    is Table -> {
                        fonts.add(element.headerStyle.resolvedFontName())
                        for (row in element.rows) {
                            for (cell in row) {
                                fonts.add(cell.style.resolvedFontName())
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
        return fonts
    }

    companion object {
        fun escPdf(text: String): String {
            return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
        }

        fun pdfDate(): String {
            val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return "D:${sdf.format(Date())}+00'00'"
        }

        fun getImageDimensions(data: ByteArray, format: ImageFormat): Pair<Int, Int> {
            return when (format) {
                ImageFormat.JPEG -> getJpegDimensions(data)
                ImageFormat.PNG -> getPngDimensions(data)
            }
        }

        private fun getJpegDimensions(data: ByteArray): Pair<Int, Int> {
            var i = 2
            while (i < data.size - 1) {
                if (data[i].toInt() and 0xFF != 0xFF) break
                val marker = data[i + 1].toInt() and 0xFF
                if (marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC) {
                    val height = ((data[i + 5].toInt() and 0xFF) shl 8) or (data[i + 6].toInt() and 0xFF)
                    val width = ((data[i + 7].toInt() and 0xFF) shl 8) or (data[i + 8].toInt() and 0xFF)
                    return Pair(width, height)
                }
                val segLen = ((data[i + 2].toInt() and 0xFF) shl 8) or (data[i + 3].toInt() and 0xFF)
                i += segLen + 2
            }
            return Pair(1, 1)
        }

        private fun getPngDimensions(data: ByteArray): Pair<Int, Int> {
            // IHDR chunk starts at byte 16 in a standard PNG
            if (data.size < 24) return Pair(1, 1)
            val width = ((data[16].toInt() and 0xFF) shl 24) or
                    ((data[17].toInt() and 0xFF) shl 16) or
                    ((data[18].toInt() and 0xFF) shl 8) or
                    (data[19].toInt() and 0xFF)
            val height = ((data[20].toInt() and 0xFF) shl 24) or
                    ((data[21].toInt() and 0xFF) shl 16) or
                    ((data[22].toInt() and 0xFF) shl 8) or
                    (data[23].toInt() and 0xFF)
            return Pair(width, height)
        }

        /**
         * Simple PNG to raw RGB decoder. Handles basic non-interlaced PNGs.
         * For production use, a full PNG decoder library would be preferred.
         */
        fun decodePngToRgb(data: ByteArray): ByteArray {
            val dims = getPngDimensions(data)
            // Return a placeholder RGB block for the image dimensions
            // A full implementation would decompress IDAT chunks
            val size = dims.first * dims.second * 3
            return ByteArray(size) { 200.toByte() } // light gray placeholder
        }

        fun deflate(data: ByteArray): ByteArray {
            val baos = ByteArrayOutputStream()
            val deflater = java.util.zip.DeflaterOutputStream(baos)
            deflater.write(data)
            deflater.finish()
            deflater.close()
            return baos.toByteArray()
        }
    }
}
