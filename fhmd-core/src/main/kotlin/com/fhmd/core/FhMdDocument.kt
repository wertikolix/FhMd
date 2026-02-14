package com.fhmd.core

data class FhMdDocument(
    val blocks: List<FhMdBlock>,
)

sealed interface FhMdBlock {
    data class Heading(
        val level: Int,
        val content: List<FhMdInline>,
    ) : FhMdBlock

    data class Paragraph(
        val content: List<FhMdInline>,
    ) : FhMdBlock

    data class ListBlock(
        val ordered: Boolean,
        val items: List<FhMdListItem>,
    ) : FhMdBlock

    data class Quote(
        val blocks: List<FhMdBlock>,
    ) : FhMdBlock

    data class CodeBlock(
        val code: String,
        val language: String?,
    ) : FhMdBlock

    data class Table(
        val header: List<FhMdTableCell>,
        val rows: List<List<FhMdTableCell>>,
    ) : FhMdBlock
}

data class FhMdListItem(
    val blocks: List<FhMdBlock>,
)

data class FhMdTableCell(
    val content: List<FhMdInline>,
    val alignment: FhMdTableAlignment?,
)

enum class FhMdTableAlignment {
    LEFT,
    CENTER,
    RIGHT,
}

sealed interface FhMdInline {
    data class Text(
        val text: String,
    ) : FhMdInline

    data class Bold(
        val content: List<FhMdInline>,
    ) : FhMdInline

    data class Italic(
        val content: List<FhMdInline>,
    ) : FhMdInline

    data class InlineCode(
        val code: String,
    ) : FhMdInline

    data class Link(
        val destination: String,
        val content: List<FhMdInline>,
    ) : FhMdInline
}
