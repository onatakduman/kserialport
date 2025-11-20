package com.onatakduman.serialport

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device. Verifies that the native library can
 * be loaded.
 */
@RunWith(AndroidJUnit4::class)
class SerialPortInstrumentedTest {
    @Test
    fun testLibraryLoading() {
        // This will trigger the static initializer of SerialPortJNI, which loads the library.
        // If the library is missing or has unsatisfied link errors, this will crash/fail.
        try {
            Class.forName("com.onatakduman.serialport.SerialPortJNI")
            // If we get here, the class loaded, which means the static block ran.
            // We can't easily call methods without a real serial port, but loading is a good first
            // step.
        } catch (e: ClassNotFoundException) {
            fail("SerialPortJNI class not found")
        } catch (e: UnsatisfiedLinkError) {
            fail("Native library failed to load: ${e.message}")
        }
    }
}
