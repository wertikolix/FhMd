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
        return lock.withLock {
            val entry = entries[key]
            if (entry != null && entry.input == input) {
                // Cache hit — move to end (LRU refresh).
                entries.remove(key)
                entries[key] = entry
                return@withLock entry.result
            }

            // Cache miss — parse under lock to prevent duplicate work
            // and ensure consistent cache state for the same key.
            val parsed = parse()

            entries.remove(key)
            entries[key] = CachedParseEntry(
                input = input,
                result = parsed,
            )
            trimToLimit()
            parsed
        }
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
