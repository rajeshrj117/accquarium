package com.premium.aquarium.model

import androidx.compose.ui.graphics.Color

enum class ParticleType {
    SPARKLE,
    RIPPLE,
    SPLASH,
    HAPPINESS,
    DUST
}

data class Particle(
    val id: Int,
    var x: Float,
    var y: Float,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    var rotation: Float = 0f,
    var color: Color = Color.White,
    val type: ParticleType,
    var lifetime: Float = 1f,
    var maxLifetime: Float = 1f
)
