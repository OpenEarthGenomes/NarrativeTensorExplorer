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
        
        // Biztonságos betöltés: ha a C++ kernel nincs meg, ne omoljon össze az app
        val kernelLoaded = try {
            System.loadLibrary("meaning_kernel")
            true
        } catch (e: Throwable) { false }

        setContent {
            var searchText by remember { mutableStateOf("") }
            val results = remember { mutableStateListOf<String>() }
            var zoomLevel by remember { mutableStateOf(16f) } // Nagyítás alapértéke

            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Narrative Tensor Explorer", fontSize = (zoomLevel + 4).sp)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Keresőmező
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Írj be egy kulcsszót...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = { 
                                if (searchText.isNotBlank()) {
                                    results.add(0, "Találat: $searchText (Kernel: ${if(kernelLoaded) "Aktív" else "Hiányzik"})")
                                    searchText = ""
                                }
                            }) { Text("Keresés") }

                            // Nagyítás gombok
                            Row {
                                Button(onClick = { zoomLevel += 2f }) { Text("+") }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(onClick = { zoomLevel -= 2f }) { Text("-") }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Eredmények (Adatbázis sorok):", fontSize = 14.sp)

                        // Görgethető lista a sorokhoz
                        LazyColumn(modifier = Modifier.fillWeight(1f)) {
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

// Kiegészítő függvény a Modifier-hez
fun Modifier.fillWeight(weight: Float): Modifier = this.then(Modifier.fillMaxHeight(weight))
