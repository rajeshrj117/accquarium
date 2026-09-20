package com.premium.aquarium.engine

import com.premium.aquarium.model.Decoration
import com.premium.aquarium.model.DecorationType
import kotlin.math.PI
import kotlin.random.Random

class DecorationManager(
    private val tankWidth: Float,
    private val tankHeight: Float
) {
    private var nextId = 5000

    fun createDefault(): List<Decoration> {
        val bottomY = tankHeight * 0.9f

        return listOf(
            Decoration(id = nextId++, type = DecorationType.ROCK_LARGE, x = tankWidth * 0.12f, y = bottomY),
            Decoration(id = nextId++, type = DecorationType.ROCK_SMALL, x = tankWidth * 0.22f, y = bottomY - 10f),
            Decoration(id = nextId++, type = DecorationType.SEAWEED_TALL, x = tankWidth * 0.35f, y = bottomY, swaySpeed = 1.2f, swayAmplitude = 4f),
            Decoration(id = nextId++, type = DecorationType.SEAWEED_SHORT, x = tankWidth * 0.40f, y = bottomY, swayPhase = PI.toFloat() * 0.5f, swayAmplitude = 3f),
            Decoration(id = nextId++, type = DecorationType.CORAL_RED, x = tankWidth * 0.72f, y = bottomY, swayPhase = PI.toFloat()),
            Decoration(id = nextId++, type = DecorationType.CORAL_PINK, x = tankWidth * 0.82f, y = bottomY - 5f, swayPhase = PI.toFloat() * 1.5f),
            Decoration(id = nextId++, type = DecorationType.TREASURE_CHEST, x = tankWidth * 0.55f, y = bottomY),
            Decoration(id = nextId++, type = DecorationType.SHELL, x = tankWidth * 0.88f, y = bottomY)
        )
    }

    fun addDecoration(type: DecorationType, x: Float, y: Float): Decoration {
        return Decoration(
            id = nextId++,
            type = type,
            x = x,
            y = y,
            swayPhase = Random.nextFloat() * 2f * PI.toFloat(),
            swayAmplitude = 3f + Random.nextFloat() * 2f
        )
    }
}
