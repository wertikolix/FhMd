package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FootnoteExtractionTest {

    @Test
    fun `no footnotes returns body unchanged and empty definitions`() {
        val input = "# Heading\n\nSome paragraph."
        val result = extractFootnoteDefinitions(input)

        assertEquals("# Heading\n\nSome paragraph.", result.markdown)
        assertTrue(result.definitions.isEmpty())
    }

    @Test
    fun `single footnote definition is extracted`() {
        val input = "Body text.\n\n[^note]: This is the footnote."
        val result = extractFootnoteDefinitions(input)

        assertEquals("Body text.\n", result.markdown)
        assertEquals(1, result.definitions.size)
        val def = result.definitions[0]
        assertEquals("note", def.label)
        assertEquals("This is the footnote.", def.markdown)
    }

    @Test
    fun `multiple footnote definitions are extracted`() {
        val input = "[^a]: First\n[^b]: Second\n[^c]: Third"
        val result = extractFootnoteDefinitions(input)

        assertTrue(result.markdown.isBlank())
        assertEquals(3, result.definitions.size)
        assertEquals("a", result.definitions[0].label)
        assertEquals("First", result.definitions[0].markdown)
        assertEquals("b", result.definitions[1].label)
        assertEquals("Second", result.definitions[1].markdown)
        assertEquals("c", result.definitions[2].label)
        assertEquals("Third", result.definitions[2].markdown)
    }

    @Test
    fun `footnote with 4-space indented continuation lines`() {
        val input = "[^note]: First line\n    continuation line\n    another line"
        val result = extractFootnoteDefinitions(input)

        val def = result.definitions.single()
        assertEquals("note", def.label)
        val content = def.markdown
        assertTrue(content.contains("First line"))
        assertTrue(content.contains("continuation line"))
        assertTrue(content.contains("another line"))
    }

    @Test
    fun `footnote with tab-indented continuation lines`() {
        val input = "[^note]: First line\n\tcontinuation"
        val result = extractFootnoteDefinitions(input)

        val def = result.definitions.single()
        assertEquals("note", def.label)
        assertTrue(def.markdown.contains("First line"))
        assertTrue(def.markdown.contains("continuation"))
    }

    @Test
    fun `footnote with blank continuation line followed by more indented content`() {
        val input = "[^note]: First line\n\n    second paragraph"
        val result = extractFootnoteDefinitions(input)

        val def = result.definitions.single()
        assertEquals("note", def.label)
        assertTrue(def.markdown.contains("First line"))
        assertTrue(def.markdown.contains("second paragraph"))
    }

    @Test
    fun `footnote at end of file without trailing newline`() {
        val input = "Body.\n[^end]: End note"
        val result = extractFootnoteDefinitions(input)

        assertEquals("Body.\n", result.markdown)
        assertEquals(1, result.definitions.size)
        assertEquals("end", result.definitions[0].label)
        assertEquals("End note", result.definitions[0].markdown)
    }

    @Test
    fun `body text before and after footnote definition`() {
        val input = "Before.\n\n[^note]: The note.\n\nAfter."
        val result = extractFootnoteDefinitions(input)

        assertTrue(result.markdown.contains("Before."))
        assertTrue(result.markdown.contains("After."))
        assertEquals(1, result.definitions.size)
        assertEquals("note", result.definitions[0].label)
    }

    @Test
    fun `footnote with empty content`() {
        val input = "[^empty]:"
        val result = extractFootnoteDefinitions(input)

        assertEquals(1, result.definitions.size)
        val def = result.definitions[0]
        assertEquals("empty", def.label)
        assertEquals("", def.markdown)
    }

    @Test
    fun `footnote label is trimmed`() {
        val input = "[^ spaced ]: Content"
        val result = extractFootnoteDefinitions(input)

        assertEquals(1, result.definitions.size)
        assertEquals("spaced", result.definitions[0].label)
    }

    @Test
    fun `footnote body markdown is trimmed of trailing whitespace`() {
        val input = "[^note]: Content   \n    more   "
        val result = extractFootnoteDefinitions(input)

        val def = result.definitions.single()
        // trimEnd() should remove trailing whitespace from the combined content
        assertTrue(!def.markdown.endsWith("   "))
    }

    @Test
    fun `non-footnote lines are preserved in body`() {
        val input = "Line 1\nLine 2\n[^n]: Note\nLine 3"
        val result = extractFootnoteDefinitions(input)

        val bodyLines = result.markdown.split('\n')
        assertTrue(bodyLines.contains("Line 1"))
        assertTrue(bodyLines.contains("Line 2"))
        // Line 3 is not indented so it stops the footnote continuation and goes to body
        assertTrue(bodyLines.contains("Line 3"))
    }
}
