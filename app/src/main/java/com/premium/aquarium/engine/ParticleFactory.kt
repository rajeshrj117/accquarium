package com.premium.aquarium.engine

import androidx.compose.ui.graphics.Color
import com.premium.aquarium.model.Particle
import com.premium.aquarium.model.ParticleType
import kotlin.math.*
import kotlin.random.Random

object ParticleFactory {

    fun createEatSparkles(x: Float, y: Float, fishColor: Color, nextId: () -> Int): List<Particle> =
        (0..12).map { i ->
            val angle = (i / 12f) * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 80f + 40f
            Particle(
                id = nextId(),
                x = x,
                y = y,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - 30f,
                color = if (i % 2 == 0) fishColor else Color(0xFFFFD700),
                type = ParticleType.SPARKLE,
                maxLifetime = 0.8f,
                lifetime = 0.8f,
                scale = Random.nextFloat() * 0.8f + 0.4f
            )
        }

    fun createRipple(x: Float, y: Float, nextId: () -> Int): List<Particle> = listOf(
        Particle(
            id = nextId(), x = x, y = y, type = ParticleType.RIPPLE,
            color = Color(0x44FFFFFF), maxLifetime = 0.8f, lifetime = 0.8f, scale = 0.1f
        ),
        Particle(
            id = nextId(), x = x, y = y, type = ParticleType.RIPPLE,
            color = Color(0x33FFFFFF), maxLifetime = 1.0f, lifetime = 1.0f, scale = 0.05f
        )
    )

    fun createHappinessHearts(x: Float, y: Float, nextId: () -> Int): List<Particle> =
        (0..3).map {
            Particle(
                id = nextId(),
                x = x + Random.nextFloat() * 40f - 20f,
                y = y,
                velocityX = Random.nextFloat() * 40f - 20f,
                velocityY = -Random.nextFloat() * 60f - 30f,
                color = Color(0xFFFF69B4),
                type = ParticleType.HAPPINESS,
                maxLifetime = 1.2f,
                lifetime = 1.2f
            )
        }

    fun createSplash(x: Float, y: Float, nextId: () -> Int): List<Particle> =
        (0..6).map { i ->
            val angle = -PI.toFloat() / 2f + (i - 3) * 0.4f
            val speed = Random.nextFloat() * 100f + 50f
            Particle(
                id = nextId(),
                x = x, y = y,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed,
                color = Color(0x99FFFFFF),
                type = ParticleType.SPLASH,
                maxLifetime = 0.5f,
                lifetime = 0.5f,
                scale = Random.nextFloat() * 0.5f + 0.3f
            )
        }
}
