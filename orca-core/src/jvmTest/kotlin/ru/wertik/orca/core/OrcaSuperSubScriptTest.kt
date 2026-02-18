package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrcaSuperSubScriptTest {

    private val parser = OrcaMarkdownParser()

    @Test
    fun superscriptParsedCorrectly() {
        val result = parser.parse("E = mc^2^")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val sup = paragraph.content.filterIsInstance<OrcaInline.Superscript>()
        assertEquals(1, sup.size)
        val text = (sup.single().content.single() as OrcaInline.Text).text
        assertEquals("2", text)
    }

    @Test
    fun subscriptParsedCorrectly() {
        val result = parser.parse("H~2~O")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val sub = paragraph.content.filterIsInstance<OrcaInline.Subscript>()
        assertEquals(1, sub.size)
        val text = (sub.single().content.single() as OrcaInline.Text).text
        assertEquals("2", text)
    }

    @Test
    fun strikethroughNotAffected() {
        val result = parser.parse("~~deleted~~")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        assertTrue(paragraph.content.any { it is OrcaInline.Strikethrough })
        assertTrue(paragraph.content.none { it is OrcaInline.Subscript })
    }

    @Test
    fun mixedSuperscriptAndSubscript() {
        val result = parser.parse("x^2^ + y~i~")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        assertEquals(1, paragraph.content.filterIsInstance<OrcaInline.Superscript>().size)
        assertEquals(1, paragraph.content.filterIsInstance<OrcaInline.Subscript>().size)
    }
}
