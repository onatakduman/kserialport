package com.onatakduman.kserialport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onatakduman.kserialport.ui.theme.AndroidkserialportTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidkserialportTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SerialPortDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview
@Composable
fun SerialPortDemoPreview() {
    AndroidkserialportTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SerialPortDemo(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun SerialPortDemo(modifier: Modifier = Modifier) {
    var path by remember { mutableStateOf("/dev/ttyHSL0") }
    var baudRate by remember { mutableStateOf("115200") }
    var dataBits by remember { mutableStateOf("8") }
    var stopBits by remember { mutableStateOf("1") }
    var parity by remember { mutableStateOf("0") }
    var connection by remember { mutableStateOf<SerialPortConnection?>(null) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    var writeData by remember { mutableStateOf("Hello Serial") }
    val scope = rememberCoroutineScope()

    fun addLog(msg: String) {
        logs = logs + msg
    }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text("Path") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = baudRate,
                onValueChange = { baudRate = it },
                label = { Text("Baud") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dataBits,
                onValueChange = { dataBits = it },
                label = { Text("Data") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = stopBits,
                onValueChange = { stopBits = it },
                label = { Text("Stop") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = parity,
                onValueChange = { parity = it },
                label = { Text("Parity (0=N, 1=O, 2=E)") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val port = SerialPort(
                                path, 
                                baudRate.toInt(),
                                dataBits.toInt(),
                                stopBits.toInt(),
                                parity.toInt()
                            )
                            val conn = port.open()
                            withContext(Dispatchers.Main) {
                                connection = conn
                                addLog("Opened $path at $baudRate ($dataBits/$stopBits/$parity)")
                            }

                            // Start reading
                            launch {
                                conn.readFlow.collect { bytes ->
                                    val str = String(bytes)
                                    withContext(Dispatchers.Main) { addLog("RX: $str") }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { addLog("Error: ${e.message}") }
                        }
                    }
                },
                enabled = connection == null
            ) { Text("Open") }

            Button(
                onClick = {
                    try {
                        connection?.close()
                        connection = null
                        addLog("Closed")
                    } catch (e: Exception) {
                        addLog("Error closing: ${e.message}")
                    }
                },
                enabled = connection != null
            ) { Text("Close") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = writeData,
            onValueChange = { writeData = it },
            label = { Text("Data to Write") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    try {
                        connection?.write(writeData.toByteArray())
                        addLog("TX: $writeData")
                    } catch (e: Exception) {
                        addLog("Write Error: ${e.message}")
                    }
                }
            },
            enabled = connection != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Write") }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Logs:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(logs) { log -> Text(text = log, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
