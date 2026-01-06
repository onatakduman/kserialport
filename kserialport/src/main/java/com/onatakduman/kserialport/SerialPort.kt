package com.onatakduman.kserialport

import java.io.IOException

class SerialPort(
    val path: String,
    val baudRate: Int,
    val dataBits: Int = DATA_BITS_8,
    val stopBits: Int = STOP_BITS_1,
    val parity: Int = PARITY_NONE
) {

    companion object {
        const val DATA_BITS_5 = 5
        const val DATA_BITS_6 = 6
        const val DATA_BITS_7 = 7
        const val DATA_BITS_8 = 8

        const val STOP_BITS_1 = 1
        const val STOP_BITS_2 = 2

        const val PARITY_NONE = 0
        const val PARITY_ODD = 1
        const val PARITY_EVEN = 2
    }

    fun open(): SerialPortConnection {
        if (!RootPermissionHelper.grantPermission(path)) {
            throw SecurityException(
                    "Cannot read/write to $path. Ensure you have root access or permissions are set."
            )
        }

        val fd =
                SerialPortJNI.open(path, 0)
                        ?: throw IOException("Failed to open serial port: $path")

        if (!SerialPortJNI.configure(fd, baudRate, dataBits, stopBits, parity)) {
            try {
                SerialPortJNI.close(fd)
            } catch (e: Exception) {
                // Ignore close errors during cleanup
            }
            throw IOException("Failed to configure serial port")
        }

        return SerialPortConnection(fd)
    }
}
