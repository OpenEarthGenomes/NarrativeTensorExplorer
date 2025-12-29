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
import com.meaning.app.kernel.QuantizationEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Kernel példányosítása a JNI teszteléséhez
        val engine = QuantizationEngine()

        setContent {
            var text by remember { mutableStateOf("") }
            val items = remember { mutableStateListOf<String>() }
            var zoom by remember { mutableStateOf(16f) }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Narrative Explorer v1.1", fontSize = (zoom + 4).sp)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = text, 
                            onValueChange = { text = it }, 
                            label = { Text("Bevitel") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = { 
                                if(text.isNotBlank()) {
                                    items.add(0, text)
                                    text = "" 
                                }
                            }) { Text("Mentés") }
                            
                            Row {
                                Button(onClick = { zoom += 2f }) { Text("+") }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(onClick = { zoom -= 2f }) { Text("-") }
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items) { item ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(
                                        text = item, 
                                        fontSize = zoom.sp, 
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
