package com.onatakduman.kserialport

import android.util.Log
import java.io.File

object RootPermissionHelper {

    private const val TAG = "RootPermissionHelper"

    fun grantPermission(path: String): Boolean {
        val device = File(path)
        if (device.canRead() && device.canWrite()) {
            return true
        }

        // Validate path to prevent command injection
        if (!isValidDevicePath(path)) {
            Log.e(TAG, "Invalid device path: $path")
            return false
        }

        return try {
            // Use ProcessBuilder with separate arguments to prevent command injection
            val process = ProcessBuilder("su", "-c", "chmod 666 $path")
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            val success = exitCode == 0 && device.canRead() && device.canWrite()

            if (!success) {
                Log.w(TAG, "Failed to grant permission for $path (exit code: $exitCode)")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Error granting permission for $path", e)
            false
        }
    }

    private fun isValidDevicePath(path: String): Boolean {
        // Only allow paths under /dev/ with alphanumeric characters, underscores, and slashes
        val validPattern = Regex("^/dev/[a-zA-Z0-9_/]+$")
        return validPattern.matches(path) && !path.contains("..")
    }
}
