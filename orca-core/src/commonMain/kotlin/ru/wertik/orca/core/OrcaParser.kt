package ru.wertik.orca.core

/**
 * Parser interface for converting markdown text into [OrcaDocument].
 *
 * Default implementation: [OrcaMarkdownParser].
 *
 * @see OrcaMarkdownParser
 */
fun interface OrcaParser {
    /**
     * Parse [input] markdown string into an [OrcaDocument].
     *
     * @param input Raw markdown text.
     * @return Parsed document.
     */
    fun parse(input: String): OrcaDocument

    /**
     * Parse [input] and return the result together with [diagnostics][OrcaParseDiagnostics].
     *
     * The default implementation delegates to [parse] and returns empty diagnostics.
     *
     * @param input Raw markdown text.
     * @return Parse result containing the document and any diagnostics.
     */
    fun parseWithDiagnostics(input: String): OrcaParseResult {
        return OrcaParseResult(
            document = parse(input),
        )
    }

    /**
     * Parse with caching by [key]. The default implementation does NOT cache
     * and delegates to [parseWithDiagnostics]. Override to provide actual caching.
     * [OrcaMarkdownParser] provides a full LRU cache implementation.
     *
     * @param key Cache key identifying this input (e.g. file path).
     * @param input Raw markdown text.
     * @return Parsed document, potentially from cache.
     */
    fun parseCached(
        key: Any,
        input: String,
    ): OrcaDocument {
        return parseCachedWithDiagnostics(
            key = key,
            input = input,
        ).document
    }

    /**
     * Parse with diagnostics and caching by [key]. The default implementation
     * does NOT cache and delegates to [parseWithDiagnostics]. Override to provide
     * actual caching. [OrcaMarkdownParser] provides a full LRU cache implementation.
     *
     * @param key Cache key identifying this input (e.g. file path).
     * @param input Raw markdown text.
     * @return Parse result, potentially from cache.
     */
    fun parseCachedWithDiagnostics(
        key: Any,
        input: String,
    ): OrcaParseResult {
        return parseWithDiagnostics(input)
    }

    /**
     * Key used to identify this parser instance in shared caches.
     *
     * @return Opaque cache key; defaults to the parser's class.
     */
    fun cacheKey(): Any = this::class
}
