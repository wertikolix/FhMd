package ru.wertik.orca.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OrcaAbbreviationTest {

    private val parser = OrcaMarkdownParser()

    @Test
    fun `abbreviation definitions are extracted and applied`() {
        val markdown = """
            |*[HTML]: Hyper Text Markup Language
            |
            |The HTML specification is maintained by the W3C.
        """.trimMargin()

        val doc = parser.parse(markdown)
        assertEquals(1, doc.blocks.size)
        val paragraph = assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
        val abbr = paragraph.content.filterIsInstance<OrcaInline.Abbreviation>()
        assertEquals(1, abbr.size)
        assertEquals("HTML", abbr[0].text)
        assertEquals("Hyper Text Markup Language", abbr[0].title)
    }

    @Test
    fun `multiple abbreviations`() {
        val markdown = """
            |*[HTML]: Hyper Text Markup Language
            |*[CSS]: Cascading Style Sheets
            |
            |HTML and CSS are web technologies.
        """.trimMargin()

        val doc = parser.parse(markdown)
        assertEquals(1, doc.blocks.size)
        val paragraph = assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
        val abbrs = paragraph.content.filterIsInstance<OrcaInline.Abbreviation>()
        assertEquals(2, abbrs.size)
        assertEquals("HTML", abbrs[0].text)
        assertEquals("CSS", abbrs[1].text)
    }

    @Test
    fun `abbreviation not matched as partial word`() {
        val markdown = """
            |*[JS]: JavaScript
            |
            |The word JSFOO should not match.
        """.trimMargin()

        val doc = parser.parse(markdown)
        assertEquals(1, doc.blocks.size)
        val paragraph = assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
        val abbrs = paragraph.content.filterIsInstance<OrcaInline.Abbreviation>()
        assertEquals(0, abbrs.size)
    }

    @Test
    fun `abbreviation in bold text`() {
        val markdown = """
            |*[API]: Application Programming Interface
            |
            |The **API** is well documented.
        """.trimMargin()

        val doc = parser.parse(markdown)
        assertEquals(1, doc.blocks.size)
        val paragraph = assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
        // The abbreviation should be inside the Bold node.
        val bold = paragraph.content.filterIsInstance<OrcaInline.Bold>()
        assertTrue(bold.isNotEmpty())
        val abbrInBold = bold.flatMap { it.content }.filterIsInstance<OrcaInline.Abbreviation>()
        assertEquals(1, abbrInBold.size)
        assertEquals("API", abbrInBold[0].text)
    }

    @Test
    fun `no abbreviation definitions means no changes`() {
        val markdown = "Just a regular paragraph with no abbreviations."

        val doc = parser.parse(markdown)
        assertEquals(1, doc.blocks.size)
        val paragraph = assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
        assertTrue(paragraph.content.none { it is OrcaInline.Abbreviation })
    }

    @Test
    fun `abbreviation definitions are removed from output`() {
        val markdown = """
            |*[HTML]: Hyper Text Markup Language
            |
            |Some text.
        """.trimMargin()

        val doc = parser.parse(markdown)
        // Should only have the paragraph, not a paragraph with the definition line.
        assertEquals(1, doc.blocks.size)
        assertIs<OrcaBlock.Paragraph>(doc.blocks[0])
    }
}
