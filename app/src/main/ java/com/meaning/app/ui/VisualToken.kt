package com.meaning.app.ui

data class VisualToken(
    val id: String = "token_${System.currentTimeMillis()}_${(0..9999).random()}",
    val label: String,
    val score: Float,                    // 0.0 - 1.0 hasonlóság
    val angle: Float,                    // 0 - 360 fok
    val radius: Float = 1.0f,            // Méretarány
    val color: TokenColor = TokenColor.NEUTRAL,
    val metadata: Map<String, Any> = emptyMap(),
    val animationState: AnimationState = AnimationState.IDLE
) {
    val scaledScore: Float get() = score * 100
    val isHighlighted: Boolean get() = score > 0.8f
    
    enum class TokenColor {
        HIGH, MEDIUM, LOW, NEUTRAL, SELECTED
    }
    
    enum class AnimationState {
        IDLE, PULSING, SELECTING, FADING
    }
}
