package com.onatakduman.kserialport

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class DataLoggerTest {

    // Helper to access private methods using reflection for testing
    // Since the format methods are private, we can either make them internal/public or test via reflection.
    // For this task, avoiding changing visibility if not needed.
    // Alternatively, verify the output logic by replicating it or modifying DataLogger to be more testable.
    // Actually, DataLogger methods are private. Let's make them internal for testing or just test the public log?
    // DataLogger.log calls Timber, which is hard to mock in unit tests without setup.
    // Better approach: Make the formatting functions internal or public for testing purposes.
    // But since I already wrote the file, let's use reflection or modify the file to make them internal.
    // I'll assume I can use reflection for now to avoid modifying source again just for visibility props if possible,
    // BUT modifying to 'internal' is cleaner.
    // Let's modify DataLogger.kt to make methods 'internal' first.
    
    // STARTING WITH REFLECTION APPROACH TO AVOID MODIFYING SOURCE IMMEDIATELY
    // IF TOO COMPLEX, I WILL SWITCH.
    
    @Test
    fun testHexFormatting() {
        val method = DataLogger.javaClass.getDeclaredMethod("toHexString", ByteArray::class.java)
        method.isAccessible = true
        
        val input = byteArrayOf(0x0A, 0xFF.toByte(), 0x00)
        val result = method.invoke(DataLogger, input) as String
        assertEquals("0A FF 00", result)
    }

    @Test
    fun testBinaryFormatting() {
        val method = DataLogger.javaClass.getDeclaredMethod("toBinaryString", ByteArray::class.java)
        method.isAccessible = true
        
        val input = byteArrayOf(0x0F, 0x80.toByte())
        val result = method.invoke(DataLogger, input) as String
        // 0x0F = 00001111, 0x80 = 10000000
        assertEquals("00001111 10000000", result)
    }

    @Test
    fun testAsciiFormatting() {
        val method = DataLogger.javaClass.getDeclaredMethod("toAsciiString", ByteArray::class.java)
        method.isAccessible = true
        
        // 'A', 'B', newline (10), space (32)
        val input = byteArrayOf(65, 66, 10, 32) 
        val result = method.invoke(DataLogger, input) as String
        // 65='A', 66='B', 10='.', 32=' '
        assertEquals("AB. ", result)
    }
}
