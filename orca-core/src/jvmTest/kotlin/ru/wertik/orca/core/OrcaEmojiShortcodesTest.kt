package ru.wertik.orca.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrcaEmojiShortcodesTest {

    private val parser = OrcaMarkdownParser()

    @Test
    fun basicEmojiShortcodeIsReplaced() {
        val result = parser.parse("Hello :smile:")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val text = paragraph.content.filterIsInstance<OrcaInline.Text>()
            .joinToString("") { it.text }
        assertTrue(text.contains("\uD83D\uDE04"))
    }

    @Test
    fun unknownShortcodeIsLeftAsIs() {
        val result = parser.parse("Hello :nonexistent_emoji:")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val text = paragraph.content.filterIsInstance<OrcaInline.Text>()
            .joinToString("") { it.text }
        assertTrue(text.contains(":nonexistent_emoji:"))
    }

    @Test
    fun shortcodeInsideInlineCodeIsNotReplaced() {
        val result = parser.parse("Use `:smile:` for emoji")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val code = paragraph.content.filterIsInstance<OrcaInline.InlineCode>().single()
        assertEquals(":smile:", code.code)
    }

    @Test
    fun multipleShortcodesInSameText() {
        val result = parser.parse(":fire: and :rocket:")
        val paragraph = result.blocks.single() as OrcaBlock.Paragraph
        val text = paragraph.content.filterIsInstance<OrcaInline.Text>()
            .joinToString("") { it.text }
        assertTrue(text.contains("\uD83D\uDD25"))
        assertTrue(text.contains("\uD83D\uDE80"))
    }

    @Test
    fun shortcodeInCodeBlockIsNotReplaced() {
        val result = parser.parse("```\n:smile:\n```")
        val codeBlock = result.blocks.single() as OrcaBlock.CodeBlock
        assertEquals(":smile:", codeBlock.code)
    }
}
