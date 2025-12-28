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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.meaning.app.db.NarrativeDatabase
import com.meaning.app.kernel.*
import timber.log.Timber

/**
 * A Narrative Tensor Explorer fő belépési pontja.
 * Ez az osztály kezeli a Compose UI-t és a 3D-s tér vizualizációját.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Adatbázis és Repository példányosítása
        // Később érdemes lesz Hilt-re vagy Koin-ra váltani
        val db = NarrativeDatabase.getInstance(applicationContext)
        val repository = NarrativeRepository(db.narrativeDao(), db.connectionDao())
        
        // 3D vezérlő logika inicializálása
        val gestureController = Narrative3DGestureController()

        Timber.i("MainActivity created. UI starting...")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF101018) // Deep space sötétkék
                ) {
                    NarrativeSpaceCanvas(gestureController)
                }
            }
        }
    }
}

@Composable
fun NarrativeSpaceCanvas(gestureController: Narrative3DGestureController) {
    // Állapot figyelése: ha a controllerben változik valami, a Compose újrarajzolja
    var cameraState by remember { mutableStateOf(gestureController.currentState) }
    
    // Coroutine scope az esetleges animációkhoz (pl. "berúgott" forgatás megállítása)
    val scope = rememberCoroutineScope()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    // Zoom (csíptetés) kezelése
                    gestureController.handlePinch(zoom)
                    cameraState = gestureController.currentState
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // 3D Forgatás (húzás) kezelése
                    gestureController.handleDrag(dragAmount)
                    cameraState = gestureController.currentState
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // --- 3D -> 2D VETÍTÉS ÉS RENDERELÉS ---

        // Debug információk kirajzolása a képernyőre (Native Canvas használatával)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 40f
                isAntiAlias = true
            }
            
            drawText(
                "ROT: X=${"%.1f".format(cameraState.rotation.x)} Y=${"%.1f".format(cameraState.rotation.y)}",
                50f, 100f, paint
            )
            drawText(
                "ZOOM: ${"%.1f".format(cameraState.zoom)}x",
                50f, 150f, paint
            )
        }
        
        // Referencia objektum kirajzolása:
        // Egy központi kör, amely reagál a zoom mértékére
        drawCircle(
            color = Color.Cyan,
            radius = 20f * cameraState.zoom,
            center = Offset(centerX, centerY),
            alpha = 0.8f
        )

        // Itt hívnád meg a QuantizationEngine.transform3DCoordinates függvényt
        // egy pontfelhőre, mielőtt kirajzolod őket a Canvas-ra.
    }
}
