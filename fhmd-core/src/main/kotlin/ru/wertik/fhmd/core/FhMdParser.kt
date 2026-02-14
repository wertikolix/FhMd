package ru.wertik.fhmd.core

fun interface FhMdParser {
    fun parse(input: String): FhMdDocument

    fun cacheKey(): Any = this::class
}
