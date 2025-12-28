package com.meaning.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meaning.app.kernel.Narrative3DGestureController

@Composable
fun CameraInfoOverlay(
    cameraState: Narrative3DGestureController.CameraState,
    fps: Float = 0f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Cím
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Kamera",
                    tint = Color.Cyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KAMERA ÁLLAPOT",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pozíció
            InfoRow(
                label = "Pozíció",
                value = "(%.2f, %.2f, %.2f)".format(
                    cameraState.position.x,
                    cameraState.position.y,
                    cameraState.position.z
                ),
                icon = Icons.Default.LocationOn,
                color = Color(0xFF4CAF50)
            )
            
            // Forgatás
            InfoRow(
                label = "Forgatás",
                value = "(%.1f°, %.1f°, %.1f°)".format(
                    cameraState.rotation.x,
                    cameraState.rotation.y,
                    cameraState.rotation.z
                ),
                icon = Icons.Default.RotateRight,
                color = Color(0xFF2196F3)
            )
            
            // Zoom
            InfoRow(
                label = "Zoom",
                value = "%.2fx".format(cameraState.zoom),
                icon = Icons.Default.ZoomIn,
                color = Color(0xFFFF9800)
            )
            
            // FOV
            InfoRow(
                label = "Látószög",
                value = "%.0f°".format(cameraState.fov),
                icon = Icons.Default.Visibility,
                color = Color(0xFF9C27B0)
            )
            
            // FPS
            if (fps > 0) {
                InfoRow(
                    label = "FPS",
                    value = "%.1f".format(fps),
                    icon = Icons.Default.Speed,
                    color = if (fps > 50) Color.Green else if (fps > 30) Color.Yellow else Color.Red
                )
            }
            
            // Interakciós útmutató
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Vezérlés:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "• Drag - forgatás",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = "• Pinch - zoom",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = "• Two-finger drag - mozgatás",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
