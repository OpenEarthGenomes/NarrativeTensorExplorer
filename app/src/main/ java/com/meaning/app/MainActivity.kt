package com.meaning.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.* // Importálja a Point3D-t és CameraState-et is
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Adatbázis indítása (egyszerűsített Dependency Injection nélkül a példa kedvéért)
        val db = NarrativeDatabase.getInstance(applicationContext)
        val repository = NarrativeRepository(db.narrativeDao(), db.connectionDao())
        
        // Gesztus vezérlő
        val gestureController = Narrative3DGestureController()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF101018) // Sötét háttér a világűrhöz
                ) {
                    NarrativeSpaceCanvas(gestureController)
                }
            }
        }
    }
}

@Composable
fun NarrativeSpaceCanvas(gestureController: Narrative3DGestureController) {
    // Állapot figyelése a Controllerből
    var cameraState by remember { mutableStateOf(gestureController.currentState) }
    
    // Coroutine scope animációkhoz
    val scope = rememberCoroutineScope()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Zoom és Panning kezelése
                    gestureController.handlePinch(zoom)
                    // Frissítjük a UI állapotot
                    cameraState = gestureController.currentState
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Forgatás kezelése
                    gestureController.handleDrag(dragAmount)
                    cameraState = gestureController.currentState
                }
            }
    ) {
        // === RENDERELÉS (Egyszerűsített debug nézet) ===
        // Itt rajzolnád ki a pontokat a 3D -> 2D vetítéssel
        
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // Debug információ kiírása (hogy lássuk, működik-e a forgatás)
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                "CAM: X=${"%.1f".format(cameraState.rotation.x)} Y=${"%.1f".format(cameraState.rotation.y)} Zoom=${"%.1f".format(cameraState.zoom)}",
                50f,
                100f,
                android.graphics.Paint().apply { 
                    color = android.graphics.Color.WHITE 
                    textSize = 40f
                }
            )
        }
        
        // Egy referencia kocka középen, hogy érezd a teret
        drawCircle(
            color = Color.Cyan,
            radius = 20f * cameraState.zoom,
            center = Offset(centerX, centerY)
        )
    }
}
