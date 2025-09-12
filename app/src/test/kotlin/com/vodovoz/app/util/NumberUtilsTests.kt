package com.vodovoz.app.util

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class NumberUtilsTests {

    @Test
    fun `test clean zeros`(){
        assert(1f.clearZeros() == "1")
        assert(1.000f.clearZeros() == "1")
        assert(1.110000f.clearZeros() == "1.11")
        assert(0.000f.clearZeros() == "0")
        assert(0f.clearZeros() == "0")
        assert(0.0f.clearZeros() == "0")
    }

    @Test
    fun `valid simple numbers`() {
        assertEquals(1.23f, "1.23".smartParseFloat())
        assertEquals(1.23f, "1,23".smartParseFloat())
        assertEquals(1234.56f, "1.234,56".smartParseFloat())
        assertEquals(1234567.89f, "1.234.567,89".smartParseFloat())
    }

    @Test
    fun `trailing and leading spaces`() {
        assertEquals(3.14f, "   3.14   ".smartParseFloat())
        assertEquals(2.71f, "\n\t2,71\r ".smartParseFloat())
    }

    @Test
    fun `invalid strings`() {
        assertNull("abc".smartParseFloat())
        assertNull("".smartParseFloat())
        assertNull("   ".smartParseFloat())
    }

    @Test
    fun `mixed characters`() {
        assertEquals(123.45f, "abc123,45xyz".smartParseFloat())
        assertEquals(99.0f, "$0,99 руб.".smartParseFloat())
        assertEquals(10.5f, "~10,5~".smartParseFloat())
    }

    @Test
    fun `multiple separators`() {
        assertEquals(1234567.9f, "1.234.567,89".smartParseFloat())
        assertEquals(1234567.9f, "1,234,567.89".smartParseFloat())
    }
}