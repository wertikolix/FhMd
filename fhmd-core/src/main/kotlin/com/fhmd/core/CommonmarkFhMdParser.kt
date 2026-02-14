package com.fhmd.core

import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.HardLineBreak
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.parser.Parser

class CommonmarkFhMdParser(
    private val parser: Parser = Parser.builder().build(),
) : FhMdParser {

    override fun parse(input: String): FhMdDocument {
        val root = parser.parse(input)
        return FhMdDocument(
            blocks = root.childSequence()
                .mapNotNull(::mapBlock)
                .toList(),
        )
    }

    private fun mapBlock(node: Node): FhMdBlock? {
        return when (node) {
            is Heading -> FhMdBlock.Heading(
                level = node.level,
                content = mapInlineContainer(node),
            )

            is Paragraph -> FhMdBlock.Paragraph(content = mapInlineContainer(node))

            is BulletList -> FhMdBlock.ListBlock(
                ordered = false,
                items = node.childSequence()
                    .filterIsInstance<ListItem>()
                    .map(::mapListItem)
                    .toList(),
            )

            is OrderedList -> FhMdBlock.ListBlock(
                ordered = true,
                items = node.childSequence()
                    .filterIsInstance<ListItem>()
                    .map(::mapListItem)
                    .toList(),
            )

            is BlockQuote -> FhMdBlock.Quote(
                blocks = node.childSequence()
                    .mapNotNull(::mapBlock)
                    .toList(),
            )

            is FencedCodeBlock -> FhMdBlock.CodeBlock(
                code = node.literal.trimEnd('\n'),
                language = node.info.takeLanguageOrNull(),
            )

            is IndentedCodeBlock -> FhMdBlock.CodeBlock(
                code = node.literal.trimEnd('\n'),
                language = null,
            )

            else -> null
        }
    }

    private fun mapListItem(node: ListItem): FhMdListItem {
        val mapped = node.childSequence()
            .mapNotNull(::mapBlock)
            .toList()
        return FhMdListItem(blocks = mapped)
    }

    private fun mapInlineContainer(container: Node): List<FhMdInline> {
        return container.childSequence().flatMap(::mapInline).toList()
    }

    private fun mapInline(node: Node): Sequence<FhMdInline> {
        return when (node) {
            is Text -> sequenceOf(FhMdInline.Text(node.literal))
            is StrongEmphasis -> sequenceOf(FhMdInline.Bold(content = mapInlineContainer(node)))
            is Emphasis -> sequenceOf(FhMdInline.Italic(content = mapInlineContainer(node)))
            is Code -> sequenceOf(FhMdInline.InlineCode(code = node.literal))
            is Link -> sequenceOf(
                FhMdInline.Link(
                    destination = node.destination,
                    content = mapInlineContainer(node),
                )
            )

            is SoftLineBreak,
            is HardLineBreak,
                -> sequenceOf(FhMdInline.Text("\n"))

            is Block -> emptySequence()
            else -> node.childSequence().flatMap(::mapInline)
        }
    }
}

private fun Node.childSequence(): Sequence<Node> = sequence {
    var child = firstChild
    while (child != null) {
        yield(child)
        child = child.next
    }
}

private fun String.takeLanguageOrNull(): String? {
    val firstToken = trim().split(' ').firstOrNull()?.trim()
    return firstToken?.takeIf { it.isNotEmpty() }
}
