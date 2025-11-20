# Android Serial Port (Kotlin)

A modern, thread-safe, and reactive Android Serial Port library written in Kotlin. This library is a modernization of the popular `xmaihh/Android-Serialport` library, leveraging Kotlin Coroutines and Flow for asynchronous data handling.

## Features

- **Modern Stack**: 100% Kotlin, Coroutines, and Flow.
- **Reactive API**: Read data as a hot `Flow<ByteArray>`.
- **Packet Handling**: Built-in support for "sticky packets" (fragmentation) using `delimiter` and `fixedLength` operators.
- **Device Discovery**: `SerialPortFinder` to list available serial ports on the device.
- **Root Support**: Helper to request root permissions for accessing serial ports.
- **Thread Safety**: I/O operations are offloaded to `Dispatchers.IO`.

## Installation

[![](https://jitpack.io/v/onatakduman/kserialport.svg)](https://jitpack.io/#onatakduman/kserialport)
Add the Project Settings to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
   mavenCentral()
   maven { url = uri("https://jitpack.io") }
  }
 }
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.onatakduman:kserialport:1.0.2")
}
```

## Usage

### 1. List Available Devices

```kotlin
val finder = SerialPortFinder()
val devices = finder.getAllDevices() // Returns Array<String> e.g., ["ttyS0 (serial)", ...]
val paths = finder.getAllDevicesPath() // Returns Array<String> e.g., ["/dev/ttyS0", ...]
```

### 2. Open Serial Port

```kotlin
try {
    val serialPort = SerialPort("/dev/ttyS1", 9600)
    val connection = serialPort.open()
    
    // Use connection...
} catch (e: SecurityException) {
    // Handle permission error
} catch (e: IOException) {
    // Handle open error
}
```

### 3. Read Data (Reactive)

Use the `readFlow` to receive data. You can apply packet handling operators directly.

```kotlin
// Simple read
lifecycleScope.launch {
    connection.readFlow.collect { bytes ->
        Log.d("Serial", "Received: ${bytes.toHexString()}")
    }
}

// Handle Sticky Packets (Split by Newline)
lifecycleScope.launch {
    connection.readFlow
        .delimiter(byteArrayOf(0x0A)) // Split by \n
        .collect { packet ->
            Log.d("Serial", "Packet: ${String(packet)}")
        }
}
```

### 4. Write Data

```kotlin
lifecycleScope.launch {
    val data = "Hello World".toByteArray()
    connection.write(data)
}
```

### 5. Close Connection

```kotlin
connection.close()
```

## License

Apache License 2.0
