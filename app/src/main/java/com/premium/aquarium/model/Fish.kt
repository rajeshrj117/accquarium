package com.premium.aquarium.model

import androidx.compose.ui.graphics.Color

enum class FishState {
    SWIMMING,
    CURIOUS,
    EATING,
    FLEEING,
    IDLE
}

enum class FishType(
    val displayName: String,
    val color: Color,
    val accentColor: Color,
    val speed: Float,
    val size: Float,
    val price: Int
) {
    CLOWNFISH("Nemo", Color(0xFFFF6B35), Color(0xFFFFFFFF), 2.5f, 60f, 0),
    BETTA("Betta King", Color(0xFF6C63FF), Color(0xFF00E5FF), 3f, 70f, 150),
    GOLDFISH("Goldie", Color(0xFFFFD700), Color(0xFFFF8C00), 2f, 65f, 0),
    ANGELFISH("Angel", Color(0xFF4FC3F7), Color(0xFFE1F5FE), 2.2f, 80f, 200),
    GUPPY("Rainbow", Color(0xFF76FF03), Color(0xFF00E5FF), 3.5f, 45f, 100),
    DISCUS("Emperor", Color(0xFF26C6DA), Color(0xFFFF7043), 1.8f, 90f, 500)
}

data class Fish(
    val id: Int,
    val type: FishType,
    var x: Float,
    var y: Float,
    var targetX: Float = x,
    var targetY: Float = y,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var rotation: Float = 0f,
    var state: FishState = FishState.SWIMMING,
    var happiness: Float = 0.8f,
    var hunger: Float = 0.2f,
    var health: Float = 1.0f,
    var scale: Float = 1f,
    var tailWagPhase: Float = 0f,
    var finWavePhase: Float = 0f,
    var idleTimer: Float = 0f,
    var stateTimer: Float = 0f,
    var controlX1: Float = 0f,
    var controlX2: Float = 0f,
    var controlY1: Float = 0f,
    var controlY2: Float = 0f,
    var pathProgress: Float = 0f,
    var name: String = type.displayName,
    var level: Int = 1,
    var experience: Float = 0f
)
