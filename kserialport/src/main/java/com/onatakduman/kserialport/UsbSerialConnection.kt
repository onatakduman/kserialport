package com.onatakduman.kserialport

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.IOException

/**
 * USB Host API based serial connection. No root required.
 * Provides the same readFlow/write/close API as [SerialPortConnection].
 */
class UsbSerialConnection(
    private val device: UsbDevice,
    private val connection: UsbDeviceConnection,
    private val usbInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint,
    baudRate: Int = 115200,
    dataBits: Int = 8,
    stopBits: Int = 1,
    parity: Int = 0
) : Closeable {

    private val TAG = "UsbSerialConnection"

    @Volatile
    private var isClosed = false

    init {
        connection.claimInterface(usbInterface, true)
        configureCdcAcm(baudRate, dataBits, stopBits, parity)
    }

    var logFormat: DataLogger.Format = DataLogger.Format.HEX

    /**
     * Hot flow emitting data from USB device. Same API as [SerialPortConnection.readFlow].
     */
    val readFlow: Flow<ByteArray> = flow {
        val buffer = ByteArray(inEndpoint.maxPacketSize.coerceAtLeast(64))
        try {
            while (!isClosed) {
                val bytesRead = connection.bulkTransfer(inEndpoint, buffer, buffer.size, 100)
                if (bytesRead > 0) {
                    val data = buffer.copyOf(bytesRead)
                    DataLogger.log("UsbSerial (RX)", data, logFormat)
                    emit(data)
                }
            }
        } catch (e: Exception) {
            if (!isClosed) {
                Log.e(TAG, "Read error", e)
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Write data to USB serial device. Same API as [SerialPortConnection.write].
     */
    suspend fun write(data: ByteArray) = withContext(Dispatchers.IO) {
        if (isClosed) throw IOException("Connection is closed")
        DataLogger.log("UsbSerial (TX)", data, logFormat)
        val sent = connection.bulkTransfer(outEndpoint, data, data.size, 1000)
        if (sent < 0) {
            throw IOException("USB write failed")
        }
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        try {
            connection.releaseInterface(usbInterface)
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing interface", e)
        }
        try {
            connection.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing connection", e)
        }
    }

    /**
     * Configure CDC ACM serial parameters via USB control transfer.
     * SET_LINE_CODING request (0x20) on CDC ACM interface.
     */
    private fun configureCdcAcm(baudRate: Int, dataBits: Int, stopBits: Int, parity: Int) {
        // CDC SET_LINE_CODING: baud(4 bytes LE), stop(1), parity(1), dataBits(1)
        val lineEncoding = ByteArray(7)
        lineEncoding[0] = (baudRate and 0xFF).toByte()
        lineEncoding[1] = (baudRate shr 8 and 0xFF).toByte()
        lineEncoding[2] = (baudRate shr 16 and 0xFF).toByte()
        lineEncoding[3] = (baudRate shr 24 and 0xFF).toByte()
        lineEncoding[4] = when (stopBits) {
            2 -> 2.toByte()
            else -> 0.toByte() // 1 stop bit
        }
        lineEncoding[5] = parity.toByte()
        lineEncoding[6] = dataBits.toByte()

        val result = connection.controlTransfer(
            0x21, // USB_DIR_OUT | USB_TYPE_CLASS | USB_RECIP_INTERFACE
            0x20, // SET_LINE_CODING
            0,
            usbInterface.id,
            lineEncoding,
            lineEncoding.size,
            1000
        )
        if (result < 0) {
            Log.w(TAG, "SET_LINE_CODING failed (non-CDC device?), continuing anyway")
        }

        // SET_CONTROL_LINE_STATE: DTR=1, RTS=1
        connection.controlTransfer(
            0x21,
            0x22, // SET_CONTROL_LINE_STATE
            0x03, // DTR | RTS
            usbInterface.id,
            null,
            0,
            1000
        )
    }

    companion object {
        /**
         * Open a USB serial connection from a UsbDevice.
         * Finds bulk IN/OUT endpoints and creates the connection.
         */
        fun open(
            device: UsbDevice,
            connection: UsbDeviceConnection,
            baudRate: Int = 115200,
            dataBits: Int = 8,
            stopBits: Int = 1,
            parity: Int = 0
        ): UsbSerialConnection {
            // Find an interface with bulk endpoints (try data interface first, then any)
            var targetInterface: UsbInterface? = null
            var inEp: UsbEndpoint? = null
            var outEp: UsbEndpoint? = null

            for (i in 0 until device.interfaceCount) {
                val intf = device.getInterface(i)
                var foundIn: UsbEndpoint? = null
                var foundOut: UsbEndpoint? = null

                for (j in 0 until intf.endpointCount) {
                    val ep = intf.getEndpoint(j)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.direction == UsbConstants.USB_DIR_IN) {
                            foundIn = ep
                        } else {
                            foundOut = ep
                        }
                    }
                }

                if (foundIn != null && foundOut != null) {
                    targetInterface = intf
                    inEp = foundIn
                    outEp = foundOut
                    break
                }
            }

            if (targetInterface == null || inEp == null || outEp == null) {
                throw IOException("No USB serial interface found on device: ${device.deviceName}")
            }

            return UsbSerialConnection(
                device = device,
                connection = connection,
                usbInterface = targetInterface,
                inEndpoint = inEp,
                outEndpoint = outEp,
                baudRate = baudRate,
                dataBits = dataBits,
                stopBits = stopBits,
                parity = parity
            )
        }
    }
}
