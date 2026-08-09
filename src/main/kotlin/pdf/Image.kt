package pdf

import java.io.File

/**
 * Supported image formats.
 */
enum class ImageFormat {
    PNG, JPEG
}

/**
 * An image element to embed in the PDF.
 */
data class Image(
    val data: ByteArray,
    val format: ImageFormat,
    val width: Float,
    val height: Float,
    val alignment: Alignment = Alignment.LEFT
) : Element {

    enum class Alignment { LEFT, CENTER, RIGHT }

    companion object {
        fun fromFile(file: File, width: Float, height: Float, alignment: Alignment = Alignment.LEFT): Image {
            val format = when (file.extension.lowercase()) {
                "png" -> ImageFormat.PNG
                "jpg", "jpeg" -> ImageFormat.JPEG
                else -> throw IllegalArgumentException("Unsupported image format: ${file.extension}")
            }
            return Image(file.readBytes(), format, width, height, alignment)
        }

        fun fromBytes(data: ByteArray, format: ImageFormat, width: Float, height: Float, alignment: Alignment = Alignment.LEFT): Image {
            return Image(data, format, width, height, alignment)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false
        return data.contentEquals(other.data) && format == other.format &&
                width == other.width && height == other.height && alignment == other.alignment
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + alignment.hashCode()
        return result
    }
}
