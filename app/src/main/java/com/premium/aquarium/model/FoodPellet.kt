package com.premium.aquarium.model

data class FoodPellet(
    val id: Int,
    var x: Float,
    var y: Float,
    var velocityY: Float = 0f,
    var velocityX: Float = 0f,
    var eaten: Boolean = false,
    var alpha: Float = 1f,
    val radius: Float = 6f
)
