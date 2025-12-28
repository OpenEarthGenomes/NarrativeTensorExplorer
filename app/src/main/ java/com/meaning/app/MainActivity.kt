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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.Narrative3DGestureController
import com.meaning.app.kernel.NarrativeKernel
import com.meaning.app.ui.*
import com.meaning.app.ui.theme.MeaningAppTheme
import kotlin.math.PI
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    
    // Lazy inicializálás: csak akkor jön létre, amikor először használjuk (gyorsabb app indítás)
    private val database by lazy { NarrativeDatabase.getInstance(this) }
    private val narrativeKernel by lazy { NarrativeKernel(database.narrativeDao()) }
    private val gestureController by lazy { Narrative3DGestureController() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 15/16 teljes kijelzős mód (edge-to-edge) aktiválása
        enableEdgeToEdge()
        
        setContent {
            MeaningAppTheme {
                // A te MeaningAppContent hívásod, kiegészítve a kernel és controller példányokkal
                MeaningAppContent(
                    narrativeKernel = narrativeKernel,
                    gestureController = gestureController
                )
            }
        }
    }
}

@Composable
fun MeaningAppContent(
    narrativeKernel: NarrativeKernel,
    gestureController: Narrative3DGestureController
) {
    var currentView by remember { mutableStateOf(AppView.THREE_D_MAP) }
    var searchText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Kamera állapot figyelése
    val cameraState by produceState(gestureController.getCurrentState()) {
        gestureController.startAutoRotation(
            speed = 0.05f,
            scope = scope
        ) { newState ->
            value = newState
        }
    }
    
    // Scaffold az UI keretrendszerhez (TopBar és tartalom)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("🔮 NARRATÍV TÉRHAJÓZÓ") },
                elevation = 8.dp,
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
                        narrativeKernel = narrativeKernel,
                        gestureController = gestureController,
                        cameraState = cameraState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppView.DIMENSION_FOREST -> {
                    DimensionForestView(
                        tokens = generateSampleTokens(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Interaktív réteg
            ControlOverlay(
                cameraState = cameraState,
                onControlClick = { direction -> gestureController.moveCamera(direction, 0.3f) },
                onResetClick = { gestureController.resetCamera() },
                onSearchClick = { showSearch = true },
                onExportClick = { /* Export logika */ },
                modifier = Modifier.fillMaxSize()
            )
            
            if (showSearch) {
                SearchOverlay(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    onSearch = { showSearch = false },
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
fun ThreeDMapView(
    narrativeKernel: NarrativeKernel,
    gestureController: Narrative3DGestureController,
    cameraState: Narrative3DGestureController.CameraState,
    modifier: Modifier = Modifier
) {
    var entities by remember { mutableStateOf<List<com.meaning.app.db.QuantizedNarrativeEntity>>(emptyList()) }
    var connections by remember { mutableStateOf<List<NarrativeConnection>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val map = narrativeKernel.generate3DMap(maxPoints = 50)
        entities = map.points
        connections = map.connections
    }
    
    Box(modifier = modifier) {
        NarrativeMap3DView(
            cameraState = cameraState,
            entities = entities,
            connections = connections,
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

private fun generateSampleTokens(): List<VisualToken> {
    return listOf(
        VisualToken("Tenger", 0.95f, 0f),
        VisualToken("Szabadság", 0.92f, (PI / 6).toFloat()),
        VisualToken("Szeretet", 0.85f, (PI / 2).toFloat()),
        VisualToken("Halál", 0.45f, (3 * PI / 2).toFloat())
    )
}

enum class AppView { THREE_D_MAP, DIMENSION_FOREST }
