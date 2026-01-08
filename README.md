# Android Serial Port (Kotlin)

A modern, thread-safe, and reactive Android Serial Port library written in Kotlin. This library is a modernization of the popular [xmaihh/Android-Serialport](https://github.com/xmaihh/Android-Serialport) library, leveraging Kotlin Coroutines and Flow for asynchronous data handling.

- **Modern Stack**: 100% Kotlin, Coroutines, and Flow.
- **Reactive API**: Read data as a hot `Flow<ByteArray>`.
- **Packet Handling**: Built-in support for "sticky packets" with multiple parsing strategies:
  - `delimiter()` - Split by delimiter bytes (e.g., newline)
  - `fixedLength()` - Split into fixed-size chunks
  - `startEndMarker()` - Extract packets framed by start/end sequences
  - `lengthPrefixed()` - Parse packets with length headers
  - `customParser()` - Implement your own parsing logic
- **Device Discovery**: `SerialPortFinder` to list available serial ports on the device.
- **Root Support**: Helper to request root permissions for accessing serial ports.
- **Thread Safety**: I/O operations are offloaded to `Dispatchers.IO`.

## Installation

[![](https://jitpack.io/v/onatakduman/kserialport.svg)](https://jitpack.io/#onatakduman/kserialport)
[![](https://img.shields.io/github/license/onatakduman/kserialport.svg)](https://github.com/onatakduman/kserialport)

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
    implementation("com.github.onatakduman:kserialport:1.0.6")
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
    // 8N1 (Default)
    val serialPort = SerialPort("/dev/ttyS1", 9600)
    
    // Custom Configuration: 8E1 (8 Data bits, Even Parity, 1 Stop bit)
    // val serialPort = SerialPort(
    //     path = "/dev/ttyS1", 
    //     baudRate = 9600,
    //     dataBits = SerialPort.DATA_BITS_8,
    //     stopBits = SerialPort.STOP_BITS_1,
    //     parity = SerialPort.PARITY_EVEN
    // )

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
// Simple read - get raw bytes as they arrive
lifecycleScope.launch {
    connection.readFlow.collect { bytes ->
        Log.d("Serial", "Received: ${bytes.toHexString()}")
    }
}
```

### 4. Packet Parsing (Sticky Packet Handling)

Serial data often arrives fragmented or concatenated. Use these operators to extract complete packets:

#### Delimiter-Based

Split packets by a delimiter sequence (e.g., newline, CRLF):

```kotlin
connection.readFlow
    .delimiter(byteArrayOf(0x0A)) // Split by \n
    .collect { packet ->
        Log.d("Serial", "Line: ${String(packet)}")
    }
```

#### Fixed-Length Packets

When packets always have the same size:

```kotlin
connection.readFlow
    .fixedLength(32) // 32-byte packets
    .collect { packet ->
        // Process 32-byte packet
    }
```

#### Start/End Marker Frames

For protocols that frame data with markers (e.g., STX/ETX):

```kotlin
val STX = byteArrayOf(0x02)
val ETX = byteArrayOf(0x03)

connection.readFlow
    .startEndMarker(startMarker = STX, endMarker = ETX)
    .collect { packet ->
        // Packet data between STX and ETX
    }
```

#### Length-Prefixed Packets

Common in binary protocols where a header contains the payload length:

```kotlin
// Protocol: [1-byte length][payload]
connection.readFlow
    .lengthPrefixed(
        lengthFieldOffset = 0,    // Length at byte 0
        lengthFieldSize = 1,      // 1-byte length field
        bigEndian = true
    )
    .collect { packet ->
        // Complete packet including header
    }

// More complex: [2-byte header][2-byte length BE][payload]
connection.readFlow
    .lengthPrefixed(
        lengthFieldOffset = 2,    // Length field starts at byte 2
        lengthFieldSize = 2,      // 2-byte length (big-endian)
        headerSize = 4,           // Total header is 4 bytes
        bigEndian = true
    )
    .collect { packet -> }
```

#### Custom Parser

For complex protocols, implement your own logic:

```kotlin
connection.readFlow
    .customParser { buffer ->
        // Return null if no complete packet yet
        if (buffer.size < 4) return@customParser null
        
        // Parse your protocol...
        val packetEnd = findPacketEnd(buffer)
        if (packetEnd == -1) return@customParser null
        
        // Return the packet and remaining bytes
        PacketParser.ParseResult(
            packet = buffer.copyOfRange(0, packetEnd),
            remaining = buffer.copyOfRange(packetEnd, buffer.size)
        )
    }
    .collect { packet -> }
```

### 5. Write Data

```kotlin
lifecycleScope.launch {
    val data = "Hello World".toByteArray()
    connection.write(data)
}
```

### 6. Close Connection

```kotlin
connection.close()
```

## License

Apache License 2.0
