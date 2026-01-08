package com.onatakduman.kserialport

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Default maximum buffer size (1 MB) to prevent out-of-memory errors */
const val DEFAULT_MAX_BUFFER_SIZE = 1024 * 1024

/** Exception thrown when the packet buffer exceeds the maximum allowed size */
class BufferOverflowException(message: String) : Exception(message)

/**
 * Splits the flow of byte arrays into chunks based on a delimiter. Useful for handling "sticky
 * packets" where messages are separated by a specific byte sequence.
 *
 * @param delimiter The byte sequence that separates packets
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize without finding a delimiter
 */
fun Flow<ByteArray>.delimiter(
    delimiter: ByteArray,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = flow {
    require(delimiter.isNotEmpty()) { "Delimiter cannot be empty" }
    require(maxBufferSize > 0) { "maxBufferSize must be positive" }

    val buffer = ByteArrayOutputStream()
    val delimiterSize = delimiter.size

    collect { chunk ->
        buffer.write(chunk)

        // Check buffer size limit
        if (buffer.size() > maxBufferSize) {
            throw BufferOverflowException(
                "Buffer size ${buffer.size()} exceeded maximum $maxBufferSize bytes without finding delimiter"
            )
        }

        // Process all complete packets in buffer
        while (true) {
            val currentBytes = buffer.toByteArray()
            val foundIndex = findDelimiter(currentBytes, delimiter)

            if (foundIndex != -1) {
                // Emit packet (data before delimiter)
                val packet = currentBytes.copyOfRange(0, foundIndex)
                if (packet.isNotEmpty()) {
                    emit(packet)
                }

                // Keep remaining data after delimiter
                val remaining = currentBytes.copyOfRange(foundIndex + delimiterSize, currentBytes.size)
                buffer.reset()
                buffer.write(remaining)
            } else {
                // No more delimiters found, wait for more data
                break
            }
        }
    }
}

/**
 * Splits the flow of byte arrays into chunks based on a delimiter.
 * Alias for [delimiter] for backwards compatibility.
 *
 * @param delimiter The byte sequence that separates packets
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize without finding a delimiter
 */
fun Flow<ByteArray>.delimiterBased(
    delimiter: ByteArray,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = delimiter(delimiter, maxBufferSize)

/**
 * Splits the flow of byte arrays into fixed-length chunks.
 *
 * @param length The fixed length of each packet
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize
 */
fun Flow<ByteArray>.fixedLength(
    length: Int,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = flow {
    require(length > 0) { "Length must be positive" }
    require(maxBufferSize >= length) { "maxBufferSize must be >= length" }

    val buffer = ByteArrayOutputStream()

    collect { chunk ->
        buffer.write(chunk)

        // Check buffer size limit
        if (buffer.size() > maxBufferSize) {
            throw BufferOverflowException(
                "Buffer size ${buffer.size()} exceeded maximum $maxBufferSize bytes"
            )
        }

        while (buffer.size() >= length) {
            val currentBytes = buffer.toByteArray()
            val packet = currentBytes.copyOfRange(0, length)
            emit(packet)

            val remaining = currentBytes.copyOfRange(length, currentBytes.size)
            buffer.reset()
            buffer.write(remaining)
        }
    }
}

/**
 * Splits the flow of byte arrays into packets based on start and end markers.
 * Useful for protocols that frame packets with specific sequences (e.g., STX/ETX).
 *
 * @param startMarker The byte sequence that marks the start of a packet
 * @param endMarker The byte sequence that marks the end of a packet
 * @param includeMarkers If true, include start/end markers in emitted packets (default false)
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize without finding a complete packet
 */
fun Flow<ByteArray>.startEndMarker(
    startMarker: ByteArray,
    endMarker: ByteArray,
    includeMarkers: Boolean = false,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = flow {
    require(startMarker.isNotEmpty()) { "Start marker cannot be empty" }
    require(endMarker.isNotEmpty()) { "End marker cannot be empty" }
    require(maxBufferSize > 0) { "maxBufferSize must be positive" }

    val buffer = ByteArrayOutputStream()

    collect { chunk ->
        buffer.write(chunk)

        // Check buffer size limit
        if (buffer.size() > maxBufferSize) {
            throw BufferOverflowException(
                "Buffer size ${buffer.size()} exceeded maximum $maxBufferSize bytes without finding complete packet"
            )
        }

        // Process all complete packets in buffer
        while (true) {
            val currentBytes = buffer.toByteArray()
            val startIndex = findDelimiter(currentBytes, startMarker)

            if (startIndex == -1) {
                // No start marker found, discard data before any potential partial start marker
                buffer.reset()
                break
            }

            // Look for end marker after start marker
            val searchStart = startIndex + startMarker.size
            if (searchStart >= currentBytes.size) break

            val endIndex = findDelimiter(
                currentBytes.copyOfRange(searchStart, currentBytes.size),
                endMarker
            )

            if (endIndex == -1) {
                // No end marker yet, keep waiting
                // Discard data before start marker
                if (startIndex > 0) {
                    val remaining = currentBytes.copyOfRange(startIndex, currentBytes.size)
                    buffer.reset()
                    buffer.write(remaining)
                }
                break
            }

            // Found complete packet
            val actualEndIndex = searchStart + endIndex
            val packet = if (includeMarkers) {
                currentBytes.copyOfRange(startIndex, actualEndIndex + endMarker.size)
            } else {
                currentBytes.copyOfRange(startIndex + startMarker.size, actualEndIndex)
            }

            if (packet.isNotEmpty() || includeMarkers) {
                emit(packet)
            }

            // Keep remaining data after end marker
            val remaining = currentBytes.copyOfRange(actualEndIndex + endMarker.size, currentBytes.size)
            buffer.reset()
            buffer.write(remaining)
        }
    }
}

/**
 * Splits the flow of byte arrays into packets based on a length prefix.
 * Common in binary protocols where the first N bytes indicate packet length.
 *
 * @param lengthFieldOffset Offset of the length field from the start of the packet (default 0)
 * @param lengthFieldSize Size of the length field in bytes (1, 2, or 4, default 2)
 * @param lengthIncludesHeader If true, length includes the header bytes (default false)
 * @param headerSize Total header size (default equals lengthFieldOffset + lengthFieldSize)
 * @param bigEndian If true, length is big-endian; if false, little-endian (default true)
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize
 */
fun Flow<ByteArray>.lengthPrefixed(
    lengthFieldOffset: Int = 0,
    lengthFieldSize: Int = 2,
    lengthIncludesHeader: Boolean = false,
    headerSize: Int = lengthFieldOffset + lengthFieldSize,
    bigEndian: Boolean = true,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = flow {
    require(lengthFieldSize in listOf(1, 2, 4)) { "lengthFieldSize must be 1, 2, or 4" }
    require(lengthFieldOffset >= 0) { "lengthFieldOffset must be non-negative" }
    require(headerSize >= lengthFieldOffset + lengthFieldSize) { "headerSize must be >= lengthFieldOffset + lengthFieldSize" }
    require(maxBufferSize > 0) { "maxBufferSize must be positive" }

    val buffer = ByteArrayOutputStream()

    collect { chunk ->
        buffer.write(chunk)

        // Check buffer size limit
        if (buffer.size() > maxBufferSize) {
            throw BufferOverflowException(
                "Buffer size ${buffer.size()} exceeded maximum $maxBufferSize bytes"
            )
        }

        while (buffer.size() >= headerSize) {
            val currentBytes = buffer.toByteArray()

            // Read length from the appropriate position
            val lengthBytes = currentBytes.copyOfRange(
                lengthFieldOffset,
                lengthFieldOffset + lengthFieldSize
            )
            val dataLength = readLength(lengthBytes, bigEndian)

            val totalPacketSize = if (lengthIncludesHeader) {
                dataLength
            } else {
                headerSize + dataLength
            }

            if (totalPacketSize <= 0 || totalPacketSize > maxBufferSize) {
                throw BufferOverflowException(
                    "Invalid packet length: $totalPacketSize (max: $maxBufferSize)"
                )
            }

            if (currentBytes.size >= totalPacketSize) {
                val packet = currentBytes.copyOfRange(0, totalPacketSize)
                emit(packet)

                val remaining = currentBytes.copyOfRange(totalPacketSize, currentBytes.size)
                buffer.reset()
                buffer.write(remaining)
            } else {
                // Not enough data yet
                break
            }
        }
    }
}

/**
 * Interface for custom packet parsing logic.
 * Implement this to define your own packet detection algorithm.
 */
fun interface PacketParser {
    /**
     * Attempts to extract a complete packet from the buffer.
     *
     * @param buffer The current accumulated data
     * @return A ParseResult indicating success with packet and remaining bytes, or null if no complete packet yet
     */
    fun parse(buffer: ByteArray): ParseResult?

    /**
     * Result of a parse operation.
     *
     * @property packet The extracted complete packet
     * @property remaining The remaining bytes after the packet (to keep in buffer)
     */
    data class ParseResult(
        val packet: ByteArray,
        val remaining: ByteArray
    )
}

/**
 * Splits the flow of byte arrays using a custom PacketParser.
 * Use this when built-in methods don't fit your protocol.
 *
 * @param parser Custom parser implementation
 * @param maxBufferSize Maximum buffer size before throwing BufferOverflowException (default 1MB)
 * @throws BufferOverflowException if buffer exceeds maxBufferSize without finding a packet
 */
fun Flow<ByteArray>.customParser(
    parser: PacketParser,
    maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE
): Flow<ByteArray> = flow {
    require(maxBufferSize > 0) { "maxBufferSize must be positive" }

    val buffer = ByteArrayOutputStream()

    collect { chunk ->
        buffer.write(chunk)

        // Check buffer size limit
        if (buffer.size() > maxBufferSize) {
            throw BufferOverflowException(
                "Buffer size ${buffer.size()} exceeded maximum $maxBufferSize bytes"
            )
        }

        // Keep parsing while we find complete packets
        while (true) {
            val currentBytes = buffer.toByteArray()
            val result = parser.parse(currentBytes)

            if (result != null) {
                emit(result.packet)
                buffer.reset()
                buffer.write(result.remaining)
            } else {
                break
            }
        }
    }
}

/**
 * Finds the index of the first occurrence of delimiter in data.
 * Returns -1 if not found.
 */
private fun findDelimiter(data: ByteArray, delimiter: ByteArray): Int {
    if (data.size < delimiter.size) return -1

    outer@ for (i in 0..data.size - delimiter.size) {
        for (j in delimiter.indices) {
            if (data[i + j] != delimiter[j]) {
                continue@outer
            }
        }
        return i
    }
    return -1
}

/**
 * Reads an integer from a byte array with specified endianness.
 */
private fun readLength(bytes: ByteArray, bigEndian: Boolean): Int {
    return when (bytes.size) {
        1 -> bytes[0].toInt() and 0xFF
        2 -> if (bigEndian) {
            ((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)
        } else {
            ((bytes[1].toInt() and 0xFF) shl 8) or (bytes[0].toInt() and 0xFF)
        }
        4 -> if (bigEndian) {
            ((bytes[0].toInt() and 0xFF) shl 24) or
                    ((bytes[1].toInt() and 0xFF) shl 16) or
                    ((bytes[2].toInt() and 0xFF) shl 8) or
                    (bytes[3].toInt() and 0xFF)
        } else {
            ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
        }
        else -> throw IllegalArgumentException("Unsupported length field size: ${bytes.size}")
    }
}
