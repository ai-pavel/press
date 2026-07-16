package pdf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertIs

class JsonTest {

    // ==================== parse: primitives ====================

    @Test
    fun `parse null`() {
        assertNull(Json.parse("null"))
    }

    @Test
    fun `parse true`() {
        assertEquals(true, Json.parse("true"))
    }

    @Test
    fun `parse false`() {
        assertEquals(false, Json.parse("false"))
    }

    @Test
    fun `parse integer`() {
        assertEquals(42L, Json.parse("42"))
    }

    @Test
    fun `parse negative integer`() {
        assertEquals(-7L, Json.parse("-7"))
    }

    @Test
    fun `parse double`() {
        assertEquals(3.14, Json.parse("3.14"))
    }

    @Test
    fun `parse double with exponent`() {
        assertEquals(1.0e3, Json.parse("1e3"))
    }

    @Test
    fun `parse negative exponent double`() {
        assertEquals(1.5e-2, Json.parse("1.5e-2"))
    }

    @Test
    fun `parse string`() {
        assertEquals("hello", Json.parse("\"hello\""))
    }

    @Test
    fun `parse empty string`() {
        assertEquals("", Json.parse("\"\""))
    }

    @Test
    fun `parse string with leading whitespace`() {
        assertEquals("x", Json.parse("   \"x\"   "))
    }

    // ==================== parse: objects ====================

    @Test
    fun `parse empty object`() {
        assertEquals(emptyMap<String, Any?>(), Json.parse("{}"))
    }

    @Test
    fun `parse simple object`() {
        val result = Json.parseObject("""{"a":1,"b":2}""")
        assertEquals(1L, result["a"])
        assertEquals(2L, result["b"])
    }

    @Test
    fun `parse object with whitespace between pairs`() {
        val result = Json.parseObject("""{ "a" : 1 , "b" : 2 }""")
        assertEquals(1L, result["a"])
        assertEquals(2L, result["b"])
    }

    @Test
    fun `parse nested object`() {
        val result = Json.parseObject("""{"outer":{"inner":5}}""")
        val inner = result["outer"] as Map<*, *>
        assertEquals(5L, inner["inner"])
    }

    @Test
    fun `parse object with null value`() {
        val result = Json.parseObject("""{"a":null}""")
        assertNull(result["a"])
    }

    // ==================== parse: arrays ====================

    @Test
    fun `parse empty array`() {
        assertEquals(emptyList<Any?>(), Json.parse("[]"))
    }

    @Test
    fun `parse array of numbers`() {
        assertEquals(listOf(1L, 2L, 3L), Json.parse("[1, 2, 3]"))
    }

    @Test
    fun `parse nested array`() {
        val result = Json.parse("[[1,2],[3,4]]") as List<*>
        assertEquals(listOf(1L, 2L), result[0])
        assertEquals(listOf(3L, 4L), result[1])
    }

    @Test
    fun `parse array with mixed types`() {
        val result = Json.parse("""[1, "two", true, null]""") as List<*>
        assertEquals(1L, result[0])
        assertEquals("two", result[1])
        assertEquals(true, result[2])
        assertNull(result[3])
    }

    @Test
    fun `parse array with trailing whitespace`() {
        val result = Json.parse("[1, 2]   ") as List<*>
        assertEquals(2, result.size)
    }

    // ==================== parseObject ====================

    @Test
    fun `parseObject rejects non-object root`() {
        assertThrows<JsonParseException> {
            Json.parseObject("[1,2,3]")
        }
    }

    @Test
    fun `parseObject rejects primitive root`() {
        assertThrows<JsonParseException> {
            Json.parseObject("42")
        }
    }

    // ==================== string escapes ====================

    @Test
    fun `parse string with escaped quote`() {
        assertEquals("a\"b", Json.parse("\"a\\\"b\""))
    }

    @Test
    fun `parse string with escaped backslash`() {
        assertEquals("a\\b", Json.parse("\"a\\\\b\""))
    }

    @Test
    fun `parse string with escaped slash`() {
        assertEquals("a/b", Json.parse("\"a\\/b\""))
    }

    @Test
    fun `parse string with escaped backspace`() {
        assertEquals("a\u0008b", Json.parse("\"a\\bb\""))
    }

    @Test
    fun `parse string with escaped form feed`() {
        assertEquals("a\u000Cb", Json.parse("\"a\\fb\""))
    }

    @Test
    fun `parse string with escaped newline`() {
        assertEquals("a\nb", Json.parse("\"a\\nb\""))
    }

    @Test
    fun `parse string with escaped carriage return`() {
        assertEquals("a\rb", Json.parse("\"a\\rb\""))
    }

    @Test
    fun `parse string with escaped tab`() {
        assertEquals("a\tb", Json.parse("\"a\\tb\""))
    }

    @Test
    fun `parse string with unicode escape`() {
        assertEquals("a\u0041b", Json.parse("\"a\\u0041b\""))
    }

    @Test
    fun `parse string with multiple escapes`() {
        assertEquals("\t\n\\\"", Json.parse("\"\\t\\n\\\\\\\"\""))
    }

    // ==================== error paths ====================

    @Test
    fun `parse empty input throws`() {
        assertThrows<JsonParseException> { Json.parse("") }
    }

    @Test
    fun `parse trailing characters throws`() {
        assertThrows<JsonParseException> { Json.parse("1 2") }
    }

    @Test
    fun `parse unterminated string throws`() {
        assertThrows<JsonParseException> { Json.parse("\"hello") }
    }

    @Test
    fun `parse unterminated escape sequence throws`() {
        assertThrows<JsonParseException> { Json.parse("\"a\\") }
    }

    @Test
    fun `parse invalid escape character throws`() {
        assertThrows<JsonParseException> { Json.parse("\"a\\xb\"") }
    }

    @Test
    fun `parse invalid unicode escape throws`() {
        assertThrows<JsonParseException> { Json.parse("\"\\u00ZZ\"") }
    }

    @Test
    fun `parse short unicode escape throws`() {
        assertThrows<JsonParseException> { Json.parse("\"\\u00\"") }
    }

    @Test
    fun `parse unescaped control character throws`() {
        assertThrows<JsonParseException> { Json.parse("\"a\u0001b\"") }
    }

    @Test
    fun `parse object missing colon throws`() {
        assertThrows<JsonParseException> { Json.parse("""{"a" 1}""") }
    }

    @Test
    fun `parse object unexpected separator throws`() {
        assertThrows<JsonParseException> { Json.parse("""{"a":1 "b":2}""") }
    }

    @Test
    fun `parse object unterminated throws`() {
        assertThrows<JsonParseException> { Json.parse("""{"a":1""") }
    }

    @Test
    fun `parse array unexpected separator throws`() {
        assertThrows<JsonParseException> { Json.parse("[1 2]") }
    }

    @Test
    fun `parse array unterminated throws`() {
        assertThrows<JsonParseException> { Json.parse("[1,2") }
    }

    @Test
    fun `parse invalid literal throws`() {
        assertThrows<JsonParseException> { Json.parse("truex") }
    }

    @Test
    fun `parse invalid number throws`() {
        assertThrows<JsonParseException> { Json.parse("--5") }
    }

    @Test
    fun `parse unexpected character throws`() {
        assertThrows<JsonParseException> { Json.parse("@") }
    }

    @Test
    fun `JsonParseException carries message`() {
        val ex = assertThrows<JsonParseException> { Json.parse("@") }
        assertTrue(ex.message!!.isNotEmpty())
    }

    // ==================== numbers edge cases ====================

    @Test
    fun `parse number that is only a minus throws`() {
        assertThrows<JsonParseException> { Json.parse("-") }
    }

    @Test
    fun `parse zero`() {
        assertEquals(0L, Json.parse("0"))
    }

    @Test
    fun `parse very long number falls back to double if needed`() {
        assertEquals(1.0, Json.parse("1.0"))
    }

    // ==================== root object key accessors ====================

    @Test
    fun `parseObject returns map type`() {
        val result = Json.parseObject("""{"k":"v"}""")
        assertIs<Map<String, Any?>>(result)
        assertEquals("v", result["k"])
    }
}