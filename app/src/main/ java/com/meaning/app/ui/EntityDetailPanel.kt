package com.meaning.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.meaning.app.db.QuantizedNarrativeEntity
import com.meaning.app.kernel.NarrativeKernel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityDetailPanel(
    entity: QuantizedNarrativeEntity,
    narrativeKernel: NarrativeKernel,
    onClose: () -> Unit,
    onNavigateTo3D: (Long) -> Unit = {},
    onShowConnections: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var similarEntities by remember { mutableStateOf<List<SimilarEntity>>(emptyList()) }
    
    LaunchedEffect(entity.id) {
        isLoading = true
        coroutineScope.launch {
            // Hasonló entitások keresése
            val vector = entity.vectorFP32?.toFloatArray() ?: FloatArray(0)
            if (vector.isNotEmpty()) {
                val results = narrativeKernel.findNearest(vector, k = 5)
                similarEntities = results.map { result ->
                    SimilarEntity(
                        entity = result.entity,
                        similarity = result.similarity,
                        distance3D = result.distance3D
                    )
                }
            }
            isLoading = false
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Fejléc
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.term,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Bezárás",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Metafora család chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(entity.metaphorFamily) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        leadingIconContentColor = getFamilyColor(entity.metaphorFamily)
                    )
                )
                
                Text(
                    text = "ID: ${entity.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3D koordináták kártya
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3D Térbeli pozíció",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CoordinateDisplay(
                            label = "X",
                            value = entity.coordX,
                            color = Color.Red
                        )
                        CoordinateDisplay(
                            label = "Y", 
                            value = entity.coordY,
                            color = Color.Green
                        )
                        CoordinateDisplay(
                            label = "Z",
                            value = entity.coordZ,
                            color = Color.Blue
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { onNavigateTo3D(entity.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Megtekintés a térképen")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statisztikák grid
            Text(
                text = "Statisztikák",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Sűrűség",
                    value = "${(entity.semanticDensity * 100).toInt()}%",
                    icon = Icons.Default.DensityMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatCard(
                    title = "Figyelem",
                    value = "${(entity.attentionWeight * 100).toInt()}%",
                    icon = Icons.Default.AttachFile,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatCard(
                    title = "Használat",
                    value = entity.usageCount.toString(),
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hasonló entitások
            Text(
                text = "Hasonló jelentések",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                similarEntities.forEach { similar ->
                    SimilarEntityRow(
                        similarEntity = similar,
                        onClick = { onShowConnections(similar.entity.id) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Művelet gombok
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = "Kapcsolatok",
                    icon = Icons.Default.AccountTree,
                    onClick = { onShowConnections(entity.id) }
                )
                
                ActionButton(
                    text = "Export",
                    icon = Icons.Default.Share,
                    onClick = { /* TODO: Export */ }
                )
                
                ActionButton(
                    text = "Szerkesztés",
                    icon = Icons.Default.Edit,
                    onClick = { /* TODO: Edit */ }
                )
            }
        }
    }
}

@Composable
fun CoordinateDisplay(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        Text(
            text = "%.3f".format(value),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SimilarEntityRow(
    similarEntity: SimilarEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hasonlóság indikátor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            similarEntity.similarity > 0.8 -> Color.Green.copy(alpha = 0.2f)
                            similarEntity.similarity > 0.6 -> Color.Yellow.copy(alpha = 0.2f)
                            else -> Color.Red.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(similarEntity.similarity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Entitás info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = similarEntity.entity.term,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = similarEntity.entity.metaphorFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Távolság
            Text(
                text = "%.2f".format(similarEntity.distance3D),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SimilarEntity(
    val entity: QuantizedNarrativeEntity,
    val similarity: Float,
    val distance3D: Float
)

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
