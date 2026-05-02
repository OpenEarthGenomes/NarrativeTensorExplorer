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
import com.meaning.app.db.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = NarrativeDatabase.getDatabase(this)
        val dao = db.narrativeDao()
        
        setContent {
            MeaningAppTheme { // Az új színes téma használata
                val scope = rememberCoroutineScope()
                var text by remember { mutableStateOf("") }
                val items by dao.getAllNarratives().collectAsState(initial = emptyList())
                var fontSize by remember { mutableStateOf(16f) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Narrative Tensor Explorer", 
                             color = MaterialTheme.colorScheme.primary,
                             fontSize = 24.sp, 
                             style = MaterialTheme.typography.headlineMedium)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = text, 
                            onValueChange = { text = it }, 
                            label = { Text("Új gondolat rögzítése") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            Button(
                                onClick = { 
                                    scope.launch { 
                                        if(text.isNotBlank()){ 
                                            dao.insert(NarrativeEntity(content = text))
                                            text = "" 
                                        } 
                                    } 
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("SQL MENTÉS") }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(onClick = { fontSize += 2f }) { Text("+") }
                            Button(onClick = { fontSize -= 2f }) { Text("-") }
                        }

                        LazyColumn {
                            items(items) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(item.content, fontSize = fontSize.sp, modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
