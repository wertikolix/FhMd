package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrcaParseDiagnosticsTest {

    @Test
    fun emptyDiagnosticsHasNoWarningsAndNoErrors() {
        val diag = OrcaParseDiagnostics()

        assertFalse(diag.hasWarnings)
        assertFalse(diag.hasErrors)
        assertTrue(diag.warnings.isEmpty())
        assertTrue(diag.errors.isEmpty())
    }

    @Test
    fun diagnosticsWithWarningReportsHasWarnings() {
        val diag = OrcaParseDiagnostics(
            warnings = listOf(OrcaParseWarning.DepthLimitExceeded(maxTreeDepth = 8, exceededDepth = 12)),
        )

        assertTrue(diag.hasWarnings)
        assertFalse(diag.hasErrors)
    }

    @Test
    fun diagnosticsWithErrorReportsHasErrors() {
        val diag = OrcaParseDiagnostics(
            errors = listOf(OrcaParseError.ParserFailure(message = "boom")),
        )

        assertFalse(diag.hasWarnings)
        assertTrue(diag.hasErrors)
    }

    @Test
    fun diagnosticsWithBothWarningsAndErrors() {
        val diag = OrcaParseDiagnostics(
            warnings = listOf(OrcaParseWarning.DepthLimitExceeded(maxTreeDepth = 8, exceededDepth = 10)),
            errors = listOf(OrcaParseError.ParserFailure(message = "error")),
        )

        assertTrue(diag.hasWarnings)
        assertTrue(diag.hasErrors)
    }

    @Test
    fun parseResultDefaultDiagnosticsAreEmpty() {
        val result = OrcaParseResult(document = OrcaDocument(emptyList()))

        assertFalse(result.diagnostics.hasWarnings)
        assertFalse(result.diagnostics.hasErrors)
    }

    @Test
    fun depthLimitExceededWarningCarriesCorrectValues() {
        val warning = OrcaParseWarning.DepthLimitExceeded(maxTreeDepth = 16, exceededDepth = 20)

        assertEquals(16, warning.maxTreeDepth)
        assertEquals(20, warning.exceededDepth)
    }

    @Test
    fun parserFailureErrorCarriesMessage() {
        val error = OrcaParseError.ParserFailure(message = "unexpected token")

        assertEquals("unexpected token", error.message)
    }

    @Test
    fun parseWithDiagnosticsProducesCleanDiagnosticsForValidInput() {
        val parser = OrcaMarkdownParser()

        val result = parser.parseWithDiagnostics("# Hello\n\nworld")

        assertFalse(result.diagnostics.hasErrors)
        assertFalse(result.diagnostics.hasWarnings)
        assertEquals(2, result.document.blocks.size)
    }

    @Test
    fun cacheKeyDiffersForDifferentMaxTreeDepth() {
        val parser1 = OrcaMarkdownParser(maxTreeDepth = 32)
        val parser2 = OrcaMarkdownParser(maxTreeDepth = 64)

        assertFalse(parser1.cacheKey() == parser2.cacheKey())
    }
}
