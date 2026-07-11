package pdf

/**
 * Thrown when a JSON document cannot be parsed.
 */
class JsonParseException(message: String) : Exception(message)

/**
 * Minimal JSON parser with no external dependencies.
 *
 * Parses a JSON document into plain Kotlin values:
 * - objects  -> Map<String, Any?>
 * - arrays   -> List<Any?>
 * - strings  -> String
 * - numbers  -> Long or Double
 * - booleans -> Boolean
 * - null     -> null
 *
 * This is intentionally small: just enough to accept request payloads
 * for the HTTP service without adding a JSON library dependency.
 */
object Json {

    /**
     * Parses [text] and returns the root value.
     *
     * @throws JsonParseException if the input is not valid JSON.
     */
    fun parse(text: String): Any? {
        val parser = Parser(text)
        val value = parser.parseValue()
        parser.skipWhitespace()
        if (!parser.atEnd()) {
            throw JsonParseException("Unexpected trailing characters at position ${parser.pos}")
        }
        return value
    }

    /**
     * Parses [text] expecting a JSON object at the root.
     *
     * @throws JsonParseException if the input is not valid JSON or the root is not an object.
     */
    fun parseObject(text: String): Map<String, Any?> {
        val value = parse(text)
        if (value !is Map<*, *>) {
            throw JsonParseException("Expected a JSON object at the root")
        }
        @Suppress("UNCHECKED_CAST")
        return value as Map<String, Any?>
    }

    private class Parser(private val text: String) {
        var pos = 0

        fun atEnd(): Boolean = pos >= text.length

        fun skipWhitespace() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }

        private fun peek(): Char {
            if (atEnd()) throw JsonParseException("Unexpected end of input")
            return text[pos]
        }

        private fun expect(c: Char) {
            if (atEnd() || text[pos] != c) {
                throw JsonParseException("Expected '$c' at position $pos")
            }
            pos++
        }

        fun parseValue(): Any? {
            skipWhitespace()
            return when (peek()) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't' -> parseLiteral("true", true)
                'f' -> parseLiteral("false", false)
                'n' -> parseLiteral("null", null)
                else -> parseNumber()
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            val result = LinkedHashMap<String, Any?>()
            skipWhitespace()
            if (!atEnd() && peek() == '}') {
                pos++
                return result
            }
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                result[key] = parseValue()
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    '}' -> {
                        pos++
                        return result
                    }
                    else -> throw JsonParseException("Expected ',' or '}' at position $pos")
                }
            }
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            val result = mutableListOf<Any?>()
            skipWhitespace()
            if (!atEnd() && peek() == ']') {
                pos++
                return result
            }
            while (true) {
                result.add(parseValue())
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    ']' -> {
                        pos++
                        return result
                    }
                    else -> throw JsonParseException("Expected ',' or ']' at position $pos")
                }
            }
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                if (atEnd()) throw JsonParseException("Unterminated string")
                when (val c = text[pos++]) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (atEnd()) throw JsonParseException("Unterminated escape sequence")
                        when (val esc = text[pos++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (pos + 4 > text.length) throw JsonParseException("Invalid unicode escape")
                                val hex = text.substring(pos, pos + 4)
                                val code = hex.toIntOrNull(16)
                                    ?: throw JsonParseException("Invalid unicode escape '\\u$hex'")
                                sb.append(code.toChar())
                                pos += 4
                            }
                            else -> throw JsonParseException("Invalid escape character '\\$esc'")
                        }
                    }
                    else -> {
                        if (c.code < 0x20) throw JsonParseException("Unescaped control character in string")
                        sb.append(c)
                    }
                }
            }
        }

        private fun parseNumber(): Any {
            val start = pos
            if (!atEnd() && text[pos] == '-') pos++
            while (!atEnd() && (text[pos].isDigit() || text[pos] in "+-.eE")) pos++
            val raw = text.substring(start, pos)
            if (raw.isEmpty()) throw JsonParseException("Unexpected character at position $start")
            return raw.toLongOrNull()
                ?: raw.toDoubleOrNull()
                ?: throw JsonParseException("Invalid number '$raw' at position $start")
        }

        private fun <T> parseLiteral(literal: String, value: T): T {
            if (!text.startsWith(literal, pos)) {
                throw JsonParseException("Unexpected token at position $pos")
            }
            pos += literal.length
            return value
        }
    }
}
