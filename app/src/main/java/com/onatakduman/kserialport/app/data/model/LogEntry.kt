package com.onatakduman.kserialport.app.data.model

data class LogEntry(
    val timestamp: String,
    val type: LogType,
    val message: String
)

enum class LogType { SYSTEM, TX, RX, ERROR }

enum class DisplayMode { ASCII, HEX, SPLIT }
