package com.meaning.app.kernel

import com.meaning.app.db.QuantizedNarrativeEntity

data class SearchResult(
    val entity: QuantizedNarrativeEntity,
    val similarity: Float,
    val distance3D: Float
)
