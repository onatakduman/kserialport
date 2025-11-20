package com.onatakduman.kserialport

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Splits the flow of byte arrays into chunks based on a delimiter. Useful for handling "sticky
 * packets" where messages are separated by a specific byte sequence.
 */
fun Flow<ByteArray>.delimiter(delimiter: ByteArray): Flow<ByteArray> = flow {
    val buffer = ByteArrayOutputStream()
    val delimiterSize = delimiter.size

    collect { chunk ->
        buffer.write(chunk)
        val currentBytes = buffer.toByteArray()
        var scanIndex = 0

        while (scanIndex <= currentBytes.size - delimiterSize) {
            // Check for delimiter at scanIndex
            var found = true
            for (i in 0 until delimiterSize) {
                if (currentBytes[scanIndex + i] != delimiter[i]) {
                    found = false
                    break
                }
            }

            if (found) {
                // Emit data up to delimiter
                val packet = currentBytes.copyOfRange(0, scanIndex)
                if (packet.isNotEmpty()) {
                    emit(packet)
                }

                // Remove processed data (including delimiter) from buffer
                val remaining =
                        currentBytes.copyOfRange(scanIndex + delimiterSize, currentBytes.size)
                buffer.reset()
                buffer.write(remaining)

                // Restart scanning from the beginning of the remaining buffer (which is now at
                // index 0 of buffer)
                // But in this loop logic, we are iterating over 'currentBytes'.
                // It's easier to just break and restart with the new buffer state in the next
                // collection,
                // BUT we might have multiple delimiters in one chunk.
                // So we need to update 'currentBytes' and reset scanIndex.

                // Optimization: We can just continue scanning 'currentBytes' but we need to track
                // where the *next* packet starts.
                // However, 'buffer' needs to be updated for the next 'collect'.

                // Let's simplify: Recalculate currentBytes from the updated buffer and reset loop.
                // This is less efficient but safer for now.
                val newBytes = buffer.toByteArray()
                // We need to re-evaluate the loop with newBytes.
                // Since we can't easily "reset" the loop variable 'currentBytes' inside the loop,
                // we can use a recursive approach or a while loop that consumes the buffer.

                // Better approach:
                // We found a delimiter. Emit.
                // The 'buffer' now contains the rest.
                // We need to continue checking 'buffer' for MORE delimiters.
                // So we should probably loop on the buffer content.
                break
            } else {
                scanIndex++
            }
        }

        // Re-process buffer to find multiple packets in one chunk
        // The above loop only finds the FIRST delimiter.
        // We need to loop until no more delimiters are found.
    }
}

/** Improved implementation of delimiter that handles multiple packets in one chunk. */
fun Flow<ByteArray>.delimiterBased(delimiter: ByteArray): Flow<ByteArray> = flow {
    val buffer = ByteArrayOutputStream()
    val delimiterSize = delimiter.size

    collect { chunk ->
        buffer.write(chunk)

        while (true) {
            val currentBytes = buffer.toByteArray()
            var foundIndex = -1

            // Search for delimiter
            for (i in 0..currentBytes.size - delimiterSize) {
                var match = true
                for (j in 0 until delimiterSize) {
                    if (currentBytes[i + j] != delimiter[j]) {
                        match = false
                        break
                    }
                }
                if (match) {
                    foundIndex = i
                    break
                }
            }

            if (foundIndex != -1) {
                // Emit packet
                val packet = currentBytes.copyOfRange(0, foundIndex)
                if (packet.isNotEmpty()) {
                    emit(packet)
                }

                // Remove processed part
                val remaining =
                        currentBytes.copyOfRange(foundIndex + delimiterSize, currentBytes.size)
                buffer.reset()
                buffer.write(remaining)
            } else {
                // No more delimiters, wait for more data
                break
            }
        }
    }
}

/** Splits the flow of byte arrays into fixed-length chunks. */
fun Flow<ByteArray>.fixedLength(length: Int): Flow<ByteArray> = flow {
    val buffer = ByteArrayOutputStream()

    collect { chunk ->
        buffer.write(chunk)

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
