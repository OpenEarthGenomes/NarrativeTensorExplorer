package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Park
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.Narrative3DGestureController
import com.meaning.app.kernel.NarrativeKernel
import com.meaning.app.kernel.NarrativeMap3D
import com.meaning.app.kernel.MapMetrics
import com.meaning.app.ui.*
import com.meaning.app.ui.theme.MeaningAppTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    // Erőforrások lazy inicializálása a biztonságos indításért
    private val database by lazy { NarrativeDatabase.getInstance(this) }
    private val narrativeKernel by lazy { NarrativeKernel(database.narrativeDao()) }
    private val gestureController by lazy { Narrative3DGestureController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 15+ modern megjelenítés
        enableEdgeToEdge()

        setContent {
            MeaningAppTheme {
                MeaningAppContent(
                    kernel = narrativeKernel,
                    gestureController = gestureController
                )
            }
        }
    }
}

@Composable
fun MeaningAppContent(
    kernel: NarrativeKernel,
    gestureController: Narrative3DGestureController
) {
    var currentView by remember { mutableStateOf(AppView.THREE_D_MAP) }
    var searchText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 1. Kamera állapota a GestureController-ből
    val cameraState by produceState(gestureController.getCurrentState()) {
        gestureController.startAutoRotation(
            speed = 0.05f,
            scope = scope
        ) { newState -> value = newState }
    }

    // 2. Real-time adatfolyam figyelése (a szétválasztott NarrativeMap3D modellt használva)
    val mapData by kernel.observeNarrativeSpace().collectAsState(
        initial = NarrativeMap3D(
            points = emptyList(),
            connections = emptyList(),
            center = null,
            metrics = MapMetrics(0, 0, 0f)
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("🔮 NARRATÍV TÉRHAJÓZÓ") },
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                actions = {
                    // Nézetváltó gombok
                    IconButton(onClick = { currentView = AppView.THREE_D_MAP }) {
                        Icon(
                            imageVector = if (currentView == AppView.THREE_D_MAP) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "3D Térkép"
                        )
                    }
                    IconButton(onClick = { currentView = AppView.DIMENSION_FOREST }) {
                        Icon(
                            imageVector = if (currentView == AppView.DIMENSION_FOREST) Icons.Filled.Park else Icons.Outlined.Park,
                            contentDescription = "Dimenzió Erdő"
                        )
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Keresés")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colors.background)
        ) {
            // Fő tartalom megjelenítése a választott nézet szerint
            when (currentView) {
                AppView.THREE_D_MAP -> {
                    NarrativeMap3DView(
                        cameraState = cameraState,
                        entities = mapData.points,
                        connections = mapData.connections,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    gestureController.handleDrag(pan)
                                    if (abs(zoom - 1.0f) > 0.01f) gestureController.handlePinch(zoom)
                                }
                            }
                    )
                }
                AppView.DIMENSION_FOREST -> {
                    DimensionForestView(
                        entities = mapData.points,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // HUD / Teljesítmény Metrikák (ControlOverlay)
            ControlOverlay(
                cameraState = cameraState,
                metrics = mapData.metrics,
                onControlClick = { /* Irányítási események */ },
                onResetClick = { gestureController.resetCamera() },
                modifier = Modifier.fillMaxSize()
            )

            // Keresési ablak (Overlay)
            if (showSearch) {
                SearchOverlay(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    onSearch = { showSearch = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

// === ENUM A NÉZETEKHEZ ===
enum class AppView { THREE_D_MAP, DIMENSION_FOREST }
