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
            val scope = rememberCoroutineScope()
            var text by remember { mutableStateOf("") }
            val items by dao.getAllNarratives().collectAsState(initial = emptyList())
            var zoom by remember { mutableStateOf(16f) }
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Offline Narrative Explorer", fontSize = (zoom + 4).sp)
                        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                        Row {
                            Button(onClick = { scope.launch { if(text.isNotBlank()){ dao.insert(NarrativeEntity(content = text)); text = "" } } }) { Text("SQL Mentés") }
                            Button(onClick = { zoom += 2f }) { Text("+") }
                            Button(onClick = { zoom -= 2f }) { Text("-") }
                        }
                        LazyColumn {
                            items(items) { item ->
                                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                    Text(item.content, fontSize = zoom.sp, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
