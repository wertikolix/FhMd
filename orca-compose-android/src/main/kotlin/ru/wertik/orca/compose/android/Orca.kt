package ru.wertik.orca.compose.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wertik.orca.core.CommonmarkOrcaParser
import ru.wertik.orca.core.OrcaBlock
import ru.wertik.orca.core.OrcaDocument
import ru.wertik.orca.core.OrcaParser
import ru.wertik.orca.core.OrcaTableAlignment
import ru.wertik.orca.core.OrcaTableCell
import ru.wertik.orca.core.OrcaTaskState

private val defaultParser: OrcaParser = CommonmarkOrcaParser()
private val defaultStyle: OrcaStyle = OrcaStyle()
private val noOpLinkClick: (String) -> Unit = {}

@Composable
fun Orca(
    markdown: String,
    modifier: Modifier = Modifier,
    parser: OrcaParser = defaultParser,
    style: OrcaStyle = defaultStyle,
    onLinkClick: (String) -> Unit = noOpLinkClick,
) {
    val parserKey = parser.cacheKey()
    val emptyDocument = remember { OrcaDocument(emptyList()) }
    var latestDocument by remember(parserKey) { mutableStateOf(emptyDocument) }
    val document by produceState(
        initialValue = latestDocument,
        markdown,
        parserKey,
    ) {
        val parsed = withContext(Dispatchers.Default) {
            parser.parse(markdown)
        }
        latestDocument = parsed
        value = parsed
    }
    Orca(
        document = document,
        modifier = modifier,
        style = style,
        onLinkClick = onLinkClick,
    )
}

@Composable
fun Orca(
    document: OrcaDocument,
    modifier: Modifier = Modifier,
    style: OrcaStyle = defaultStyle,
    onLinkClick: (String) -> Unit = noOpLinkClick,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(style.layout.blockSpacing),
    ) {
        itemsIndexed(
            items = document.blocks,
            key = { index, _ -> index },
        ) { _, block ->
            OrcaBlockNode(
                block = block,
                style = style,
                onLinkClick = onLinkClick,
            )
        }
    }
}

@Composable
private fun OrcaBlockNode(
    block: OrcaBlock,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
) {
    when (block) {
        is OrcaBlock.Heading -> {
            val headingText = remember(block.content, style, onLinkClick) {
                buildInlineAnnotatedString(
                    inlines = block.content,
                    style = style,
                    onLinkClick = onLinkClick,
                )
            }
            InlineTextNode(
                text = headingText,
                textStyle = style.heading(block.level),
            )
        }

        is OrcaBlock.Paragraph -> {
            val paragraphText = remember(block.content, style, onLinkClick) {
                buildInlineAnnotatedString(
                    inlines = block.content,
                    style = style,
                    onLinkClick = onLinkClick,
                )
            }
            InlineTextNode(
                text = paragraphText,
                textStyle = style.typography.paragraph,
            )
        }

        is OrcaBlock.ListBlock -> Column(
            verticalArrangement = Arrangement.spacedBy(style.layout.nestedBlockSpacing),
        ) {
            block.items.forEachIndexed { index, item ->
                Row {
                    val marker = listMarkerText(
                        ordered = block.ordered,
                        startNumber = block.startNumber,
                        index = index,
                        taskState = item.taskState,
                    )
                    Text(
                        text = marker,
                        style = style.typography.paragraph,
                        modifier = Modifier.width(style.layout.listMarkerWidth),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(style.layout.nestedBlockSpacing),
                    ) {
                        item.blocks.forEach { listItemBlock ->
                            OrcaBlockNode(
                                block = listItemBlock,
                                style = style,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }
                }
            }
        }

        is OrcaBlock.Quote -> Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(style.quote.stripeWidth)
                    .fillMaxHeight()
                    .background(style.quote.stripeColor),
            )
            Column(
                modifier = Modifier
                    .padding(start = style.quote.spacing)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(style.layout.nestedBlockSpacing),
            ) {
                block.blocks.forEach { nested ->
                    OrcaBlockNode(
                        block = nested,
                        style = style,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }

        is OrcaBlock.CodeBlock -> Text(
            text = block.code,
            style = style.code.text,
            modifier = Modifier
                .fillMaxWidth()
                .background(style.code.background, style.code.shape)
                .padding(style.code.padding),
        )

        is OrcaBlock.Image -> MarkdownImageNode(
            block = block,
            style = style,
        )

        is OrcaBlock.ThematicBreak -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.thematicBreak.thickness)
                .background(style.thematicBreak.color),
        )

        is OrcaBlock.Table -> {
            val columnCount = maxOf(
                block.header.size,
                block.rows.maxOfOrNull { row -> row.size } ?: 0,
            )
            if (columnCount == 0) return

            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .border(style.table.borderWidth, style.table.borderColor),
            ) {
                TableRowNode(
                    cells = block.header,
                    columnCount = columnCount,
                    isHeader = true,
                    style = style,
                    onLinkClick = onLinkClick,
                )
                block.rows.forEach { row ->
                    TableRowNode(
                        cells = row,
                        columnCount = columnCount,
                        isHeader = false,
                        style = style,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
    }
}

internal fun listMarkerText(
    ordered: Boolean,
    startNumber: Int,
    index: Int,
    taskState: OrcaTaskState?,
): String {
    return when (taskState) {
        OrcaTaskState.CHECKED -> "☑"
        OrcaTaskState.UNCHECKED -> "☐"
        null -> if (ordered) {
            "${startNumber + index}."
        } else {
            "•"
        }
    }
}

@Composable
private fun TableRowNode(
    cells: List<OrcaTableCell>,
    columnCount: Int,
    isHeader: Boolean,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
) {
    Row {
        repeat(columnCount) { index ->
            val cell = cells.getOrNull(index)
            val text = remember(cell, style, onLinkClick) {
                if (cell == null) {
                    AnnotatedString("")
                } else {
                    buildInlineAnnotatedString(
                        inlines = cell.content,
                        style = style,
                        onLinkClick = onLinkClick,
                    )
                }
            }
            val align = tableCellAlignment(cell?.alignment)
            Box(
                modifier = Modifier
                    .width(style.table.columnWidth)
                    .border(style.table.borderWidth, style.table.borderColor)
                    .background(if (isHeader) style.table.headerBackground else Color.Transparent)
                    .padding(style.table.cellPadding),
            ) {
                Text(
                    text = text,
                    style = if (isHeader) style.table.headerText else style.table.text,
                    textAlign = align,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MarkdownImageNode(
    block: OrcaBlock.Image,
    style: OrcaStyle,
) {
    val safeSource = remember(block.source) {
        block.source.takeIf(::isSafeImageSource)
    }
    if (safeSource == null) {
        Text(
            text = imageBlockFallbackText(block),
            style = style.typography.paragraph,
        )
        return
    }

    AsyncImage(
        model = safeSource,
        contentDescription = block.alt,
        contentScale = style.image.contentScale,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = style.image.maxHeight)
            .clip(style.image.shape)
            .background(style.image.background),
    )
}

private fun imageBlockFallbackText(block: OrcaBlock.Image): String {
    return block.alt?.takeIf { it.isNotBlank() } ?: block.source
}

private fun tableCellAlignment(alignment: OrcaTableAlignment?): TextAlign {
    return when (alignment) {
        OrcaTableAlignment.LEFT -> TextAlign.Start
        OrcaTableAlignment.CENTER -> TextAlign.Center
        OrcaTableAlignment.RIGHT -> TextAlign.End
        null -> TextAlign.Start
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
