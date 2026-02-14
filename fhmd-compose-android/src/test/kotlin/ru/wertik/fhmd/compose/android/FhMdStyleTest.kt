package ru.wertik.fhmd.compose.android

import org.junit.Assert.assertEquals
import org.junit.Test

class FhMdStyleTest {

    @Test
    fun `heading levels are clamped to supported range`() {
        val style = FhMdStyle()

        assertEquals(style.heading1, style.heading(-10))
        assertEquals(style.heading1, style.heading(1))
        assertEquals(style.heading4, style.heading(4))
        assertEquals(style.heading6, style.heading(6))
        assertEquals(style.heading6, style.heading(42))
    }
}
