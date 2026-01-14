# N2505 Sprayer ECU Communication Protocol

## 1. Overview

This document defines the serial communication data exchange protocol between the Sprayer Electronic Control Unit (ECU) and the Control Display (Tablet).

### Communication Specifications

| Parameter | Value |
|-----------|-------|
| Interface | RS232 |
| Baud Rate | 115200 |
| Data Bits | 8 |
| Parity | None |
| Stop Bits | 1 |
| Communication Period | 500ms (can be set to 1000ms if tablet processing power is insufficient) |
| Packet Size | 100 bytes (both TX and RX) |

### Communication Flow

The ECU initiates communication by sending a 100-byte packet. After receiving this message, the Tablet responds with its own 100-byte packet.

> **Note:** There is no master-slave architecture. The ECU determines the communication timing. The tablet only sends messages after receiving a message from the ECU.

### Checksum Calculation

The checksum is calculated using XOR over the data bytes (excluding preamble and checksum bytes):

```c
#define STATUS_MESSAGE_LENGTH 100
#define PREAMBLE_LENGTH 4
#define CHECKSUM_LENGTH 2
#define DATA_LENGTH (STATUS_MESSAGE_LENGTH - CHECKSUM_LENGTH)

void calculateChecksum() {
    byte checksum = 0;
    for (int i = 0; i < STATUS_MESSAGE_LENGTH; i++) {
        byte y = outgoingData[i];
        if (i >= PREAMBLE_LENGTH && i < DATA_LENGTH) {
            checksum ^= y;
        }
    }
    outgoingData[STATUS_MESSAGE_LENGTH - 2] = 42;  // '*' CRC key
    outgoingData[STATUS_MESSAGE_LENGTH - 1] = checksum;
}
```

---

## 2. Tablet → ECU Message (Command Packet)

This section describes the 100-byte message sent from the Tablet to the ECU.

### Configuration Categories

| Category | Description | UI Location |
|----------|-------------|-------------|
| **Main Screen** | Values that change during operation | Main cockpit screen |
| **Settings** | Values configured before field work | Settings screen |
| **Factory Settings** | Values set during initial installation | Factory/Advanced settings |

### Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 0 | 1 | Byte | Preamble[0] | Start marker | `'$'` (36 decimal) | - |
| 1 | 1 | Byte | Preamble[1] | Start marker | `'N'` (78 decimal) | - |
| 2 | 1 | Byte | Preamble[2] | Start marker | `'I'` (73 decimal) | - |
| 3 | 1 | Byte | Preamble[3] | Start marker | `'T'` (84 decimal) | - |
| 4 | 1 | Byte | Tank Capacity | Main tank volume | Range: 600-10000L, Resolution: 50L. **Send value ÷ 50** (e.g., 3000L → send 60) | Factory Settings |
| 5 | 1 | Byte | Reserved | - | - | - |
| 6 | 1 | Byte | Boom Width (meters) | Working width in whole meters | Range: 8-40 meters | Factory Settings |
| 7 | 1 | Byte | Boom Width (half meter) | Adds 0.5m to width | `0`: No addition, `1`: Add 0.5m (e.g., byte[6]=15, byte[7]=1 → 15.5m) | Factory Settings |
| 8-9 | 2 | Short | Target Application Rate | Liquid amount per decare (1000 m²) | Range: 0-200L, Resolution: 1L. **Send value × 10** (e.g., 35L → send 350) | Main Screen |
| 10-11 | 2 | -- | Reserved | - | - | - |
| 12 | 1 | Byte | Job Status | Current operation state | `0`: N/A, `1`: Start Job, `2`: Pause, `3`: Finish, `4`: Resume Job | Main Screen |
| 13-15 | 3 | -- | Reserved | - | - | - |

### Subsystems Bitfield (Address 16-17)

| Bit | Subsystem | Values |
|:---:|-----------|--------|
| 0 | GPS | `0`: Not present, `1`: Present |
| 1 | IMU | `0`: Not present, `1`: Present |
| 2 | Height Sensor | `0`: Not present, `1`: Present |
| 3 | Pressure Sensor | `0`: Not present, `1`: Present |
| 4 | Flow Sensor | `0`: Not present, `1`: Present |
| 5 | Main Tank Level Sensor | `0`: Not present, `1`: Present |
| 6 | Backup Tank Level Sensor | `0`: Not present, `1`: Present |
| 7 | Backup Tank | `0`: Not present, `1`: Present |
| 8 | Shaft | Reserved |
| 9-15 | Reserved | - |

> **Note:** This field is for future use. Current system configuration is fixed.

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 18 | 1 | Byte | Section Count | Number of boom sections | Range: 3-15 | Factory Settings |
| 19 | 1 | Byte | Operation Mode | Control mode selection | `0`: Automatic (software controls valves & flow), `1`: Manual (user controls valves), `2`: Semi-Auto (user selects sections, auto dosing) | Main Screen |
| 20 | 1 | Byte | Reserved | - | - | - |
| 21 | 1 | Byte | Flow Control Valve | Flow valve state | `0`: Close, `1`: Open, `2`: Auto Control, `3`: Idle | Factory (Testing) |

### Section Valve Control (Address 22)

| Bit | Valve | State |
|:---:|-------|-------|
| 0 | Main Line (Master Valve) | `0`: Closed, `1`: Open |
| 1 | Section Valve 1 | `0`: Closed, `1`: Open |
| 2 | Section Valve 2 | `0`: Closed, `1`: Open |
| 3 | Section Valve 3 | `0`: Closed, `1`: Open |
| 4 | Section Valve 4 | `0`: Closed, `1`: Open |
| 5 | Section Valve 5 | `0`: Closed, `1`: Open |
| 6 | Section Valve 6 | `0`: Closed, `1`: Open |
| 7 | Section Valve 7 | `0`: Closed, `1`: Open |

> **UI Note:** When Operation Mode is Manual or Semi-Auto, these can be controlled via the main screen visualization.

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 23 | 1 | Byte | Simulation Mode | Enable/disable simulation | `0`: Off, `1`: On | Factory Settings |
| 24 | 1 | Byte | Mixer Status | Agitator on/off | `0`: Off, `1`: On | Settings (optional feature) |

### Hydraulic Up Control (Address 25) - Future Feature

| Bit | Hydraulic | State |
|:---:|-----------|-------|
| 0 | Hyd.A | `0`: Idle, `1`: Active |
| 1 | Hyd.B | `0`: Idle, `1`: Active |
| 2 | Hyd.1 | `0`: Idle, `1`: Active |
| 3 | Hyd.2 | `0`: Idle, `1`: Active |
| 4 | Hyd.3 | `0`: Idle, `1`: Active |
| 5 | Hyd.4 | `0`: Idle, `1`: Active |
| 6 | Hyd.5 | `0`: Idle, `1`: Active |
| 7 | Hyd.6 | `0`: Idle, `1`: Active |

### Hydraulic Down Control (Address 26) - Future Feature

Same bit structure as Hydraulic Up Control.

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 27-29 | 3 | -- | Reserved | - | - | - |
| 30-33 | 4 | Float | Calibration Correction Factor | Corrects flow measurement errors | Calculated as: Expected Flow ÷ Measured Flow | Factory Settings |
| 34 | 1 | Byte | Pressure Measurement Method | How pressure is measured | `0`: No measurement, `1`: Manual (via nozzle), `2`: Sensor | Factory (Default: 1) |
| 35 | 1 | Byte | Tank Level Measurement Method | How tank level is measured | `0`: No measurement, `1`: Manual input, `2`: Sensor | Factory (Default: 1) |
| 36 | 1 | Byte | Speed Measurement Method | How speed is measured | `0`: GPS, `1`: Wheel, `2`: GPS & Wheel | Factory (Default: 0) |

### Nozzle Type (Address 37)

| Value | Nozzle Type | Value | Nozzle Type |
|:-----:|-------------|:-----:|-------------|
| 1 | ISO Orange | 10 | ISO Light Blue |
| 2 | ISO Green | 11 | ISO Light Green |
| 3 | ISO Yellow | 12 | ISO Light Black |
| 4 | ISO Lilac | 13 | User Type A |
| 5 | ISO Blue | 14 | User Type B |
| 6 | ISO Red | 15 | User Type C |
| 7 | ISO Brown | 16 | User Type D |
| 8 | ISO Gray | 17 | User Type E |
| 9 | ISO White | | |

> **UI Location:** Settings (configured before field work)

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 38 | 1 | Byte | Wheel Magnet Count | Number of magnets on wheel | Range: 1-12 | Not used |
| 39 | 1 | Byte | Unused | - | - | - |
| 40-41 | 2 | UShort | Wheel Circumference | Wheel size in cm | Range: 100-400 cm | Not used |
| 42-43 | 2 | UShort | Tank Input Amount | Amount filled into tank | Range: 0 to tank capacity, Resolution: 50L | Main Screen (before spraying) |

### Tank Information Flags (Address 44)

| Bit | Description |
|:---:|-------------|
| 0 | Tank level received flag - Set to 1 after byte[42] is entered. ECU responds with byte[88] bit 0 = 1, then reset to 0 |
| 1 | Reset tank to zero flag - Set to 1 when tank water amount is set to zero |

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 45 | 1 | Byte | Simulation Speed | Speed for simulation mode | Range: 0-30 km/h, Resolution: 0.5 km/h. **Send value × 10** | Factory Settings |
| 46 | 1 | Byte | Minimum Speed | Minimum operating speed | Range: 0-5 km/h, Resolution: 0.5 km/h. **Send value × 10** | Settings |
| 47 | 1 | Byte | Language | UI language | `0`: Turkish, `1`: English, `2`: Greek | Optional |
| 48 | 1 | Byte | Unit System | Measurement units | `0`: Metric, `1`: Imperial | Optional |

### Settings Flags (Address 49)

| Bit | Description |
|:---:|-------------|
| 0 | Section nozzle count configured flag - Set to 1 after byte[52] is configured |
| 1 | Measurement methods configured flag - Can always be set to 1 |

### Reserved (Address 50-51)

### Section Nozzle Count (Address 52-55)

4-byte packed field containing nozzle count per section (max 15 nozzles per section):

| Bits | Section |
|:----:|---------|
| 3:0 | Section 1 nozzle count |
| 7:4 | Section 2 nozzle count |
| 11:8 | Section 3 nozzle count |
| 15:12 | Section 4 nozzle count |
| 19:16 | Section 5 nozzle count |
| 23:20 | Section 6 nozzle count |
| 27:24 | Section 7 nozzle count |

### Reserved (Address 56-97)

### Checksum (Address 98-99)

| Address | Size | Type | Field | Description |
|:-------:|:----:|:----:|-------|-------------|
| 98 | 1 | Byte | CRC Key | Always `'*'` (42 decimal) |
| 99 | 1 | Byte | Checksum | XOR checksum of data bytes |

---

## 3. ECU → Tablet Message (Status Packet)

This section describes the 100-byte message sent from the ECU to the Tablet.

### Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 0 | 1 | Byte | Preamble[0] | Start marker | `'$'` (36 decimal) | - |
| 1 | 1 | Byte | Preamble[1] | Start marker | `'N'` (78 decimal) | - |
| 2 | 1 | Byte | Preamble[2] | Start marker | `'I'` (73 decimal) | - |
| 3 | 1 | Byte | Preamble[3] | Start marker | `'M'` (77 decimal) | - |
| 4-11 | 8 | Double | Latitude | GPS latitude for map display | Decimal degrees | Debug Screen |
| 12-19 | 8 | Double | Longitude | GPS longitude for map display | Decimal degrees | Debug Screen |
| 20-23 | 4 | Float | Altitude | GPS altitude for map display | Meters | Debug Screen |
| 24-27 | 4 | Float | Speed | Current vehicle speed | km/h (display with 1 decimal) | Main Screen |
| 28-35 | 8 | ULong | GPS Timestamp | GPS time information | Unix timestamp | Debug Screen |
| 36 | 1 | Byte | Tank Capacity | Main tank volume | **Value × 50 = Liters** (Range: 0-200 → 0-10000L) | Main Screen (tank display) |
| 37 | 1 | Byte | Reserved | - | - | - |
| 38 | 1 | Byte | Tank Level | Current tank fill percentage | Range: 0-100%, Resolution: 5% | Main Screen |
| 39 | 1 | Byte | Reserved | - | - | - |
| 40 | 1 | Byte | Boom Width | Working width in meters | Range: 8-40m. Bit 7 = 1 means add 0.5m | Main Screen |
| 41 | 1 | Byte | Section Count | Number of boom sections | Determines sprayer visualization | Main Screen |
| 42-43 | 2 | Short | Target Application Rate | Liquid per decare | **Value ÷ 10 = L/decare** | Main Screen (if flow diagram shown) |

### Subsystems Status (Address 44-45)

Same bitfield structure as command packet (Section 2). Used for debug display.

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 46 | 1 | Byte | Operation Mode | Echo of command byte[19] | `0`: Auto, `1`: Manual, `2`: Semi-Auto | Main Screen |
| 47 | 1 | Byte | Reserved | - | - | - |
| 48 | 1 | Byte | Flow Valve Status | Current valve state | `0`: Closed, `1`: Open, `2`: Auto Control, `3`: Idle | Feedback |

### Section Valve Status (Address 49)

Echo of commanded valve states:

| Bit | Valve | State |
|:---:|-------|-------|
| 0 | Main Line | `0`: Closed, `1`: Open |
| 1-7 | Section Valves 1-7 | `0`: Closed, `1`: Open |

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 50-53 | 4 | -- | Reserved | - | - | - |
| 54-57 | 4 | -- | Reserved | - | - | - |

### Section Nozzle Count Echo (Address 58-61)

Same packed structure as command packet byte[52-55].

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 62 | 1 | Byte | Reserved | - | - | - |

### Timeout Flags (Address 63)

| Bit | Sensor | State |
|:---:|--------|-------|
| 0 | GPS | `0`: Normal, `1`: Timeout |
| 1-7 | Reserved | - |

> **UI Note:** When GPS timeout occurs, display GPS communication error indicator.

### GPS Status (Address 64)

| Value | Status | Icon Color |
|:-----:|--------|------------|
| 0 | Initializing | Gray |
| 1 | Normal | Green |
| 2 | No Position Fix | Yellow |
| 3 | Fault | Red |
| 4 | Not Available | Gray |
| 5 | Position Acquired | Green |

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 65 | 1 | Byte | Pressure Value | System pressure | Range: 0-255 | Main Screen (if pressure sensor present) |
| 66-69 | 4 | Float | Application Rate (Actual) | Actual L/decare applied | L/decare (display as integer) | Main Screen |
| 70 | 1 | Byte | Job Status | Current job state | `0`: None, `1`: Started, `2`: Paused, `3`: Finished | Main Screen |
| 71 | 1 | Byte | Simulation Status | Echo of simulation mode | - | - |
| 72-75 | 4 | Float | Flow Rate | L/minute being applied | L/min | Main Screen (info display) |
| 76-77 | 2 | -- | Reserved | - | - | - |
| 78 | 1 | Byte | Mixer Status | Agitator state | `0`: Off, `1`: On | Main Screen |
| 79 | 1 | Byte | Pressure Meas. Method | Echo | Always 1 | Factory Settings |
| 80 | 1 | Byte | Tank Level Meas. Method | Echo | Always 1 | Factory Settings |
| 81 | 1 | Byte | Speed Meas. Method | Echo | Always 0 | Factory Settings |
| 82 | 1 | Byte | Nozzle Type | Echo of selected nozzle | See nozzle table | Settings |
| 83 | 1 | Byte | Wheel Magnet Count | Echo | Not used | - |
| 84-85 | 2 | UShort | Wheel Circumference | Echo | Not used | - |
| 86-87 | 2 | UShort | Remaining Tank Level | Liquid remaining in tank | Liters | Main Screen |

### Tank Information Echo (Address 88)

| Bit | Description |
|:---:|-------------|
| 0 | Tank level acknowledged - Set to 1 by ECU when byte[44] bit 0 received |
| 1 | Tank reset acknowledged |

### Continued Packet Structure

| Address | Size | Type | Field | Description | Values | UI Location |
|:-------:|:----:|:----:|-------|-------------|--------|-------------|
| 89 | 1 | Byte | Minimum Speed | Echo | **Value ÷ 10 = km/h** | Settings |
| 90 | 1 | Byte | Simulation Speed | Echo | **Value ÷ 10 = km/h** | Factory Settings |
| 91-95 | 5 | -- | Reserved | - | - | - |
| 96 | 1 | Byte | Firmware Minor Version | ECU software minor version | - | Debug/Info |
| 97 | 1 | Byte | Firmware Major Version | ECU software major version | - | Debug/Info |
| 98 | 1 | Byte | CRC Key | Always `'*'` (42 decimal) | - | - |
| 99 | 1 | Byte | Checksum | XOR checksum | - | - |

---

## 4. Hardware Pin Assignments

### 4.1 Pressure Sensor

#### 4.1.1 Internal Pressure Sensor Connections

*Details to be added*

### 4.2 Tank Level Sensor

*Details to be added*

---

## Appendix A: Quick Reference

### Preambles

| Direction | Preamble | ASCII |
|-----------|----------|-------|
| Tablet → ECU | `$NIT` | 36, 78, 73, 84 |
| ECU → Tablet | `$NIM` | 36, 78, 73, 77 |

### Value Encoding Summary

| Field | Encoding | Example |
|-------|----------|---------|
| Tank Capacity | Value ÷ 50 | 3000L → 60 |
| Application Rate | Value × 10 | 35 L/da → 350 |
| Speed Values | Value × 10 | 15 km/h → 150 |
| Tank Input | Value (direct) | 1500L → 1500 |

### GPS Status Icons

| Status | Value | Icon Color |
|--------|:-----:|------------|
| Initializing | 0 | Gray |
| Normal | 1 | Green |
| No Fix | 2 | Yellow |
| Fault | 3 | Red |
| Not Available | 4 | Gray |
| Fix Acquired | 5 | Green |

### Nozzle Types (ISO Color Codes)

| Color | Value | Flow Rate Class |
|-------|:-----:|-----------------|
| Orange | 1 | 0.4 L/min @ 3 bar |
| Green | 2 | 0.6 L/min @ 3 bar |
| Yellow | 3 | 0.8 L/min @ 3 bar |
| Lilac | 4 | 1.0 L/min @ 3 bar |
| Blue | 5 | 1.2 L/min @ 3 bar |
| Red | 6 | 1.6 L/min @ 3 bar |
| Brown | 7 | 2.0 L/min @ 3 bar |
| Gray | 8 | 2.4 L/min @ 3 bar |
| White | 9 | 3.2 L/min @ 3 bar |

---

## Appendix B: Operation Modes

### Automatic Mode (0)
- Software automatically controls all section valves based on GPS position
- Flow meter regulates liquid amount to maintain target application rate
- Recommended for normal field operations

### Manual Mode (1)
- User manually controls all valve selections via UI
- No automatic dosing or section control
- Used for testing or special applications

### Semi-Automatic Mode (2)
- User manually selects which sections are active via UI
- Dosing is automatically controlled to maintain target rate
- Useful when selective spraying is needed

---

## Appendix C: Data Type Reference

| Type | Size | Range | Notes |
|------|:----:|-------|-------|
| Byte | 1 | 0-255 | Unsigned 8-bit |
| Short | 2 | -32768 to 32767 | Signed 16-bit, Little Endian |
| UShort | 2 | 0-65535 | Unsigned 16-bit, Little Endian |
| Float | 4 | ±3.4×10³⁸ | IEEE 754 single precision |
| Double | 8 | ±1.8×10³⁰⁸ | IEEE 754 double precision |
| ULong | 8 | 0 to 2⁶⁴-1 | Unsigned 64-bit, Little Endian |
