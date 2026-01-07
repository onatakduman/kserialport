package com.onatakduman.kserialport

import android.util.Log
import java.io.File

class SerialPortFinder {

    class Driver(val name: String, val deviceRoot: String) {
        private var _devices: List<File>? = null

        fun getDevices(): List<File> {
            if (_devices == null) {
                _devices = File("/dev").listFiles()
                    ?.filter { it.absolutePath.startsWith(deviceRoot) }
                    ?.onEach { Log.d(TAG, "Found device: $it") }
                    ?: emptyList()
            }
            return _devices!!
        }
    }

    private var _drivers: List<Driver>? = null

    private fun loadDrivers(): List<Driver> {
        return try {
            File("/proc/tty/drivers").useLines { lines ->
                lines.mapNotNull { line ->
                    parseDriverLine(line)
                }.toList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to read /proc/tty/drivers: ${e.message}. Using fallback drivers")
            getDefaultDrivers()
        }
    }

    private fun parseDriverLine(line: String): Driver? {
        // Driver name may contain spaces, extract from fixed position (first 21 chars)
        if (line.length < 21) return null

        val driverName = line.substring(0, 21).trim()
        val parts = line.split(Regex(" +")).filter { it.isNotEmpty() }

        return if (parts.size >= 5 && parts.last() == "serial") {
            val deviceRoot = parts[parts.size - 4]
            Log.d(TAG, "Found driver $driverName on $deviceRoot")
            Driver(driverName, deviceRoot)
        } else {
            null
        }
    }

    private fun getDefaultDrivers(): List<Driver> = listOf(
        Driver("Serial", "/dev/ttyS"),
        Driver("USB", "/dev/ttyUSB"),
        Driver("ACM", "/dev/ttyACM"),
        Driver("AMA", "/dev/ttyAMA"),
        Driver("MXC", "/dev/ttymxc"),
        Driver("HS", "/dev/ttyHS"),
        Driver("SAC", "/dev/ttySAC")
    )

    fun getDrivers(): List<Driver> {
        if (_drivers == null) {
            _drivers = loadDrivers()
        }
        return _drivers!!
    }

    fun getAllDevices(): Array<String> {
        return try {
            getDrivers().flatMap { driver ->
                driver.getDevices().map { device ->
                    "${device.name} (${driver.name})"
                }
            }.toTypedArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting devices", e)
            emptyArray()
        }
    }

    fun getAllDevicesPath(): Array<String> {
        return try {
            getDrivers().flatMap { driver ->
                driver.getDevices().map { it.absolutePath }
            }.toTypedArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device paths", e)
            emptyArray()
        }
    }

    companion object {
        private const val TAG = "SerialPortFinder"
    }
}
