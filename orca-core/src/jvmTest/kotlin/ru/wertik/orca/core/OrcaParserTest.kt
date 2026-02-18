package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OrcaParserTest {

    @Test
    fun `default cache key uses parser class`() {
        val parser = OrcaParser { OrcaDocument(emptyList()) }

        assertEquals(parser::class, parser.cacheKey())
    }

    @Test
    fun `custom parser may provide custom cache key`() {
        val expectedKey = "custom-parser-v2"
        val parser = object : OrcaParser {
            override fun parse(input: String): OrcaDocument = OrcaDocument(emptyList())

            override fun cacheKey(): Any = expectedKey
        }

        assertSame(expectedKey, parser.cacheKey())
    }

    @Test
    fun `default parseWithDiagnostics wraps parse result`() {
        val parser = OrcaParser { OrcaDocument(emptyList()) }

        val result = parser.parseWithDiagnostics("hello")

        assertTrue(result.document.blocks.isEmpty())
        assertTrue(result.diagnostics.warnings.isEmpty())
        assertTrue(result.diagnostics.errors.isEmpty())
    }

    @Test
    fun defaultParseCachedWithDiagnosticsDoesNotCache() {
        val parser = OrcaParser { input ->
            OrcaDocument(
                blocks = listOf(OrcaBlock.Paragraph(content = listOf(OrcaInline.Text(input)))),
            )
        }

        val first = parser.parseCachedWithDiagnostics("key", "hello")
        val second = parser.parseCachedWithDiagnostics("key", "hello")

        // Default implementation does not cache — returns new instances each time
        assertEquals(first.document, second.document)
        assertFalse(first === second)
    }

    @Test
    fun defaultParseCachedDoesNotCache() {
        val parser = OrcaParser { input ->
            OrcaDocument(
                blocks = listOf(OrcaBlock.Paragraph(content = listOf(OrcaInline.Text(input)))),
            )
        }

        val first = parser.parseCached("key", "hello")
        val second = parser.parseCached("key", "hello")

        assertEquals(first, second)
        assertFalse(first === second)
    }
}
