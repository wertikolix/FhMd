package com.fhmd.core

fun interface FhMdParser {
    fun parse(input: String): FhMdDocument
}
