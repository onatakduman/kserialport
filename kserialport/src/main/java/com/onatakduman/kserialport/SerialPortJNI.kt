package com.onatakduman.kserialport

import android.util.Log
import java.io.FileDescriptor
import java.io.IOException

internal object SerialPortJNI {

    private const val TAG = "SerialPortJNI"
    private const val LIB_NAME = "kserialport"

    /**
     * Non-null when the native library failed to load. Stored instead of letting
     * the raw [UnsatisfiedLinkError] escape `<clinit>`: on devices with a broken
     * split-APK install (custom-ROM POS/industrial terminals, sideloaded base
     * APKs) that Error would crash the host app before any `catch (Exception)`
     * could run. [ensureLoaded] converts it into a catchable [IOException].
     */
    @Volatile
    private var loadError: Throwable? = null

    init {
        loadError = try {
            System.loadLibrary(LIB_NAME)
            null
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load lib$LIB_NAME.so", t)
            t
        }
    }

    /** True when lib{kserialport}.so is loaded and native calls are safe. */
    val isLoaded: Boolean
        get() = loadError == null

    /**
     * Throws a catchable [IOException] (never [UnsatisfiedLinkError]) when the
     * native library is unavailable. Retries the load once first, in case the
     * host app loaded the library through its own fallback (e.g. ReLinker)
     * after this object was initialized.
     */
    @Synchronized
    fun ensureLoaded() {
        if (loadError == null) return
        loadError = try {
            System.loadLibrary(LIB_NAME)
            null
        } catch (t: Throwable) {
            t
        }
        loadError?.let {
            throw IOException(
                "Native library lib$LIB_NAME.so could not be loaded — usually a broken " +
                    "(split-APK) installation; reinstalling the app from the store fixes it.",
                it
            )
        }
    }

    external fun open(path: String, flags: Int): FileDescriptor?
    external fun close(fd: FileDescriptor)
    external fun configure(
            fd: FileDescriptor,
            baudRate: Int,
            dataBits: Int,
            stopBits: Int,
            parity: Int
    ): Boolean
}
