package com.onatakduman.kserialport

import android.util.Log
import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InterruptedIOException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SerialPortConnection(private val fd: FileDescriptor) : Closeable {

    private val fileInputStream: FileInputStream
    private val fileOutputStream: FileOutputStream

    init {
        // Initialize streams with proper error handling to prevent resource leaks
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(fd)
            fileOutputStream = FileOutputStream(fd)
            fileInputStream = inputStream
        } catch (e: Exception) {
            inputStream?.close()
            throw e
        }
    }

    val inputStream: FileInputStream get() = fileInputStream
    val outputStream: FileOutputStream get() = fileOutputStream

    /**
     * A hot flow that emits data as it arrives from the serial port. This flow runs on
     * Dispatchers.IO.
     *
     * Note: This is a hot flow - collectors that join after data has been emitted will miss
     * that data. Consider using SharedFlow if you need multiple collectors.
     */
    val readFlow: Flow<ByteArray> =
            flow {
                        val buffer = ByteArray(1024)
                        try {
                            while (true) {
                                val size = fileInputStream.read(buffer)
                                if (size > 0) {
                                    emit(buffer.copyOf(size))
                                } else {
                                    // size <= 0 means EOF or error, exit the loop
                                    break
                                }
                            }
                        } catch (e: InterruptedIOException) {
                            // Coroutine was cancelled, end the flow gracefully
                            Log.d(TAG, "Read flow interrupted (coroutine cancelled)")
                        } catch (e: IOException) {
                            // Rethrow to let collectors handle via catch operator
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
        } catch (e: IOException) {
            Log.w(TAG, "Error closing input stream", e)
        }
        try {
            fileOutputStream.close()
        } catch (e: IOException) {
            Log.w(TAG, "Error closing output stream", e)
        }
        try {
            SerialPortJNI.close(fd)
        } catch (e: Exception) {
            Log.w(TAG, "Error closing file descriptor", e)
        }
    }

    companion object {
        private const val TAG = "SerialPortConnection"
    }
}
