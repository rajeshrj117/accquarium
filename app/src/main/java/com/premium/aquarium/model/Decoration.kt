package com.premium.aquarium.model

enum class DecorationType(
    val displayName: String,
    val price: Int,
    val width: Float,
    val height: Float
) {
    CORAL_RED("Red Coral", 100, 80f, 100f),
    CORAL_PINK("Pink Coral", 120, 70f, 90f),
    SEAWEED_TALL("Tall Seaweed", 80, 40f, 140f),
    SEAWEED_SHORT("Short Seaweed", 50, 35f, 80f),
    TREASURE_CHEST("Treasure Chest", 300, 90f, 70f),
    ROCK_LARGE("Large Rock", 60, 100f, 80f),
    ROCK_SMALL("Small Rock", 40, 60f, 50f),
    CASTLE("Castle", 500, 120f, 150f),
    SHELL("Shell", 80, 50f, 40f),
    STARFISH("Starfish", 90, 60f, 55f),
    ANEMONE("Anemone", 150, 65f, 85f),
    ANCHOR("Anchor", 200, 70f, 110f)
}

data class Decoration(
    val id: Int,
    val type: DecorationType,
    var x: Float,
    var y: Float,
    var swayPhase: Float = 0f,
    var swaySpeed: Float = 1f,
    var swayAmplitude: Float = 3f,
    val isSwaying: Boolean = type in listOf(
        DecorationType.SEAWEED_TALL,
        DecorationType.SEAWEED_SHORT,
        DecorationType.ANEMONE
    )
)
