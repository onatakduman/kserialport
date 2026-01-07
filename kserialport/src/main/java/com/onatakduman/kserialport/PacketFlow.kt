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
