package ru.wertik.orca.compose.android

import java.net.URI

private val safeLinkSchemes = setOf("http", "https", "mailto")
private val safeImageSchemes = setOf("http", "https")

internal fun isSafeLinkDestination(destination: String): Boolean {
    return hasAllowedScheme(
        value = destination,
        allowedSchemes = safeLinkSchemes,
    )
}

internal fun isSafeImageSource(source: String): Boolean {
    return hasAllowedScheme(
        value = source,
        allowedSchemes = safeImageSchemes,
    )
}

private fun hasAllowedScheme(
    value: String,
    allowedSchemes: Set<String>,
): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) {
        return false
    }

    val uri = runCatching { URI(trimmed) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase() ?: return false
    return scheme in allowedSchemes
}
