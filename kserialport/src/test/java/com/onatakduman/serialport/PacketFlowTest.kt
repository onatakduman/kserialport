package com.onatakduman.serialport

import com.onatakduman.kserialport.delimiter
import com.onatakduman.kserialport.delimiterBased
import com.onatakduman.kserialport.fixedLength
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PacketFlowTest {

    @Test
    fun testDelimiter() = runBlocking {
        val input =
                flowOf(
                        byteArrayOf(0x01, 0x02, 0x03, 0x0A), // Packet 1 + delimiter
                        byteArrayOf(0x04, 0x05), // Part of Packet 2
                        byteArrayOf(0x06, 0x0A, 0x07) // Rest of Packet 2 + delimiter + extra
                )

        val delimiter = byteArrayOf(0x0A)
        val result = input.delimiter(delimiter).toList()

        assertEquals(2, result.size)
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03), result[0])
        assertArrayEquals(byteArrayOf(0x04, 0x05, 0x06), result[1])
    }

    @Test
    fun testDelimiterBased() = runBlocking {
        // Test with multiple packets in one chunk
        val input = flowOf(byteArrayOf(0x01, 0x0A, 0x02, 0x0A), byteArrayOf(0x03, 0x0A))

        val delimiter = byteArrayOf(0x0A)
        val result = input.delimiterBased(delimiter).toList()

        assertEquals(3, result.size)
        assertArrayEquals(byteArrayOf(0x01), result[0])
        assertArrayEquals(byteArrayOf(0x02), result[1])
        assertArrayEquals(byteArrayOf(0x03), result[2])
    }

    @Test
    fun testFixedLength() = runBlocking {
        val input = flowOf(byteArrayOf(0x01, 0x02, 0x03), byteArrayOf(0x04, 0x05, 0x06, 0x07))

        val result = input.fixedLength(3).toList()

        assertEquals(2, result.size)
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03), result[0])
        assertArrayEquals(byteArrayOf(0x04, 0x05, 0x06), result[1])
        // 0x07 is left in buffer
    }
}
