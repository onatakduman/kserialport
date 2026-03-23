package com.onatakduman.kserialport.app.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onatakduman.kserialport.SerialPort
import com.onatakduman.kserialport.SerialPortConnection
import com.onatakduman.kserialport.SerialPortFinder
import com.onatakduman.kserialport.UsbSerialConnection
import com.onatakduman.kserialport.UsbSerialDeviceFinder

import com.onatakduman.kserialport.app.billing.BillingManager
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.data.model.DeviceInfo
import com.onatakduman.kserialport.app.data.model.LogEntry
import com.onatakduman.kserialport.app.data.model.SerialConfig
import com.onatakduman.kserialport.app.data.preferences.UserPreferencesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * Wraps both connection types (root JNI and USB Host API) under a single interface.
 */
private class ActiveConnection(
    private val rootConnection: SerialPortConnection? = null,
    private val usbConnection: UsbSerialConnection? = null
) : Closeable {
    val readFlow: Flow<ByteArray>
        get() = rootConnection?.readFlow ?: usbConnection?.readFlow
            ?: throw IllegalStateException("No active connection")

    suspend fun write(data: ByteArray) {
        rootConnection?.write(data) ?: usbConnection?.write(data)
            ?: throw IllegalStateException("No active connection")
    }

    override fun close() {
        rootConnection?.close()
        usbConnection?.close()
    }
}

class SerialViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesRepo = UserPreferencesRepo(application)
    private val usbFinder = UsbSerialDeviceFinder(application)
    val billingManager = BillingManager(application)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _serialConfig = MutableStateFlow(SerialConfig())
    val serialConfig: StateFlow<SerialConfig> = _serialConfig.asStateFlow()

    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices: StateFlow<List<DeviceInfo>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _incomingData = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 64)
    val incomingData: SharedFlow<ByteArray> = _incomingData.asSharedFlow()

    // Terminal state — persisted across tab switches
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _rxBytes = MutableStateFlow(0L)
    val rxBytes: StateFlow<Long> = _rxBytes.asStateFlow()

    private val _txBytes = MutableStateFlow(0L)
    val txBytes: StateFlow<Long> = _txBytes.asStateFlow()

    private val _errors = MutableStateFlow(0)
    val errors: StateFlow<Int> = _errors.asStateFlow()

    fun addLog(entry: LogEntry) {
        _logs.value = _logs.value + entry
    }

    fun addRxBytes(count: Int) { _rxBytes.value += count }
    fun addTxBytes(count: Int) { _txBytes.value += count }
    fun incrementErrors() { _errors.value++ }

    fun clearTerminal() {
        _logs.value = emptyList()
        _rxBytes.value = 0
        _txBytes.value = 0
        _errors.value = 0
    }

    private val _events = MutableSharedFlow<SerialEvent>(replay = 0, extraBufferCapacity = 16)
    val events: SharedFlow<SerialEvent> = _events.asSharedFlow()

    val recentConnections = preferencesRepo.recentConnections
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isProUser: StateFlow<Boolean> = combine(
        preferencesRepo.proPurchased,
        billingManager.isPro
    ) { dataPro, billingPro -> dataPro || billingPro }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var activeConnection: ActiveConnection? = null
    private var readJob: Job? = null
    private var periodicSendJob: Job? = null

    // Track USB devices for permission flow
    private var pendingUsbDevice: UsbDevice? = null

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted && device != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        connectUsb(device)
                    }
                } else {
                    _connectionState.value = ConnectionState.Error("USB permission denied")
                    viewModelScope.launch {
                        _events.emit(SerialEvent.Error("USB permission denied"))
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            preferencesRepo.serialConfig.collect { config ->
                _serialConfig.value = config
            }
        }
        // Start billing connection
        billingManager.startConnection()
        // Sync billing state to DataStore (both activate and deactivate)
        viewModelScope.launch {
            billingManager.isPro.collect { isPro ->
                preferencesRepo.saveProPurchased(isPro)
            }
        }
        // Register USB permission receiver
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(usbPermissionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(usbPermissionReceiver, filter)
        }
    }

    fun updateConfig(config: SerialConfig) {
        _serialConfig.value = config
        viewModelScope.launch {
            preferencesRepo.saveSerialConfig(config)
        }
    }

    fun updatePath(path: String) {
        updateConfig(_serialConfig.value.copy(path = path))
    }

    /**
     * Scan both /dev filesystem (root) AND USB Host API devices.
     */
    fun scanDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            val allDevices = mutableListOf<DeviceInfo>()

            // 1. Scan USB Host API devices (no root needed)
            try {
                val usbDevices = usbFinder.getAllDevices()
                usbDevices.forEach { device ->
                    allDevices.add(
                        DeviceInfo(
                            name = device.productName ?: "USB Device",
                            path = device.deviceName,
                            driver = "USB (VID:${device.vendorId} PID:${device.productId})"
                        )
                    )
                }
            } catch (e: Exception) {
                // USB scanning failed, continue with root scanning
            }

            // 2. Scan /dev filesystem (may require root)
            try {
                val finder = SerialPortFinder()
                val deviceNames = finder.getAllDevices()
                val devicePaths = finder.getAllDevicesPath()
                devicePaths.forEachIndexed { index, path ->
                    // Skip if already found via USB
                    if (allDevices.none { it.path == path }) {
                        val name = path.substringAfterLast("/")
                        val driver = if (index < deviceNames.size) {
                            deviceNames[index].substringAfter("(").substringBefore(")")
                        } else ""
                        allDevices.add(DeviceInfo(name = name, path = path, driver = driver))
                    }
                }
            } catch (_: Exception) {
                // Root scanning failed, USB devices still available
            }

            _devices.value = allDevices
            _events.emit(SerialEvent.SystemMessage("Found ${allDevices.size} device(s)"))
            _isScanning.value = false
        }
    }

    /**
     * Connect — tries USB Host API first, falls back to root JNI.
     */
    fun connect() {
        val config = _serialConfig.value
        viewModelScope.launch(Dispatchers.IO) {
            _connectionState.value = ConnectionState.Connecting

            // Check if this is a USB Host API device
            val usbDevice = usbFinder.findDevice(config.path)
            if (usbDevice != null) {
                // USB device found — check permission
                if (usbFinder.hasPermission(usbDevice)) {
                    connectUsb(usbDevice)
                } else {
                    // Request permission — connection will happen in receiver callback
                    pendingUsbDevice = usbDevice
                    val permissionIntent = PendingIntent.getBroadcast(
                        getApplication(),
                        0,
                        Intent(ACTION_USB_PERMISSION).apply {
                            `package` = getApplication<Application>().packageName
                        },
                        PendingIntent.FLAG_MUTABLE
                    )
                    usbFinder.getUsbManager().requestPermission(usbDevice, permissionIntent)
                    _events.emit(SerialEvent.SystemMessage("Requesting USB permission..."))
                }
            } else {
                // Not a USB device — try root JNI path
                connectRoot(config)
            }
        }
    }

    private suspend fun connectUsb(usbDevice: UsbDevice) {
        val config = _serialConfig.value
        try {
            val usbManager = usbFinder.getUsbManager()
            val deviceConnection = usbManager.openDevice(usbDevice)
                ?: throw Exception("Failed to open USB device")

            val usbConn = UsbSerialConnection.open(
                device = usbDevice,
                connection = deviceConnection,
                baudRate = config.baudRate,
                dataBits = config.dataBits,
                stopBits = config.stopBits,
                parity = config.parity
            )

            activeConnection = ActiveConnection(usbConnection = usbConn)
            _connectionState.value = ConnectionState.Connected(config.path, config.baudRate)
            _events.emit(SerialEvent.SystemMessage("Connected via USB: ${usbDevice.productName ?: config.path} @ ${config.baudRate}"))

            withContext(Dispatchers.Main) {
                viewModelScope.launch {
                    preferencesRepo.addRecentConnection("${config.path}@${config.baudRate}")
                }
            }

            startReadJob()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("USB connection failed: ${e.message}")
            _events.emit(SerialEvent.Error("USB connection failed: ${e.message}"))
        }
    }

    private suspend fun connectRoot(config: SerialConfig) {
        try {
            val port = SerialPort(
                path = config.path,
                baudRate = config.baudRate,
                dataBits = config.dataBits,
                stopBits = config.stopBits,
                parity = config.parity
            )
            val conn = port.open()
            activeConnection = ActiveConnection(rootConnection = conn)
            _connectionState.value = ConnectionState.Connected(config.path, config.baudRate)
            _events.emit(SerialEvent.SystemMessage("Connected to ${config.path} @ ${config.baudRate}"))

            withContext(Dispatchers.Main) {
                viewModelScope.launch {
                    preferencesRepo.addRecentConnection("${config.path}@${config.baudRate}")
                }
            }

            startReadJob()
        } catch (e: Exception) {
            val userMessage = when {
                e.message?.contains("No such file or directory") == true && e.message?.contains("su") == true ->
                    "Root access required. Device is not rooted. Use USB connection instead."
                e.message?.contains("Permission denied") == true ->
                    "Permission denied for ${config.path}"
                e.message?.contains("No such file") == true ->
                    "Device not found: ${config.path}"
                else -> "Connection failed: ${e.message}"
            }
            _connectionState.value = ConnectionState.Error(userMessage)
            _events.emit(SerialEvent.Error(userMessage))
        }
    }

    private fun startReadJob() {
        readJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                activeConnection?.readFlow?.collect { bytes ->
                    _incomingData.emit(bytes)
                }
            } catch (_: Exception) {
                _events.emit(SerialEvent.SystemMessage("Connection closed"))
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
        try {
            activeConnection?.close()
            activeConnection = null
            _connectionState.value = ConnectionState.Disconnected
            viewModelScope.launch {
                _events.emit(SerialEvent.SystemMessage("Disconnected"))
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.emit(SerialEvent.Error("Disconnect error: ${e.message}"))
            }
        }
    }

    fun write(data: ByteArray) {
        viewModelScope.launch {
            try {
                activeConnection?.write(data)
                _events.emit(SerialEvent.DataSent(data))
            } catch (e: Exception) {
                _events.emit(SerialEvent.Error("Send failed: ${e.message}"))
            }
        }
    }

    val isConnected: Boolean
        get() = _connectionState.value is ConnectionState.Connected

    fun startPeriodicSend(data: ByteArray, intervalMs: Long) {
        stopPeriodicSend()
        periodicSendJob = viewModelScope.launch {
            while (true) {
                if (isConnected) {
                    write(data)
                }
                delay(intervalMs)
            }
        }
    }

    fun stopPeriodicSend() {
        periodicSendJob?.cancel()
        periodicSendJob = null
    }

    val isPeriodicSendActive: Boolean
        get() = periodicSendJob?.isActive == true

    override fun onCleared() {
        stopPeriodicSend()
        disconnect()
        billingManager.endConnection()
        try {
            getApplication<Application>().unregisterReceiver(usbPermissionReceiver)
        } catch (_: Exception) { }
        super.onCleared()
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.onatakduman.kserialport.USB_PERMISSION"
    }
}

sealed interface SerialEvent {
    data class SystemMessage(val message: String) : SerialEvent
    data class Error(val message: String) : SerialEvent
    data class DataSent(val data: ByteArray) : SerialEvent
}
