package com.onatakduman.kserialport

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

/**
 * Discovers USB serial devices using Android USB Host API. No root required.
 */
class UsbSerialDeviceFinder(private val context: Context) {

    private fun manager(): UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    /**
     * Returns all connected USB devices.
     */
    fun getAllDevices(): List<UsbDevice> {
        return manager().deviceList.values.toList()
    }

    /**
     * Returns device names with info (e.g., "USB Device (VID:1234 PID:5678)")
     */
    fun getAllDeviceNames(): Array<String> {
        return getAllDevices().map { device ->
            "${device.deviceName} (VID:${device.vendorId} PID:${device.productId})"
        }.toTypedArray()
    }

    /**
     * Returns device paths (device names in USB Host API context).
     */
    fun getAllDevicePaths(): Array<String> {
        return getAllDevices().map { it.deviceName }.toTypedArray()
    }

    /**
     * Find a UsbDevice by its device name/path.
     */
    fun findDevice(deviceName: String): UsbDevice? {
        return manager().deviceList.values.find { it.deviceName == deviceName }
    }

    /**
     * Check if USB Host API permission is granted for a device.
     */
    fun hasPermission(device: UsbDevice): Boolean {
        return manager().hasPermission(device)
    }

    /**
     * Get the UsbManager for permission requests.
     */
    fun getUsbManager(): UsbManager = manager()
}
