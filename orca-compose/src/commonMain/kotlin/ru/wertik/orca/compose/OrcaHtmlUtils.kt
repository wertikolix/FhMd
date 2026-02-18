package ru.wertik.orca.compose

internal val HTML_TAG_REGEX = Regex("</?[a-zA-Z][^>]*>")
internal val BR_TAG_REGEX = Regex("(?i)<br\\s*/?>")
internal val BLOCK_BREAK_TAG_REGEX = Regex("(?i)</(p|div|li|h[1-6]|blockquote|tr|table|ul|ol)>")

internal fun decodeBasicHtmlEntities(text: String): String {
    return text
        .replace("&amp;", "&")
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
}
