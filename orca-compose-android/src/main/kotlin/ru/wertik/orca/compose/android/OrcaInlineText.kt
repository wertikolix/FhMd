package ru.wertik.orca.compose.android

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import ru.wertik.orca.core.OrcaInline

internal fun buildInlineAnnotatedString(
    inlines: List<OrcaInline>,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
): AnnotatedString {
    return buildAnnotatedString {
        appendInlines(
            inlines = inlines,
            style = style,
            onLinkClick = onLinkClick,
        )
    }
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<OrcaInline>,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
) {
    inlines.forEach { inline ->
        appendInline(inline, style, onLinkClick)
    }
}

private fun AnnotatedString.Builder.appendInline(
    inline: OrcaInline,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
) {
    when (inline) {
        is OrcaInline.Text -> append(inline.text)

        is OrcaInline.Bold -> withStyle(style = boldStyle) {
            appendInlines(
                inlines = inline.content,
                style = style,
                onLinkClick = onLinkClick,
            )
        }

        is OrcaInline.Italic -> withStyle(style = italicStyle) {
            appendInlines(
                inlines = inline.content,
                style = style,
                onLinkClick = onLinkClick,
            )
        }

        is OrcaInline.Strikethrough -> withStyle(style = style.inline.strikethrough) {
            appendInlines(
                inlines = inline.content,
                style = style,
                onLinkClick = onLinkClick,
            )
        }

        is OrcaInline.InlineCode -> withStyle(style = style.inline.inlineCode) {
            append(inline.code)
        }

        is OrcaInline.Link -> if (!isSafeLinkDestination(inline.destination)) {
            appendLinkContent(
                inline = inline,
                style = style,
                onLinkClick = onLinkClick,
            )
        } else {
            withLink(
                LinkAnnotation.Url(
                    url = inline.destination,
                    styles = TextLinkStyles(style = style.inline.link),
                    linkInteractionListener = LinkInteractionListener { annotation ->
                        val target = (annotation as? LinkAnnotation.Url)?.url ?: inline.destination
                        onLinkClick(target)
                    },
                ),
            ) {
                appendLinkContent(
                    inline = inline,
                    style = style,
                    onLinkClick = onLinkClick,
                )
            }
        }

        is OrcaInline.Image -> append(imageInlineFallbackText(inline))
    }
}

private fun AnnotatedString.Builder.appendLinkContent(
    inline: OrcaInline.Link,
    style: OrcaStyle,
    onLinkClick: (String) -> Unit,
) {
    if (inline.content.isEmpty()) {
        append(inline.destination)
    } else {
        appendInlines(
            inlines = inline.content,
            style = style,
            onLinkClick = onLinkClick,
        )
    }
}

internal fun imageInlineFallbackText(image: OrcaInline.Image): String {
    return image.alt?.takeIf { it.isNotBlank() } ?: image.source
}
