package com.fhmd.compose.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.fhmd.core.CommonmarkFhMdParser
import com.fhmd.core.FhMdBlock
import com.fhmd.core.FhMdDocument
import com.fhmd.core.FhMdParser

@Composable
fun FhMd(
    markdown: String,
    modifier: Modifier = Modifier,
    parser: FhMdParser = remember { CommonmarkFhMdParser() },
    style: FhMdStyle = FhMdStyle(),
    onLinkClick: (String) -> Unit = {},
) {
    val document = remember(markdown, parser) {
        parser.parse(markdown)
    }
    FhMd(
        document = document,
        modifier = modifier,
        style = style,
        onLinkClick = onLinkClick,
    )
}

@Composable
fun FhMd(
    document: FhMdDocument,
    modifier: Modifier = Modifier,
    style: FhMdStyle = FhMdStyle(),
    onLinkClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(style.blockSpacing),
    ) {
        document.blocks.forEach { block ->
            FhMdBlockNode(
                block = block,
                style = style,
                onLinkClick = onLinkClick,
            )
        }
    }
}

@Composable
private fun FhMdBlockNode(
    block: FhMdBlock,
    style: FhMdStyle,
    onLinkClick: (String) -> Unit,
) {
    when (block) {
        is FhMdBlock.Heading -> InlineTextNode(
            text = buildInlineAnnotatedString(
                inlines = block.content,
                style = style,
                onLinkClick = onLinkClick,
            ),
            textStyle = style.heading(block.level),
        )

        is FhMdBlock.Paragraph -> InlineTextNode(
            text = buildInlineAnnotatedString(
                inlines = block.content,
                style = style,
                onLinkClick = onLinkClick,
            ),
            textStyle = style.paragraph,
        )

        is FhMdBlock.ListBlock -> Column(
            verticalArrangement = Arrangement.spacedBy(style.nestedBlockSpacing),
        ) {
            block.items.forEachIndexed { index, item ->
                Row {
                    val marker = if (block.ordered) "${index + 1}." else "•"
                    Text(
                        text = marker,
                        style = style.paragraph,
                        modifier = Modifier.width(style.listMarkerWidth),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(style.nestedBlockSpacing),
                    ) {
                        item.blocks.forEach { listItemBlock ->
                            FhMdBlockNode(
                                block = listItemBlock,
                                style = style,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }
                }
            }
        }

        is FhMdBlock.Quote -> Row {
            Box(
                modifier = Modifier
                    .width(style.quoteStripeWidth)
                    .fillMaxHeight()
                    .background(style.quoteStripeColor),
            )
            Column(
                modifier = Modifier
                    .padding(start = style.quoteSpacing)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(style.nestedBlockSpacing),
            ) {
                block.blocks.forEach { nested ->
                    FhMdBlockNode(
                        block = nested,
                        style = style,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }

        is FhMdBlock.CodeBlock -> Text(
            text = block.code,
            style = style.codeBlock,
            modifier = Modifier
                .fillMaxWidth()
                .background(style.codeBlockBackground, style.codeBlockShape)
                .padding(style.codeBlockPadding),
        )
    }
}

@Composable
private fun InlineTextNode(
    text: AnnotatedString,
    textStyle: androidx.compose.ui.text.TextStyle,
) {
    Text(
        text = text,
        style = textStyle,
    )
}
