package com.onatakduman.kserialport

import java.io.IOException

/**
 * Represents a serial port configuration.
 *
 * @param path The device path (e.g., "/dev/ttyUSB0")
 * @param baudRate The baud rate (use BAUDRATE_* constants)
 * @param dataBits Number of data bits (5, 6, 7, or 8)
 * @param stopBits Number of stop bits (1 or 2)
 * @param parity Parity mode (PARITY_NONE, PARITY_ODD, or PARITY_EVEN)
 * @throws IllegalArgumentException if any parameter is invalid
 */
class SerialPort(
    val path: String,
    val baudRate: Int = BAUDRATE_115200,
    val dataBits: Int = DATA_BITS_8,
    val stopBits: Int = STOP_BITS_1,
    val parity: Int = PARITY_NONE
) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
        require(baudRate in VALID_BAUD_RATES) {
            "Invalid baud rate: $baudRate. Valid rates: $VALID_BAUD_RATES"
        }
        require(dataBits in DATA_BITS_5..DATA_BITS_8) {
            "Invalid data bits: $dataBits. Must be 5, 6, 7, or 8"
        }
        require(stopBits in STOP_BITS_1..STOP_BITS_2) {
            "Invalid stop bits: $stopBits. Must be 1 or 2"
        }
        require(parity in PARITY_NONE..PARITY_EVEN) {
            "Invalid parity: $parity. Must be PARITY_NONE (0), PARITY_ODD (1), or PARITY_EVEN (2)"
        }
    }

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

        const val BAUDRATE_0 = 0
        const val BAUDRATE_50 = 50
        const val BAUDRATE_75 = 75
        const val BAUDRATE_110 = 110
        const val BAUDRATE_134 = 134
        const val BAUDRATE_150 = 150
        const val BAUDRATE_200 = 200
        const val BAUDRATE_300 = 300
        const val BAUDRATE_600 = 600
        const val BAUDRATE_1200 = 1200
        const val BAUDRATE_1800 = 1800
        const val BAUDRATE_2400 = 2400
        const val BAUDRATE_4800 = 4800
        const val BAUDRATE_9600 = 9600
        const val BAUDRATE_19200 = 19200
        const val BAUDRATE_38400 = 38400
        const val BAUDRATE_57600 = 57600
        const val BAUDRATE_115200 = 115200
        const val BAUDRATE_230400 = 230400
        const val BAUDRATE_460800 = 460800
        const val BAUDRATE_500000 = 500000
        const val BAUDRATE_576000 = 576000
        const val BAUDRATE_921600 = 921600
        const val BAUDRATE_1000000 = 1000000
        const val BAUDRATE_1152000 = 1152000
        const val BAUDRATE_1500000 = 1500000
        const val BAUDRATE_2000000 = 2000000
        const val BAUDRATE_2500000 = 2500000
        const val BAUDRATE_3000000 = 3000000
        const val BAUDRATE_3500000 = 3500000
        const val BAUDRATE_4000000 = 4000000

        private val VALID_BAUD_RATES = setOf(
            BAUDRATE_0, BAUDRATE_50, BAUDRATE_75, BAUDRATE_110, BAUDRATE_134,
            BAUDRATE_150, BAUDRATE_200, BAUDRATE_300, BAUDRATE_600, BAUDRATE_1200,
            BAUDRATE_1800, BAUDRATE_2400, BAUDRATE_4800, BAUDRATE_9600, BAUDRATE_19200,
            BAUDRATE_38400, BAUDRATE_57600, BAUDRATE_115200, BAUDRATE_230400, BAUDRATE_460800,
            BAUDRATE_500000, BAUDRATE_576000, BAUDRATE_921600, BAUDRATE_1000000, BAUDRATE_1152000,
            BAUDRATE_1500000, BAUDRATE_2000000, BAUDRATE_2500000, BAUDRATE_3000000, BAUDRATE_3500000,
            BAUDRATE_4000000
        )
    }

    /**
     * Opens and configures the serial port.
     *
     * @return A [SerialPortConnection] for reading and writing data
     * @throws SecurityException if permission cannot be granted
     * @throws IOException if the port cannot be opened or configured
     */
    fun open(): SerialPortConnection {
        if (!RootPermissionHelper.grantPermission(path)) {
            throw SecurityException(
                "Cannot read/write to $path. Ensure you have root access or permissions are set."
            )
        }

        val fd = SerialPortJNI.open(path, 0)
            ?: throw IOException("Failed to open serial port: $path")

        if (!SerialPortJNI.configure(fd, baudRate, dataBits, stopBits, parity)) {
            try {
                SerialPortJNI.close(fd)
            } catch (_: Exception) {
                // Ignore close errors during cleanup
            }
            throw IOException("Failed to configure serial port: baudRate=$baudRate, dataBits=$dataBits, stopBits=$stopBits, parity=$parity")
        }

        return SerialPortConnection(fd)
    }
}
