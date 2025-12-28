package com.meaning.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.meaning.app.db.QuantizedNarrativeEntity
import com.meaning.app.kernel.Narrative3DGestureController
import kotlin.math.*

@Composable
fun NarrativeMap3DView(
    modifier: Modifier = Modifier,
    cameraState: Narrative3DGestureController.CameraState,
    entities: List<QuantizedNarrativeEntity>,
    connections: List<NarrativeConnection>,
    onEntitySelected: (QuantizedNarrativeEntity) -> Unit = {}
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        val center = size.center
        val viewMatrix = cameraState.getViewMatrix()
        
        // 3D pontok vetítése
        val projectedPoints = entities.map { entity ->
            val point3D = Offset(
                x = entity.coordX * size.width * 0.4f,
                y = entity.coordY * size.height * 0.4f
            )
            
            // Egyszerűsített 3D vetítés
            val projected = project3DTo2D(
                point3D,
                cameraState.position.z,
                cameraState.zoom,
                center
            )
            
            ProjectedPoint(entity, projected, entity.semanticDensity)
        }
        
        // Kapcsolatok rajzolása
        connections.forEach { connection ->
            val fromPoint = projectedPoints.find { it.entity.id == connection.fromId }
            val toPoint = projectedPoints.find { it.entity.id == connection.toId }
            
            if (fromPoint != null && toPoint != null) {
                drawLine(
                    color = getConnectionColor(connection.connectionType)
                        .copy(alpha = connection.strength * 0.5f),
                    start = fromPoint.position,
                    end = toPoint.position,
                    strokeWidth = connection.strength * 4f,
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Entitások rajzolása
        projectedPoints.forEach { point ->
            val radius = 5f + (point.density * 20f)
            
            // Fényeffekt
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = point.position,
                    radius = radius * 2
                ),
                radius = radius * 2,
                center = point.position
            )
            
            // Fő kör
            drawCircle(
                color = getFamilyColor(point.entity.metaphorFamily),
                radius = radius,
                center = point.position
            )
            
            // Szöveg (ha elég nagy)
            if (radius > 10) {
                drawContext.canvas.nativeCanvas.drawText(
                    point.entity.term,
                    point.position.x,
                    point.position.y - radius - 5,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
        
        // Középpont jelzés
        drawCircle(
            color = Color.Cyan.copy(alpha = 0.3f),
            radius = 10f,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

private fun project3DTo2D(
    point: Offset,
    cameraZ: Float,
    zoom: Float,
    center: Offset
): Offset {
    // Egyszerűsített perspektív vetítés
    val depth = 1f / (cameraZ + 2f)
    val scale = zoom * depth
    
    return Offset(
        x = center.x + point.x * scale,
        y = center.y + point.y * scale
    )
}

private fun getFamilyColor(family: String): Color {
    return when (family.lowercase()) {
        "természet" -> Color(0xFF4CAF50)
        "érzelem" -> Color(0xFFF44336)
        "absztrakt" -> Color(0xFF2196F3)
        "idő" -> Color(0xFF9C27B0)
        "test" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

private fun getConnectionColor(type: String): Color {
    return when (type) {
        "nature_emotion" -> Color(0xFF4CAF50)
        "abstract_link" -> Color(0xFF2196F3)
        "intra_family" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
}

data class ProjectedPoint(
    val entity: QuantizedNarrativeEntity,
    val position: Offset,
    val density: Float
)

data class NarrativeConnection(
    val fromId: Long,
    val toId: Long,
    val strength: Float,
    val connectionType: String
)
