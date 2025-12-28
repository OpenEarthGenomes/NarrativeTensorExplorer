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
import com.meaning.app.db.QuantizedNarrativeEntity
import com.meaning.app.kernel.*
import com.meaning.app.ui.*
import com.meaning.app.ui.theme.MeaningAppTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    // Erőforrások lazy betöltése
    private val database by lazy { NarrativeDatabase.getInstance(this) }
    private val narrativeKernel by lazy { NarrativeKernel(database.narrativeDao()) }
    private val gestureController by lazy { Narrative3DGestureController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Teljes kijelzős mód aktiválása
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

    // Kamera állapota
    val cameraState by produceState(gestureController.getCurrentState()) {
        gestureController.startAutoRotation(
            speed = 0.05f,
            scope = scope
        ) { newState -> value = newState }
    }

    // Élő adatfolyam a Kernelből (3D pontok és kapcsolatok)
    val mapData by kernel.observeNarrativeSpace().collectAsState(
        initial = NarrativeMap3D(emptyList(), emptyList(), null, MapMetrics(0, 0, 0f))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("🔮 NARRATÍV TÉRHAJÓZÓ") },
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                actions = {
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
            when (currentView) {
                AppView.THREE_D_MAP -> {
                    ThreeDMapView(
                        mapData = mapData,
                        gestureController = gestureController,
                        cameraState = cameraState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppView.DIMENSION_FOREST -> {
                    // Itt hívjuk meg az új UI komponenst
                    DimensionForestView(
                        entities = mapData.points,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // HUD réteg (Metrikák kijelzése)
            ControlOverlay(
                cameraState = cameraState,
                metrics = mapData.metrics,
                onControlClick = { direction -> gestureController.moveCamera(direction, 0.3f) },
                onResetClick = { gestureController.resetCamera() },
                modifier = Modifier.fillMaxSize()
            )

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

@Composable
fun ThreeDMapView(
    mapData: NarrativeMap3D,
    gestureController: Narrative3DGestureController,
    cameraState: Narrative3DGestureController.CameraState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NarrativeMap3DView(
            cameraState = cameraState,
            entities = mapData.points,
            connections = mapData.connections,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        gestureController.handleDrag(pan)
                        if (abs(zoom - 1.0f) > 0.01f) gestureController.handlePinch(zoom)
                        if (abs(rotation) > 0.1f) gestureController.handleRotation(rotation)
                    }
                }
        )
    }
}

// === ENUMOK ÉS TÉMA ===

enum class AppView { THREE_D_MAP, DIMENSION_FOREST }

@Composable
fun MeaningAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = androidx.compose.ui.graphics.Color.Cyan,
            background = androidx.compose.ui.graphics.Color(0xFF0A0E17),
            surface = androidx.compose.ui.graphics.Color(0xFF1A1B2E)
        ),
        content = content
    )
}
