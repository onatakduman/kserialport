package com.onatakduman.kserialport

import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SerialPortConnection(private val fd: FileDescriptor) : Closeable {

    private val fileInputStream = FileInputStream(fd)
    private val fileOutputStream = FileOutputStream(fd)

    val inputStream = fileInputStream
    val outputStream = fileOutputStream

    /**
     * A hot flow that emits data as it arrives from the serial port. This flow runs on
     * Dispatchers.IO.
     */
    val readFlow: Flow<ByteArray> =
            flow {
                        val buffer = ByteArray(1024)
                        try {
                            while (true) {
                                val size = fileInputStream.read(buffer)
                                if (size > 0) {
                                    emit(buffer.copyOf(size))
                                } else if (size < 0) {
                                    break // End of stream
                                }
                            }
                        } catch (e: IOException) {
                            // Handle or rethrow, depending on desired behavior.
                            // For a flow, usually we let the collector handle exceptions or use
                            // catch operator.
                            throw e
                        }
                    }
                    .flowOn(Dispatchers.IO)

    /**
     * Writes data to the serial port in a thread-safe manner (handled by OS/Stream). This function
     * suspends to offload the blocking I/O to Dispatchers.IO.
     */
    suspend fun write(data: ByteArray) =
            withContext(Dispatchers.IO) {
                fileOutputStream.write(data)
                fileOutputStream.flush()
            }

    override fun close() {
        try {
            fileInputStream.close()
            fileOutputStream.close()
            SerialPortJNI.close(fd)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
