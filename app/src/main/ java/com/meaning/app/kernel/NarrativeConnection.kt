package com.meaning.app.kernel

data class NarrativeConnection(
    val id: Long,
    val fromId: Long,
    val toId: Long,
    val strength: Float,
    val connectionType: String,
    val distance3D: Float,
    val semanticSimilarity: Float,
    val creationTime: Long,
    val usageCount: Int
)
