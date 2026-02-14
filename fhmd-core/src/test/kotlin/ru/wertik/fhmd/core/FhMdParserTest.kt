package ru.wertik.fhmd.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FhMdParserTest {

    @Test
    fun `default cache key uses parser class`() {
        val parser = FhMdParser { FhMdDocument(emptyList()) }

        assertEquals(parser::class, parser.cacheKey())
    }

    @Test
    fun `custom parser may provide custom cache key`() {
        val expectedKey = "custom-parser-v2"
        val parser = object : FhMdParser {
            override fun parse(input: String): FhMdDocument = FhMdDocument(emptyList())

            override fun cacheKey(): Any = expectedKey
        }

        assertSame(expectedKey, parser.cacheKey())
    }
}
