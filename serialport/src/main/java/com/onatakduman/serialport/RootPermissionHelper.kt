package com.onatakduman.serialport

import java.io.File
import java.io.IOException

object RootPermissionHelper {

    fun grantPermission(path: String): Boolean {
        val device = File(path)
        if (device.canRead() && device.canWrite()) {
            return true
        }

        return try {
            val su = Runtime.getRuntime().exec("su")
            val cmd = "chmod 666 $path\n" + "exit\n"
            su.outputStream.write(cmd.toByteArray())
            su.outputStream.flush()
            
            val exitCode = su.waitFor()
            exitCode == 0 && device.canRead() && device.canWrite()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
