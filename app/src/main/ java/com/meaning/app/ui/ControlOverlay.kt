package com.meaning.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meaning.app.kernel.MapMetrics
import com.meaning.app.kernel.Narrative3DGestureController

@Composable
fun ControlOverlay(
    cameraState: Narrative3DGestureController.CameraState,
    metrics: MapMetrics,
    onControlClick: (String) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // BAL FELSŐ: Kernel Metrikák (Samsung A35 optimalizáció visszajelzés)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("NEON KERNEL AKTÍV", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Entitások: ${metrics.pointCount}", color = Color.White, fontSize = 12.sp)
            Text("Kapcsolatok: ${metrics.connectionCount}", color = Color.White, fontSize = 12.sp)
            Text("Sűrűség: ${"%.2f".format(metrics.averageDensity)}", color = Color.White, fontSize = 12.sp)
        }

        // JOBB ALSÓ: Kamera vezérlők
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = onResetClick,
                backgroundColor = Color.Cyan,
                contentColor = Color.Black,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Camera")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Koordináta kijelző
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "XYZ: ${"%.1f".format(cameraState.position.x)}, ${"%.1f".format(cameraState.position.y)}, ${"%.1f".format(cameraState.position.z)}",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp
                )
            }
        }
    }
}
