package com.meaning.app.kernel

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlin.math.*

class Narrative3DGestureController {
    
    // Belső állapot tárolása
    private var _cameraState = CameraState()
    
    // Publikus getter a nézethez
    val currentState: CameraState
        get() = _cameraState

    // Állapotok a gesztusokhoz
    private var animationJob: Job? = null
    
    // === VIEW MATRIX (Rendereléshez) ===
    fun getViewMatrix(): FloatArray {
        val matrix = FloatArray(16) { 0f }
        val state = _cameraState
        
        // Identity
        matrix[0] = 1f; matrix[5] = 1f; matrix[10] = 1f; matrix[15] = 1f
        
        // Translation (Pozíció)
        matrix[12] = -state.position.x
        matrix[13] = -state.position.y
        matrix[14] = -state.position.z
        
        // Rotation (Egyszerűsített Y tengely körüli forgatás)
        val cosY = cos(Math.toRadians(state.rotation.y.toDouble())).toFloat()
        val sinY = sin(Math.toRadians(state.rotation.y.toDouble())).toFloat()
        
        // Mátrix szorzás a forgatással (Z tengely fix)
        val rm0 = matrix[0] * cosY + matrix[2] * -sinY
        val rm2 = matrix[0] * sinY + matrix[2] * cosY
        matrix[0] = rm0
        matrix[2] = rm2
        
        // Scale (Zoom)
        matrix[0] *= state.zoom
        matrix[5] *= state.zoom
        matrix[10] *= state.zoom
        
        return matrix
    }
    
    // === GESTURE KEZELÉS ===
    fun handleDrag(delta: Offset) {
        animationJob?.cancel()
        
        val sensitivity = 0.05f / _cameraState.zoom
        
        // Mivel a CameraState immutable (val), .copy()-t használunk
        val currentRot = _cameraState.rotation
        
        _cameraState = _cameraState.copy(
            rotation = currentRot.copy(
                x = (currentRot.x - delta.y * sensitivity).coerceIn(-85f, 85f),
                y = (currentRot.y + delta.x * sensitivity) % 360f
            )
        )
    }
    
    fun handlePinch(scaleFactor: Float) {
        val newZoom = (_cameraState.zoom * scaleFactor).coerceIn(0.1f, 5.0f)
        _cameraState = _cameraState.copy(zoom = newZoom)
    }
    
    fun handleRotation(angle: Float) {
        val currentRot = _cameraState.rotation
        _cameraState = _cameraState.copy(
            rotation = currentRot.copy(
                y = (currentRot.y + angle * 0.5f) % 360f
            )
        )
    }
    
    // === ANIMÁCIÓK ===
    fun animateTo(
        targetState: CameraState,
        duration: Long = 1000,
        scope: CoroutineScope,
        onUpdate: (CameraState) -> Unit
    ) {
        animationJob?.cancel()
        
        val startState = _cameraState
        val startTime = System.currentTimeMillis()
        
        animationJob = scope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                
                val eased = easeInOutCubic(progress)
                
                _cameraState = interpolate(startState, targetState, eased)
                onUpdate(_cameraState)
                
                if (progress >= 1f) break
                delay(16) // ~60 FPS
            }
        }
    }
    
    fun startAutoRotation(
        speed: Float = 0.1f,
        scope: CoroutineScope,
        onUpdate: (CameraState) -> Unit
    ): Job {
        animationJob?.cancel()
        return scope.launch {
            while (isActive) {
                val currentRot = _cameraState.rotation
                _cameraState = _cameraState.copy(
                    rotation = currentRot.copy(
                        y = (currentRot.y + speed) % 360f
                    )
                )
                onUpdate(_cameraState)
                delay(16)
            }
        }
    }
    
    // === KAMERA MOZGATÁS (Gombokhoz) ===
    fun moveCamera(direction: CameraDirection, amount: Float = 0.5f) {
        val pos = _cameraState.position
        var newPos = pos
        
        when (direction) {
            CameraDirection.UP -> newPos = pos.copy(y = pos.y + amount)
            CameraDirection.DOWN -> newPos = pos.copy(y = pos.y - amount)
            CameraDirection.LEFT -> newPos = pos.copy(x = pos.x - amount)
            CameraDirection.RIGHT -> newPos = pos.copy(x = pos.x + amount)
            CameraDirection.FORWARD -> newPos = pos.copy(z = pos.z + amount)
            CameraDirection.BACKWARD -> newPos = pos.copy(z = pos.z - amount)
        }
        _cameraState = _cameraState.copy(position = newPos)
    }
    
    fun resetCamera() {
        _cameraState = CameraState() // Visszaáll az alapértelmezettre (NarrativeMap3D-ben definiált default)
    }
    
    // === SEGÉDFÜGGVÉNYEK ===
    private fun easeInOutCubic(x: Float): Float {
        return if (x < 0.5) 4 * x * x * x else 1 - (-2 * x + 2).pow(3) / 2
    }
    
    private fun interpolate(start: CameraState, end: CameraState, t: Float): CameraState {
        return CameraState(
            position = Point3D(
                lerp(start.position.x, end.position.x, t),
                lerp(start.position.y, end.position.y, t),
                lerp(start.position.z, end.position.z, t)
            ),
            rotation = Point3D(
                lerp(start.rotation.x, end.rotation.x, t),
                lerp(start.rotation.y, end.rotation.y, t),
                lerp(start.rotation.z, end.rotation.z, t)
            ),
            zoom = lerp(start.zoom, end.zoom, t)
        )
    }
    
    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}

enum class CameraDirection {
    UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD
}
