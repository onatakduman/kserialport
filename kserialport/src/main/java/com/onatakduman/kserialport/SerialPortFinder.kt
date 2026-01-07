package com.onatakduman.kserialport

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader
import java.util.Vector

class SerialPortFinder {

    data class Driver(val name: String, val deviceRoot: String) {
        private val mDevices = Vector<File>()

        fun getDevices(): Vector<File> {
            if (mDevices.isEmpty()) {
                val dev = File("/dev")
                val files = dev.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.absolutePath.startsWith(deviceRoot)) {
                            Log.d(TAG, "Found new device: $file")
                            mDevices.add(file)
                        }
                    }
                }
            }
            return mDevices
        }
    }

    private val mDrivers = Vector<Driver>()

    fun getDrivers(): Vector<Driver> {
        if (mDrivers.isEmpty()) {
            try {
                val r = LineNumberReader(FileReader("/proc/tty/drivers"))
                var l: String?
                while (r.readLine().also { l = it } != null) {
                    // Issue 3:
                    // Since driver name may contain spaces, we do not extract driver name with split()
                    val line = l ?: continue
                    val driverName = line.substring(0, 0x15).trim { it <= ' ' }
                    val w = line.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (w.size >= 5 && w[w.size - 1] == "serial") {
                        Log.d(TAG, "Found new driver " + driverName + " on " + w[w.size - 4])
                        mDrivers.add(Driver(driverName, w[w.size - 4]))
                    }
                }
                r.close()
            } catch (e: Exception) {
                Log.w(TAG, "Unable to read /proc/tty/drivers: " + e.message + ". fallback to common drivers")
                mDrivers.add(Driver("Serial", "/dev/ttyS"))
                mDrivers.add(Driver("USB", "/dev/ttyUSB"))
                mDrivers.add(Driver("ACM", "/dev/ttyACM"))
                mDrivers.add(Driver("AMA", "/dev/ttyAMA"))
                mDrivers.add(Driver("MXC", "/dev/ttymxc"))
                mDrivers.add(Driver("HS", "/dev/ttyHS"))
                mDrivers.add(Driver("SAC", "/dev/ttySAC"))
            }

        }
        return mDrivers
    }

    fun getAllDevices(): Array<String> {
        val devices = Vector<String>()
        // Parse each driver
        val itdriv: Iterator<Driver>
        try {
            itdriv = getDrivers().iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.getDevices().iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().name
                    val value = String.format("%s (%s)", device, driver.name)
                    devices.add(value)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return devices.toTypedArray()
    }

    fun getAllDevicesPath(): Array<String> {
        val devices = Vector<String>()
        // Parse each driver
        val itdriv: Iterator<Driver>
        try {
            itdriv = getDrivers().iterator()
            while (itdriv.hasNext()) {
                val driver = itdriv.next()
                val itdev: Iterator<File> = driver.getDevices().iterator()
                while (itdev.hasNext()) {
                    val device = itdev.next().absolutePath
                    devices.add(device)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return devices.toTypedArray()
    }

    companion object {
        private const val TAG = "SerialPort"
    }
}
