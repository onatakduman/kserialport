package com.onatakduman.kserialport.app.data.model

import org.json.JSONObject

data class AutoReplyRule(
    val trigger: String,
    val response: String,
    val enabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("trigger", trigger)
        put("response", response)
        put("enabled", enabled)
    }

    companion object {
        fun fromJson(json: JSONObject): AutoReplyRule = AutoReplyRule(
            trigger = json.getString("trigger"),
            response = json.getString("response"),
            enabled = json.optBoolean("enabled", true)
        )
    }
}
