package com.premium.aquarium.model

data class Bubble(
    val id: Int,
    var x: Float,
    var y: Float,
    val radius: Float,
    var alpha: Float = 0.6f,
    var velocityY: Float = -1.5f,
    var velocityX: Float = 0f,
    var wobblePhase: Float = 0f,
    var wobbleAmplitude: Float = 0.8f,
    var lifetime: Float = 1f
)
