package pdf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ImageTest {

    @Test
    fun `fromBytes creates image with correct properties`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val image = Image.fromBytes(data, ImageFormat.JPEG, 100f, 50f)
        assertEquals(ImageFormat.JPEG, image.format)
        assertEquals(100f, image.width)
        assertEquals(50f, image.height)
        assertEquals(Image.Alignment.LEFT, image.alignment)
        assertTrue(data.contentEquals(image.data))
    }

    @Test
    fun `fromBytes with alignment`() {
        val data = byteArrayOf(10, 20, 30)
        val image = Image.fromBytes(data, ImageFormat.PNG, 200f, 150f, Image.Alignment.CENTER)
        assertEquals(Image.Alignment.CENTER, image.alignment)
        assertEquals(200f, image.width)
        assertEquals(150f, image.height)
    }

    @Test
    fun `fromBytes with RIGHT alignment`() {
        val data = byteArrayOf(10, 20, 30)
        val image = Image.fromBytes(data, ImageFormat.PNG, 200f, 150f, Image.Alignment.RIGHT)
        assertEquals(Image.Alignment.RIGHT, image.alignment)
    }

    @Test
    fun `fromFile with PNG extension`(@TempDir tempDir: File) {
        val pngFile = File(tempDir, "test.png")
        val testData = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 1, 2, 3)
        pngFile.writeBytes(testData)

        val image = Image.fromFile(pngFile, 100f, 80f)
        assertEquals(ImageFormat.PNG, image.format)
        assertEquals(100f, image.width)
        assertEquals(80f, image.height)
        assertTrue(testData.contentEquals(image.data))
    }

    @Test
    fun `fromFile with JPG extension`(@TempDir tempDir: File) {
        val jpgFile = File(tempDir, "test.jpg")
        val testData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3)
        jpgFile.writeBytes(testData)

        val image = Image.fromFile(jpgFile, 120f, 90f)
        assertEquals(ImageFormat.JPEG, image.format)
    }

    @Test
    fun `fromFile with JPEG extension`(@TempDir tempDir: File) {
        val jpegFile = File(tempDir, "test.jpeg")
        val testData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3)
        jpegFile.writeBytes(testData)

        val image = Image.fromFile(jpegFile, 120f, 90f)
        assertEquals(ImageFormat.JPEG, image.format)
    }

    @Test
    fun `fromFile with unsupported extension throws`(@TempDir tempDir: File) {
        val bmpFile = File(tempDir, "test.bmp")
        bmpFile.writeBytes(byteArrayOf(1, 2, 3))

        assertThrows<IllegalArgumentException> {
            Image.fromFile(bmpFile, 100f, 80f)
        }
    }

    @Test
    fun `fromFile with alignment`(@TempDir tempDir: File) {
        val pngFile = File(tempDir, "test.png")
        pngFile.writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 1, 2, 3))

        val image = Image.fromFile(pngFile, 100f, 80f, Image.Alignment.RIGHT)
        assertEquals(Image.Alignment.RIGHT, image.alignment)
    }

    @Test
    fun `equals returns true for identical images`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.LEFT)
        val img2 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.LEFT)
        assertEquals(img1, img2)
    }

    @Test
    fun `equals returns true for same instance`() {
        val data = byteArrayOf(1, 2, 3)
        val img = Image(data, ImageFormat.PNG, 100f, 80f)
        @Suppress("SelfEquals")
        assertTrue(img.equals(img))
    }

    @Test
    fun `equals returns false for different data`() {
        val img1 = Image(byteArrayOf(1, 2, 3), ImageFormat.PNG, 100f, 80f)
        val img2 = Image(byteArrayOf(4, 5, 6), ImageFormat.PNG, 100f, 80f)
        assertNotEquals(img1, img2)
    }

    @Test
    fun `equals returns false for different format`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f)
        val img2 = Image(data.copyOf(), ImageFormat.JPEG, 100f, 80f)
        assertNotEquals(img1, img2)
    }

    @Test
    fun `equals returns false for different width`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f)
        val img2 = Image(data.copyOf(), ImageFormat.PNG, 200f, 80f)
        assertNotEquals(img1, img2)
    }

    @Test
    fun `equals returns false for different height`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f)
        val img2 = Image(data.copyOf(), ImageFormat.PNG, 100f, 160f)
        assertNotEquals(img1, img2)
    }

    @Test
    fun `equals returns false for different alignment`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.LEFT)
        val img2 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.CENTER)
        assertNotEquals(img1, img2)
    }

    @Test
    fun `equals returns false for non-Image type`() {
        val img = Image(byteArrayOf(1, 2, 3), ImageFormat.PNG, 100f, 80f)
        assertFalse(img.equals("not an image"))
    }

    @Test
    fun `equals returns false for null`() {
        val img = Image(byteArrayOf(1, 2, 3), ImageFormat.PNG, 100f, 80f)
        assertFalse(img.equals(null))
    }

    @Test
    fun `hashCode is consistent for equal images`() {
        val data = byteArrayOf(1, 2, 3)
        val img1 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.LEFT)
        val img2 = Image(data.copyOf(), ImageFormat.PNG, 100f, 80f, Image.Alignment.LEFT)
        assertEquals(img1.hashCode(), img2.hashCode())
    }

    @Test
    fun `hashCode differs for different images`() {
        val img1 = Image(byteArrayOf(1, 2, 3), ImageFormat.PNG, 100f, 80f)
        val img2 = Image(byteArrayOf(4, 5, 6), ImageFormat.JPEG, 200f, 160f)
        // Not guaranteed but very likely
        assertNotEquals(img1.hashCode(), img2.hashCode())
    }

    @Test
    fun `ImageFormat enum values`() {
        assertEquals(2, ImageFormat.values().size)
        assertEquals(ImageFormat.PNG, ImageFormat.valueOf("PNG"))
        assertEquals(ImageFormat.JPEG, ImageFormat.valueOf("JPEG"))
    }

    @Test
    fun `Image Alignment enum values`() {
        assertEquals(3, Image.Alignment.values().size)
        assertEquals(Image.Alignment.LEFT, Image.Alignment.valueOf("LEFT"))
        assertEquals(Image.Alignment.CENTER, Image.Alignment.valueOf("CENTER"))
        assertEquals(Image.Alignment.RIGHT, Image.Alignment.valueOf("RIGHT"))
    }
}
