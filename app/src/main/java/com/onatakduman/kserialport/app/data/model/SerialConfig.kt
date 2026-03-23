package com.onatakduman.kserialport.app.data.model

data class SerialConfig(
    val path: String = "/dev/ttyUSB0",
    val baudRate: Int = 115200,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: Int = 0,
    val lineEnding: LineEnding = LineEnding.CRLF
)

enum class LineEnding(val bytes: ByteArray) {
    NONE(byteArrayOf()),
    CR(byteArrayOf(0x0D)),
    LF(byteArrayOf(0x0A)),
    CRLF(byteArrayOf(0x0D, 0x0A));

    val displayName: String
        get() = when (this) {
            NONE -> "None"
            CR -> "CR (\\r)"
            LF -> "LF (\\n)"
            CRLF -> "CRLF (\\r\\n)"
        }
}
