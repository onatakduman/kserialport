package com.onatakduman.kserialport.app.data.model

import org.json.JSONArray
import org.json.JSONObject

data class ConnectionProfile(
    val name: String,
    val config: SerialConfig,
    val macros: List<String> = listOf("AT", "AT+RST", "PING", "TEST")
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("path", config.path)
        put("baudRate", config.baudRate)
        put("dataBits", config.dataBits)
        put("stopBits", config.stopBits)
        put("parity", config.parity)
        put("lineEnding", config.lineEnding.name)
        put("macros", JSONArray(macros))
    }

    companion object {
        fun fromJson(json: JSONObject): ConnectionProfile {
            val macroArray = json.optJSONArray("macros")
            val macros = if (macroArray != null) {
                (0 until macroArray.length()).map { macroArray.getString(it) }
            } else {
                listOf("AT", "AT+RST", "PING", "TEST")
            }
            return ConnectionProfile(
                name = json.getString("name"),
                config = SerialConfig(
                    path = json.optString("path", "/dev/ttyUSB0"),
                    baudRate = json.optInt("baudRate", 115200),
                    dataBits = json.optInt("dataBits", 8),
                    stopBits = json.optInt("stopBits", 1),
                    parity = json.optInt("parity", 0),
                    lineEnding = try {
                        LineEnding.valueOf(json.optString("lineEnding", "CRLF"))
                    } catch (_: Exception) {
                        LineEnding.CRLF
                    }
                ),
                macros = macros
            )
        }
    }
}
