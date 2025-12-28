package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity

data class SearchResult(
    val entity: QuantizedNarrativeEntity,
    val similarity: Float,               // 0.0 - 1.0
    val distance3D: Float,               // 3D térbeli távolság
    val rankingScore: Float = 0f,        // Összesített rangsorolási pontszám
    val matchReasons: List<String> = emptyList()  // Egyezés okai
) {
    val isStrongMatch: Boolean get() = similarity > 0.7f
    val isWeakMatch: Boolean get() = similarity in 0.4f..0.7f
    
    fun getFormattedSimilarity(): String = "${(similarity * 100).toInt()}%"
}
