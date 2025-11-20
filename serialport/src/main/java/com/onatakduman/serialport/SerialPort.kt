package com.onatakduman.serialport

import java.io.IOException

class SerialPort(val path: String, val baudRate: Int) {

    fun open(): SerialPortConnection {
        if (!RootPermissionHelper.grantPermission(path)) {
            throw SecurityException(
                    "Cannot read/write to $path. Ensure you have root access or permissions are set."
            )
        }

        val fd =
                SerialPortJNI.open(path, 0)
                        ?: throw IOException("Failed to open serial port: $path")

        // Default configuration: 8N1
        if (!SerialPortJNI.configure(fd, baudRate, 8, 1, 0)) {
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
