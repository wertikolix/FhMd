package ru.wertik.orca.core

import org.intellij.markdown.parser.MarkdownParser

@Deprecated(
    message = "Use OrcaMarkdownParser. This alias will be removed in a future major release.",
    replaceWith = ReplaceWith(
        expression = "OrcaMarkdownParser(parser, maxTreeDepth, onDepthLimitExceeded)",
        imports = ["ru.wertik.orca.core.OrcaMarkdownParser"],
    ),
)
class IntellijMarkdownOrcaParser(
    parser: MarkdownParser = defaultParser(),
    maxTreeDepth: Int = DEFAULT_MAX_TREE_DEPTH,
    onDepthLimitExceeded: ((Int) -> Unit)? = null,
) : OrcaParser by OrcaMarkdownParser(
    parser = parser,
    maxTreeDepth = maxTreeDepth,
    onDepthLimitExceeded = onDepthLimitExceeded,
)
