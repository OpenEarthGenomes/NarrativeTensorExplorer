package com.meaning.app.kernel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import kotlinx.coroutines.*
import kotlin.math.*

class Narrative3DGestureController {
    
    data class CameraState(
        var position: Vector3D = Vector3D(0f, 0f, -3f),
        var rotation: Vector3D = Vector3D(0f, 0f, 0f), // Euler angles
        var zoom: Float = 1.0f,
        var fov: Float = 60f
    ) {
        fun getViewMatrix(): FloatArray {
            // Egyszerűsített nézetmátrix
            val matrix = FloatArray(16) { 0f }
            
            // Identity
            matrix[0] = 1f; matrix[5] = 1f; matrix[10] = 1f; matrix[15] = 1f
            
            // Translation
            matrix[12] = -position.x
            matrix[13] = -position.y
            matrix[14] = -position.z
            
            // Rotation (simplified)
            val cosY = cos(Math.toRadians(rotation.y.toDouble())).toFloat()
            val sinY = sin(Math.toRadians(rotation.y.toDouble())).toFloat()
            matrix[0] = cosY; matrix[2] = sinY
            matrix[8] = -sinY; matrix[10] = cosY
            
            // Scale (zoom)
            matrix[0] *= zoom; matrix[5] *= zoom; matrix[10] *= zoom
            
            return matrix
        }
    }
    
    data class Vector3D(var x: Float, var y: Float, var z: Float)
    
    // Állapotok
    private var cameraState = CameraState()
    private var lastDragOffset: Offset? = null
    private var lastPinchDistance: Float = 0f
    private var isRotating = false
    private var animationJob: Job? = null
    
    // === GESTURE KEZELÉS ===
    fun handleDrag(delta: Offset) {
        animationJob?.cancel()
        
        val sensitivity = 0.005f / cameraState.zoom
        cameraState.rotation = Vector3D(
            x = (cameraState.rotation.x - delta.y * sensitivity).coerceIn(-85f, 85f),
            y = (cameraState.rotation.y + delta.x * sensitivity) % 360f,
            z = cameraState.rotation.z
        )
    }
    
    fun handlePinch(scale: Float) {
        cameraState.zoom *= scale
        cameraState.zoom = cameraState.zoom.coerceIn(0.1f, 5f)
    }
    
    fun handleRotation(angle: Float) {
        cameraState.rotation = Vector3D(
            x = cameraState.rotation.x,
            y = (cameraState.rotation.y + angle * 0.5f) % 360f,
            z = cameraState.rotation.z
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
        
        val startState = cameraState.copy()
        val startTime = System.currentTimeMillis()
        
        animationJob = scope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                
                // Easing
                val eased = easeInOutCubic(progress)
                
                // Interpoláció
                cameraState = interpolate(startState, targetState, eased)
                onUpdate(cameraState)
                
                if (progress >= 1f) break
                delay(16) // 60 FPS
            }
        }
    }
    
    fun startAutoRotation(
        speed: Float = 0.1f,
        scope: CoroutineScope,
        onUpdate: (CameraState) -> Unit
    ): Job {
        return scope.launch {
            while (isActive) {
                cameraState.rotation = Vector3D(
                    x = cameraState.rotation.x,
                    y = (cameraState.rotation.y + speed) % 360f,
                    z = cameraState.rotation.z
                )
                onUpdate(cameraState)
                delay(16)
            }
        }
    }
    
    // === KAMERA MOZGATÁS ===
    fun moveCamera(direction: CameraDirection, amount: Float = 0.5f) {
        when (direction) {
            CameraDirection.UP -> cameraState.position.y += amount
            CameraDirection.DOWN -> cameraState.position.y -= amount
            CameraDirection.LEFT -> cameraState.position.x -= amount
            CameraDirection.RIGHT -> cameraState.position.x += amount
            CameraDirection.FORWARD -> cameraState.position.z += amount
            CameraDirection.BACKWARD -> cameraState.position.z -= amount
        }
    }
    
    fun resetCamera() {
        cameraState = CameraState()
    }
    
    fun getCurrentState(): CameraState = cameraState
    
    // === SEGÉDFÜGGVÉNYEK ===
    private fun easeInOutCubic(x: Float): Float {
        return if (x < 0.5) 4 * x * x * x else 1 - (-2 * x + 2).pow(3) / 2
    }
    
    private fun interpolate(start: CameraState, end: CameraState, t: Float): CameraState {
        return CameraState(
            position = Vector3D(
                lerp(start.position.x, end.position.x, t),
                lerp(start.position.y, end.position.y, t),
                lerp(start.position.z, end.position.z, t)
            ),
            rotation = Vector3D(
                lerp(start.rotation.x, end.rotation.x, t),
                lerp(start.rotation.y, end.rotation.y, t),
                lerp(start.rotation.z, end.rotation.z, t)
            ),
            zoom = lerp(start.zoom, end.zoom, t),
            fov = lerp(start.fov, end.fov, t)
        )
    }
    
    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}

enum class CameraDirection {
    UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD
}
