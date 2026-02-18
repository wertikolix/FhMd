package ru.wertik.orca.compose

import kotlin.test.Test
import kotlin.test.assertEquals

class OrcaHtmlUtilsTest {

    // --- decodeBasicHtmlEntities ---

    @Test
    fun decodeAmpersandEntity() {
        assertEquals("a & b", decodeBasicHtmlEntities("a &amp; b"))
    }

    @Test
    fun decodeNbspEntity() {
        assertEquals("a b", decodeBasicHtmlEntities("a&nbsp;b"))
    }

    @Test
    fun decodeLtAndGtEntities() {
        assertEquals("<tag>", decodeBasicHtmlEntities("&lt;tag&gt;"))
    }

    @Test
    fun decodeQuotEntity() {
        assertEquals("say \"hi\"", decodeBasicHtmlEntities("say &quot;hi&quot;"))
    }

    @Test
    fun decodeApos39Entity() {
        assertEquals("it's", decodeBasicHtmlEntities("it&#39;s"))
    }

    @Test
    fun decodeMultipleEntitiesInSingleString() {
        assertEquals("< & > \"'", decodeBasicHtmlEntities("&lt; &amp; &gt; &quot;&#39;"))
    }

    @Test
    fun decodeEmptyStringReturnsEmpty() {
        assertEquals("", decodeBasicHtmlEntities(""))
    }

    @Test
    fun decodeStringWithNoEntitiesIsUnchanged() {
        assertEquals("plain text", decodeBasicHtmlEntities("plain text"))
    }

    // --- htmlBlockFallbackText ---

    @Test
    fun htmlBlockFallbackStripsSimpleTags() {
        assertEquals("hello\n world", htmlBlockFallbackText("<p>hello</p> <span>world</span>"))
    }

    @Test
    fun htmlBlockFallbackConvertsBreakTagToNewline() {
        assertEquals("line1\nline2", htmlBlockFallbackText("line1<br/>line2"))
    }

    @Test
    fun htmlBlockFallbackConvertsBreakTagCaseInsensitive() {
        assertEquals("a\nb", htmlBlockFallbackText("a<BR>b"))
    }

    @Test
    fun htmlBlockFallbackConvertsBlockBreakTagsToNewlines() {
        assertEquals("hello\nworld", htmlBlockFallbackText("<p>hello</p><p>world</p>"))
    }

    @Test
    fun htmlBlockFallbackDecodesEntitiesAfterStrippingTags() {
        assertEquals("a & b", htmlBlockFallbackText("<p>a &amp; b</p>"))
    }

    @Test
    fun htmlBlockFallbackTrimsLeadingAndTrailingWhitespace() {
        assertEquals("text", htmlBlockFallbackText("  <p>text</p>  "))
    }

    @Test
    fun htmlBlockFallbackReturnsEmptyForTagsOnly() {
        assertEquals("", htmlBlockFallbackText("<div></div>"))
    }

    @Test
    fun htmlBlockFallbackHandlesDivAndLiAndH1() {
        assertEquals("item\nheading", htmlBlockFallbackText("<li>item</li><h1>heading</h1>"))
    }

    // --- htmlInlineFallbackText ---

    @Test
    fun htmlInlineFallbackStripsTagsAndDecodesEntities() {
        assertEquals("bold & text", htmlInlineFallbackText("<b>bold</b> &amp; text"))
    }

    @Test
    fun htmlInlineFallbackConvertsBreakToNewline() {
        assertEquals("a\nb", htmlInlineFallbackText("a<br/>b"))
    }

    @Test
    fun htmlInlineFallbackEmptyHtmlReturnsEmpty() {
        assertEquals("", htmlInlineFallbackText(""))
    }

    @Test
    fun htmlInlineFallbackPreservesTextOutsideTags() {
        assertEquals("hello", htmlInlineFallbackText("hello"))
    }
}
