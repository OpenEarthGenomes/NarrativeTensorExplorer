package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // C++ Könyvtár betöltése
        val kernelLoaded = try {
            System.loadLibrary("meaning_kernel")
            true
        } catch (e: Throwable) { false }

        setContent {
            var searchText by remember { mutableStateOf("") }
            val results = remember { mutableStateListOf<String>() }
            var zoomLevel by remember { mutableStateOf(16f) }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Narrative Tensor Explorer", fontSize = (zoomLevel + 4).sp)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Keresés (Adatbázis szimuláció)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = { 
                                if (searchText.isNotBlank()) {
                                    results.add(0, "Bevitel: $searchText | Kernel: ${if(kernelLoaded) "OK" else "HIBA"}")
                                    searchText = ""
                                }
                            }) { Text("Indítás") }

                            Row {
                                Button(onClick = { zoomLevel += 2f }) { Text("+") }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(onClick = { zoomLevel -= 2f }) { Text("-") }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(results) { item ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(item, modifier = Modifier.padding(12.dp), fontSize = zoomLevel.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
