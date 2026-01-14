package com.onatakduman.kserialport

import timber.log.Timber

/**
 * DataLogger provides utilities for logging byte arrays in various formats.
 * Supported formats: Hex, Binary, ASCII.
 */
object DataLogger {

    enum class Format {
        HEX,
        BINARY,
        ASCII
    }

    /**
     * Logs the given data with the specified tag and format.
     *
     * @param tag The tag for the log message.
     * @param data The byte array to log.
     * @param format The format to use for logging (HEX, BINARY, ASCII).
     */
    fun log(tag: String, data: ByteArray, format: Format = Format.HEX) {
        if (data.isEmpty()) return

        val formattedData = when (format) {
            Format.HEX -> toHexString(data)
            Format.BINARY -> toBinaryString(data)
            Format.ASCII -> toAsciiString(data)
        }

        Timber.tag(tag).d("[%s] %s", format.name, formattedData)
    }

    private fun toHexString(data: ByteArray): String {
        return data.joinToString(separator = " ") { "%02X".format(it) }
    }

    private fun toBinaryString(data: ByteArray): String {
        return data.joinToString(separator = " ") {
            String.format("%8s", Integer.toBinaryString(it.toInt() and 0xFF)).replace(' ', '0')
        }
    }

    private fun toAsciiString(data: ByteArray): String {
        return data.map { byte ->
            val char = byte.toInt().toChar()
            if (isPrintable(char)) char else '.'
        }.joinToString(separator = "")
    }

    private fun isPrintable(char: Char): Boolean {
        // Simple check for printable ASCII: space (32) to tilde (126)
        return char.code in 32..126
    }
}
