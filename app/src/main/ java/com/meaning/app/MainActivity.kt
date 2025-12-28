package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.NarrativeKernel
import com.meaning.app.ui.theme.MeaningAppTheme

class MainActivity : ComponentActivity() {
    
    // Lazy inicializálás a jobb teljesítményért
    private val database by lazy { NarrativeDatabase.getInstance(this) }
    private val kernel by lazy { NarrativeKernel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 15/16 edge-to-edge támogatás
        enableEdgeToEdge()
        
        setContent {
            MeaningAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Itt indul a 3D motorod vagy a listád
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        appName = "Meaning Archive v1.1-PRO"
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, appName: String) {
    Text(
        text = "Üdvözöllek a $appName-ban!",
        modifier = modifier
    )
}

