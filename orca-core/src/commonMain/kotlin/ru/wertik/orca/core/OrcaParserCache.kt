package ru.wertik.orca.core

internal class OrcaParserCache(
    maxEntries: Int = DEFAULT_PARSE_CACHE_SIZE,
) {
    private val maxEntries = maxEntries.coerceAtLeast(1)
    private val entries = linkedMapOf<Any, CachedParseEntry>()
    private val lock = OrcaLock()

    fun getOrPut(
        key: Any,
        input: String,
        parse: () -> OrcaParseResult,
    ): OrcaParseResult {
        val cached = lock.withLock {
            val entry = entries[key]
            if (entry != null && entry.input == input) {
                entries.remove(key)
                entries[key] = entry
                entry.result
            } else {
                null
            }
        }
        if (cached != null) return cached

        val parsed = parse()

        lock.withLock {
            entries.remove(key)
            entries[key] = CachedParseEntry(
                input = input,
                result = parsed,
            )
            trimToLimit()
        }
        return parsed
    }

    fun clear() {
        lock.withLock {
            entries.clear()
        }
    }

    private fun trimToLimit() {
        while (entries.size > maxEntries) {
            val eldestKey = entries.entries.firstOrNull()?.key ?: return
            entries.remove(eldestKey)
        }
    }
}

private data class CachedParseEntry(
    val input: String,
    val result: OrcaParseResult,
)
