package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrcaAdmonitionTest {

    private val parser = OrcaMarkdownParser()

    @Test
    fun noteAdmonitionParsed() {
        val result = parser.parse("> [!NOTE]\n> This is a note.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertEquals(OrcaAdmonitionType.NOTE, admonition.type)
        assertNull(admonition.title)
    }

    @Test
    fun warningAdmonitionParsed() {
        val result = parser.parse("> [!WARNING]\n> Be careful here.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertEquals(OrcaAdmonitionType.WARNING, admonition.type)
    }

    @Test
    fun tipAdmonitionParsed() {
        val result = parser.parse("> [!TIP]\n> A helpful tip.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertEquals(OrcaAdmonitionType.TIP, admonition.type)
    }

    @Test
    fun importantAdmonitionParsed() {
        val result = parser.parse("> [!IMPORTANT]\n> Critical info.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertEquals(OrcaAdmonitionType.IMPORTANT, admonition.type)
    }

    @Test
    fun cautionAdmonitionParsed() {
        val result = parser.parse("> [!CAUTION]\n> Danger zone.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertEquals(OrcaAdmonitionType.CAUTION, admonition.type)
    }

    @Test
    fun regularQuoteNotConvertedToAdmonition() {
        val result = parser.parse("> Just a regular quote.")
        val quote = result.blocks.single() as OrcaBlock.Quote
        assertTrue(quote.blocks.isNotEmpty())
    }

    @Test
    fun admonitionWithContentBlocks() {
        val result = parser.parse("> [!NOTE]\n> First paragraph.\n>\n> Second paragraph.")
        val admonition = result.blocks.single() as OrcaBlock.Admonition
        assertTrue(admonition.blocks.isNotEmpty())
    }
}
