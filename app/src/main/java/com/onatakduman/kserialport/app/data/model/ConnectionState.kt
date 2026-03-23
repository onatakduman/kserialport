package com.onatakduman.kserialport.app.data.model

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val path: String, val baudRate: Int) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
