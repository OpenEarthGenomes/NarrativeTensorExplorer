package com.meaning.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meaning.app.db.QuantizedNarrativeEntity
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DimensionForestView(
    entities: List<QuantizedNarrativeEntity>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF05080E),
                        Color(0xFF1A1B2E)
                    )
                )
            )
    ) {
        // Erdő alapréteg: A 3D vetítés rajzolása
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 0.8f // Kicsit alacsonyabb horizont

            entities.forEachIndexed { index, entity ->
                drawNarrativeTree(
                    entity = entity,
                    centerX = centerX,
                    centerY = centerY,
                    index = index
                )
            }
        }

        // Overlay információk
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = "DIMENZIÓ ERDŐ",
                style = TextStyle(
                    color = Color.Cyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(color = Color.Cyan, blurRadius = 10f)
                )
            )
            Text(
                text = "${entities.size} narratív entitás ökoszisztémája",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

private fun DrawScope.drawNarrativeTree(
    entity: QuantizedNarrativeEntity,
    centerX: Float,
    centerY: Float,
    index: Int
) {
    // 3D -> 2D vetítés alapú elhelyezés
    // A koordináták (X, Z) határozzák meg a földön elfoglalt helyet
    val scale = 150f
    val xPos = centerX + (entity.coordX * scale)
    val zPos = entity.coordZ * scale // Mélység érzet
    val yPos = centerY - (entity.coordY * 50f) // Magasság

    // Szín meghatározása a metafora család alapján
    val treeColor = when (entity.metaphorFamily.lowercase()) {
        "természet" -> Color(0xFF4CAF50)
        "érzelem" -> Color(0xFFF44336)
        "absztrakt" -> Color(0xFF9C27B0)
        "technológia" -> Color(0xFF03A9F4)
        else -> Color.Cyan
    }

    // A sűrűség (semanticDensity) határozza meg a fa méretét
    val treeHeight = 40f + (entity.semanticDensity * 120f)
    val trunkWidth = 4f + (entity.semanticDensity * 10f)

    // Törzs rajzolása
    drawLine(
        color = treeColor.copy(alpha = 0.4f),
        start = Offset(xPos, centerY),
        end = Offset(xPos, centerY - treeHeight),
        strokeWidth = trunkWidth
    )

    // Lomb/Energiafelhő rajzolása (különböző formák a család szerint)
    when (entity.metaphorFamily.lowercase()) {
        "természet" -> {
            drawCircle(
                color = treeColor.copy(alpha = 0.6f),
                radius = treeHeight / 3,
                center = Offset(xPos, centerY - treeHeight)
            )
        }
        "technológia" -> {
            drawRect(
                color = treeColor.copy(alpha = 0.6f),
                topLeft = Offset(xPos - 20f, centerY - treeHeight - 20f),
                size = androidx.compose.ui.geometry.Size(40f, 40f)
            )
        }
        else -> {
            // Absztrakt csillag alakzat
            val points = 5
            val innerRadius = treeHeight / 6
            val outerRadius = treeHeight / 3
            for (i in 0 until points * 2) {
                val angle = i * Math.PI / points
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val nextAngle = (i + 1) * Math.PI / points
                val nextR = if ((i + 1) % 2 == 0) outerRadius else innerRadius
                
                drawLine(
                    color = treeColor,
                    start = Offset(
                        xPos + (r * cos(angle)).toFloat(),
                        centerY - treeHeight + (r * sin(angle)).toFloat()
                    ),
                    end = Offset(
                        xPos + (nextR * cos(nextAngle)).toFloat(),
                        centerY - treeHeight + (nextR * sin(nextAngle)).toFloat()
                    ),
                    strokeWidth = 2f
                )
            }
        }
    }

    // Felirat a fa felett (csak ha elég nagy a sűrűség)
    if (entity.semanticDensity > 0.5f) {
        // Megjegyzés: A szövegrajzoláshoz NativeCanvas vagy BasicText kellene a Canvas-en belül,
        // itt most a vizuális reprezentációra fókuszálunk.
    }
}
