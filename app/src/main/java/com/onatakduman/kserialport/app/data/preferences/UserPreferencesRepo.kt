package com.onatakduman.kserialport.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.onatakduman.kserialport.app.data.model.AutoReplyRule
import com.onatakduman.kserialport.app.data.model.ConnectionProfile
import com.onatakduman.kserialport.app.data.model.DisplayMode
import com.onatakduman.kserialport.app.data.model.LineEnding
import com.onatakduman.kserialport.app.data.model.SerialConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepo(private val context: Context) {

    private object Keys {
        val DEVICE_PATH = stringPreferencesKey("device_path")
        val BAUD_RATE = intPreferencesKey("baud_rate")
        val DATA_BITS = intPreferencesKey("data_bits")
        val STOP_BITS = intPreferencesKey("stop_bits")
        val PARITY = intPreferencesKey("parity")
        val LINE_ENDING = stringPreferencesKey("line_ending")
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
        val DARK_THEME = stringPreferencesKey("dark_theme") // "system", "dark", "light"
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val AUTO_SCROLL = booleanPreferencesKey("auto_scroll")
        val SHOW_TIMESTAMPS = booleanPreferencesKey("show_timestamps")
        val RECENT_CONNECTIONS = stringPreferencesKey("recent_connections")
        val PRO_PURCHASED = booleanPreferencesKey("pro_purchased")
        val MACROS = stringPreferencesKey("macros")
        val SHOW_TOOLBAR = booleanPreferencesKey("show_toolbar")
        val SHOW_MACROS = booleanPreferencesKey("show_macros")
        val PROFILES = stringPreferencesKey("profiles")
        val AUTO_REPLY_RULES = stringPreferencesKey("auto_reply_rules")
    }

    val serialConfig: Flow<SerialConfig> = context.dataStore.data.map { prefs ->
        SerialConfig(
            path = prefs[Keys.DEVICE_PATH] ?: "/dev/ttyUSB0",
            baudRate = prefs[Keys.BAUD_RATE] ?: 115200,
            dataBits = prefs[Keys.DATA_BITS] ?: 8,
            stopBits = prefs[Keys.STOP_BITS] ?: 1,
            parity = prefs[Keys.PARITY] ?: 0,
            lineEnding = try {
                LineEnding.valueOf(prefs[Keys.LINE_ENDING] ?: "CRLF")
            } catch (_: Exception) {
                LineEnding.CRLF
            }
        )
    }

    val displayMode: Flow<DisplayMode> = context.dataStore.data.map { prefs ->
        try {
            DisplayMode.valueOf(prefs[Keys.DISPLAY_MODE] ?: "ASCII")
        } catch (_: Exception) {
            DisplayMode.ASCII
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: "dark"
    }

    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DYNAMIC_COLORS] ?: false
    }

    val autoScroll: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_SCROLL] ?: true
    }

    val showTimestamps: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_TIMESTAMPS] ?: true
    }

    val recentConnections: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.RECENT_CONNECTIONS]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
    }

    val proPurchased: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRO_PURCHASED] ?: false
    }

    val macros: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.MACROS]?.split("|")?.filter { it.isNotBlank() }
            ?: listOf("AT", "AT+RST", "PING", "TEST")
    }

    val showToolbar: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_TOOLBAR] ?: true
    }

    val showMacros: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_MACROS] ?: true
    }

    suspend fun saveShowToolbar(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_TOOLBAR] = show
        }
    }

    suspend fun saveShowMacros(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_MACROS] = show
        }
    }

    suspend fun saveSerialConfig(config: SerialConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEVICE_PATH] = config.path
            prefs[Keys.BAUD_RATE] = config.baudRate
            prefs[Keys.DATA_BITS] = config.dataBits
            prefs[Keys.STOP_BITS] = config.stopBits
            prefs[Keys.PARITY] = config.parity
            prefs[Keys.LINE_ENDING] = config.lineEnding.name
        }
    }

    suspend fun saveDisplayMode(mode: DisplayMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_MODE] = mode.name
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = mode
        }
    }

    suspend fun saveDynamicColors(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun saveAutoScroll(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_SCROLL] = enabled
        }
    }

    suspend fun saveShowTimestamps(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_TIMESTAMPS] = enabled
        }
    }

    suspend fun addRecentConnection(connectionString: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_CONNECTIONS]?.split("|")
                ?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            current.remove(connectionString)
            current.add(0, connectionString)
            val trimmed = current.take(10)
            prefs[Keys.RECENT_CONNECTIONS] = trimmed.joinToString("|")
        }
    }

    suspend fun saveProPurchased(purchased: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRO_PURCHASED] = purchased
        }
    }

    suspend fun saveMacros(macroList: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MACROS] = macroList.joinToString("|")
        }
    }

    // --- Connection Profiles ---

    val profiles: Flow<List<ConnectionProfile>> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.PROFILES] ?: return@map emptyList()
        try {
            val array = JSONArray(json)
            (0 until array.length()).map { ConnectionProfile.fromJson(array.getJSONObject(it)) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveProfile(profile: ConnectionProfile) {
        context.dataStore.edit { prefs ->
            val current = try {
                JSONArray(prefs[Keys.PROFILES] ?: "[]")
            } catch (_: Exception) {
                JSONArray()
            }
            // Remove existing profile with same name
            val filtered = JSONArray()
            for (i in 0 until current.length()) {
                val obj = current.getJSONObject(i)
                if (obj.optString("name") != profile.name) {
                    filtered.put(obj)
                }
            }
            filtered.put(profile.toJson())
            prefs[Keys.PROFILES] = filtered.toString()
        }
    }

    suspend fun deleteProfile(name: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                JSONArray(prefs[Keys.PROFILES] ?: "[]")
            } catch (_: Exception) {
                JSONArray()
            }
            val filtered = JSONArray()
            for (i in 0 until current.length()) {
                val obj = current.getJSONObject(i)
                if (obj.optString("name") != name) {
                    filtered.put(obj)
                }
            }
            prefs[Keys.PROFILES] = filtered.toString()
        }
    }

    // --- Auto-Reply Rules ---

    val autoReplyRules: Flow<List<AutoReplyRule>> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.AUTO_REPLY_RULES] ?: return@map emptyList()
        try {
            val array = JSONArray(json)
            (0 until array.length()).map { AutoReplyRule.fromJson(array.getJSONObject(it)) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveAutoReplyRules(rules: List<AutoReplyRule>) {
        context.dataStore.edit { prefs ->
            val array = JSONArray()
            rules.forEach { array.put(it.toJson()) }
            prefs[Keys.AUTO_REPLY_RULES] = array.toString()
        }
    }
}
