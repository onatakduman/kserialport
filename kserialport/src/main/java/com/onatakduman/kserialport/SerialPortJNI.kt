package com.onatakduman.kserialport

import java.io.FileDescriptor

internal object SerialPortJNI {
    init {
        System.loadLibrary("serialport")
    }

    external fun open(path: String, flags: Int): FileDescriptor?
    external fun close(fd: FileDescriptor)
    external fun configure(
            fd: FileDescriptor,
            baudRate: Int,
            dataBits: Int,
            stopBits: Int,
            parity: Int
    ): Boolean
}
