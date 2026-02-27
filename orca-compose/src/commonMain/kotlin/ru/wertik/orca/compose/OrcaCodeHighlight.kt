package ru.wertik.orca.compose

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

internal fun buildCodeAnnotatedString(
    code: String,
    language: String?,
    style: OrcaStyle,
): AnnotatedString {
    if (!style.code.syntaxHighlightingEnabled || code.isEmpty()) {
        return AnnotatedString(code)
    }

    val normalizedLanguage = language
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotEmpty() }
        ?: return AnnotatedString(code)

    val highlights = detectHighlights(
        code = code,
        language = normalizedLanguage,
        style = style,
    )
    if (highlights.isEmpty()) {
        return AnnotatedString(code)
    }

    return buildAnnotatedString {
        append(code)
        highlights.forEach { token ->
            addStyle(
                style = token.style,
                start = token.start,
                end = token.endExclusive,
            )
        }
    }
}

private data class HighlightToken(
    val start: Int,
    val endExclusive: Int,
    val style: SpanStyle,
)

private fun detectHighlights(
    code: String,
    language: String,
    style: OrcaStyle,
): List<HighlightToken> {
    val result = mutableListOf<HighlightToken>()

    fun overlapsExisting(start: Int, endExclusive: Int): Boolean {
        for (token in result) {
            if (token.start >= endExclusive) break
            if (token.endExclusive > start) return true
        }
        return false
    }

    fun insertSorted(token: HighlightToken) {
        val insertIndex = result.binarySearchInsertionPoint(token.start)
        result.add(insertIndex, token)
    }

    fun addMatches(regex: Regex, tokenStyle: SpanStyle) {
        regex.findAll(code).forEach { match ->
            val range = match.range
            if (range.isEmpty()) return@forEach
            if (range.last >= code.length) return@forEach
            val start = range.first
            val endExclusive = range.last + 1
            if (overlapsExisting(start, endExclusive)) return@forEach
            insertSorted(HighlightToken(
                start = start,
                endExclusive = endExclusive,
                style = tokenStyle,
            ))
        }
    }

    // Comments first (highest priority — nothing should override them)
    commentRegexes(language).forEach { regex ->
        addMatches(regex, style.code.highlightComment)
    }

    // Multiline / raw strings before regular strings
    multilineStringRegexes(language).forEach { regex ->
        addMatches(regex, style.code.highlightString)
    }

    // Template literals (JS/TS)
    if (language in setOf("js", "javascript", "ts", "typescript")) {
        addMatches(TEMPLATE_LITERAL_REGEX, style.code.highlightString)
    }

    // Regular strings
    addMatches(STRING_REGEX, style.code.highlightString)

    // Decorators / annotations
    decoratorRegex(language)?.let { regex ->
        addMatches(regex, style.code.highlightKeyword)
    }

    // Numbers
    addMatches(NUMBER_REGEX, style.code.highlightNumber)

    // Type annotations (simplified — capitalized words after : or as or is)
    if (language in setOf("kotlin", "typescript", "ts", "swift", "rust")) {
        addMatches(TYPE_ANNOTATION_REGEX, style.code.highlightNumber)
    }

    // Keywords
    val keywords = keywordsFor(language)
    val isCaseInsensitiveKeywords = language == "sql"
    if (keywords.isNotEmpty()) {
        WORD_REGEX.findAll(code).forEach { match ->
            val matchValue = if (isCaseInsensitiveKeywords) match.value.lowercase() else match.value
            if (matchValue !in keywords) return@forEach
            val range = match.range
            if (range.last >= code.length) return@forEach
            val start = range.first
            val endExclusive = range.last + 1
            if (overlapsExisting(start, endExclusive)) return@forEach
            insertSorted(HighlightToken(
                start = start,
                endExclusive = endExclusive,
                style = style.code.highlightKeyword,
            ))
        }
    }

    return result
}

private fun List<HighlightToken>.binarySearchInsertionPoint(start: Int): Int {
    var low = 0
    var high = size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (this[mid].start < start) low = mid + 1 else high = mid
    }
    return low
}

private fun commentRegexes(language: String): List<Regex> {
    return when (language) {
        "kotlin", "java", "js", "javascript", "ts", "typescript", "c", "cpp", "csharp", "swift", "go", "rust" -> {
            listOf(SLASH_LINE_COMMENT_REGEX, BLOCK_COMMENT_REGEX)
        }

        "sql" -> listOf(SQL_LINE_COMMENT_REGEX)
        "bash", "sh", "zsh", "shell", "python", "yaml", "yml", "toml", "properties", "ruby" -> listOf(HASH_LINE_COMMENT_REGEX)
        "lua" -> listOf(LUA_LINE_COMMENT_REGEX, LUA_BLOCK_COMMENT_REGEX)
        "html", "xml" -> listOf(HTML_COMMENT_REGEX)
        else -> emptyList()
    }
}

private fun multilineStringRegexes(language: String): List<Regex> {
    return when (language) {
        "kotlin" -> listOf(KOTLIN_RAW_STRING_REGEX)
        "python" -> listOf(PYTHON_TRIPLE_DOUBLE_REGEX, PYTHON_TRIPLE_SINGLE_REGEX)
        "js", "javascript", "ts", "typescript" -> listOf(TEMPLATE_LITERAL_REGEX)
        "rust" -> listOf(RUST_RAW_STRING_REGEX)
        else -> emptyList()
    }
}

private fun decoratorRegex(language: String): Regex? {
    return when (language) {
        "python" -> PYTHON_DECORATOR_REGEX
        "kotlin", "java" -> ANNOTATION_REGEX
        "ts", "typescript" -> TYPESCRIPT_DECORATOR_REGEX
        else -> null
    }
}

private fun keywordsFor(language: String): Set<String> {
    return when (language) {
        "kotlin" -> setOf(
            "fun", "val", "var", "class", "object", "interface", "data", "sealed", "enum",
            "if", "else", "when", "for", "while", "do", "return", "in", "is", "as", "try", "catch", "finally",
            "null", "true", "false", "private", "public", "internal", "protected", "suspend", "inline",
            "override", "abstract", "open", "companion", "init", "typealias", "by", "where",
            "expect", "actual", "annotation", "crossinline", "noinline", "reified", "value",
        )

        "java" -> setOf(
            "class", "interface", "enum", "public", "private", "protected", "static", "final", "void",
            "if", "else", "switch", "case", "for", "while", "do", "return", "new", "try", "catch", "finally",
            "null", "true", "false", "extends", "implements", "abstract", "synchronized", "volatile",
            "transient", "native", "throws", "throw", "instanceof", "super", "this", "import", "package",
        )

        "js", "javascript", "ts", "typescript" -> setOf(
            "function", "const", "let", "var", "class", "extends", "import", "export", "from",
            "if", "else", "switch", "case", "for", "while", "do", "return", "new", "try", "catch", "finally",
            "null", "true", "false", "async", "await", "yield", "typeof", "instanceof", "in", "of",
            "default", "break", "continue", "throw", "delete", "void", "this", "super",
            "type", "interface", "enum", "implements", "declare", "readonly", "as", "keyof",
        )

        "json" -> setOf("true", "false", "null")
        "sql" -> setOf(
            "select", "from", "where", "join", "left", "right", "inner", "outer",
            "insert", "into", "update", "delete", "create", "table", "alter", "drop",
            "group", "by", "order", "having", "limit", "and", "or", "not", "as",
            "on", "set", "values", "distinct", "union", "all", "exists", "between",
            "like", "in", "is", "null", "true", "false", "case", "when", "then", "else", "end",
            "with", "recursive", "over", "partition", "row_number", "rank",
        )

        "bash", "sh", "zsh", "shell" -> setOf(
            "if", "then", "else", "elif", "fi", "for", "in", "do", "done", "case", "esac",
            "function", "local", "export", "return", "while", "until", "select", "break", "continue",
            "source", "alias", "unalias", "readonly", "declare", "typeset", "set", "unset",
        )

        "python" -> setOf(
            "def", "class", "import", "from", "as", "if", "elif", "else", "for", "while", "break", "continue",
            "return", "yield", "try", "except", "finally", "raise", "with", "lambda", "pass", "del",
            "None", "True", "False", "and", "or", "not", "in", "is", "global", "nonlocal", "assert",
            "async", "await",
        )

        "go" -> setOf(
            "func", "var", "const", "type", "struct", "interface", "map", "chan", "package", "import",
            "if", "else", "for", "range", "switch", "case", "default", "return", "break", "continue",
            "goto", "defer", "go", "select", "nil", "true", "false", "fallthrough",
        )

        "rust" -> setOf(
            "fn", "let", "mut", "const", "struct", "enum", "trait", "impl", "use", "pub", "mod",
            "if", "else", "match", "for", "while", "loop", "return", "break", "continue",
            "true", "false", "None", "Some", "Ok", "Err", "self", "Self", "super",
            "where", "async", "await", "move", "ref", "type", "unsafe", "extern", "crate", "dyn",
        )

        "c", "cpp" -> setOf(
            "int", "float", "double", "char", "bool", "void", "long", "short", "unsigned", "signed",
            "if", "else", "for", "while", "do", "switch", "case", "default", "return", "break", "continue",
            "struct", "union", "enum", "typedef", "const", "static", "extern", "inline",
            "true", "false", "null", "NULL", "sizeof", "auto", "register", "volatile",
            "class", "public", "private", "protected", "virtual", "override", "template", "namespace", "using",
        )

        "swift" -> setOf(
            "func", "var", "let", "class", "struct", "enum", "protocol", "extension", "import",
            "if", "else", "for", "while", "switch", "case", "return", "break", "continue",
            "true", "false", "nil", "self", "super", "guard", "defer", "throw", "try", "catch",
            "async", "await", "actor", "some", "any", "where", "in", "is", "as",
        )

        "csharp" -> setOf(
            "class", "interface", "struct", "enum", "namespace", "using", "public", "private", "protected",
            "static", "readonly", "const", "void", "if", "else", "for", "foreach", "while", "switch",
            "case", "return", "break", "continue", "new", "null", "true", "false", "var", "async", "await",
            "abstract", "virtual", "override", "sealed", "partial", "ref", "out", "in", "is", "as",
            "typeof", "nameof", "default", "throw", "try", "catch", "finally", "yield", "lock",
        )

        "ruby" -> setOf(
            "def", "class", "module", "if", "elsif", "else", "unless", "case", "when", "while", "until",
            "for", "do", "end", "begin", "rescue", "ensure", "raise", "return", "yield", "block_given?",
            "nil", "true", "false", "self", "super", "require", "include", "extend", "attr_accessor",
        )

        "lua" -> setOf(
            "function", "local", "if", "then", "else", "elseif", "end", "for", "while", "do", "repeat",
            "until", "return", "break", "in", "and", "or", "not", "nil", "true", "false",
        )

        else -> emptySet()
    }
}

private val WORD_REGEX = Regex("""\b[A-Za-z_][A-Za-z0-9_]*\b""")
private val NUMBER_REGEX = Regex("""\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFLlUu]?\b""")
private val STRING_REGEX = Regex("""("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')""")
private val SLASH_LINE_COMMENT_REGEX = Regex("""//.*$""", setOf(RegexOption.MULTILINE))
private val HASH_LINE_COMMENT_REGEX = Regex("""#.*$""", setOf(RegexOption.MULTILINE))
private val SQL_LINE_COMMENT_REGEX = Regex("""--.*$""", setOf(RegexOption.MULTILINE))
private val BLOCK_COMMENT_REGEX = Regex("""/\*(?:[^*]|\*(?!/))*\*/""")
private val LUA_LINE_COMMENT_REGEX = Regex("""--(?!\[\[).*$""", setOf(RegexOption.MULTILINE))
private val LUA_BLOCK_COMMENT_REGEX = Regex("""--\[\[[\s\S]*?\]\]""")
private val HTML_COMMENT_REGEX = Regex("""<!--[\s\S]*?-->""")

// Multiline / raw strings
private val KOTLIN_RAW_STRING_REGEX = Regex("\"\"\"[\\s\\S]*?\"\"\"")
private val PYTHON_TRIPLE_DOUBLE_REGEX = Regex("\"\"\"[\\s\\S]*?\"\"\"")
private val PYTHON_TRIPLE_SINGLE_REGEX = Regex("'''[\\s\\S]*?'''")
private val TEMPLATE_LITERAL_REGEX = Regex("""`(?:\\.|[^`\\])*`""")
private val RUST_RAW_STRING_REGEX = Regex("""r#+"[^"]*?"#+""")

// Decorators / annotations
private val PYTHON_DECORATOR_REGEX = Regex("""@\w+(?:\.\w+)*""")
private val ANNOTATION_REGEX = Regex("""@\w+""")
private val TYPESCRIPT_DECORATOR_REGEX = Regex("""@\w+""")

// Type annotations (capitalized identifiers after : or as or is)
private val TYPE_ANNOTATION_REGEX = Regex("""(?<=:\s|as\s|is\s)[A-Z][A-Za-z0-9_]*""")
