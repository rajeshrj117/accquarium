package com.premium.aquarium.engine

import com.premium.aquarium.model.*
import kotlin.math.*
import kotlin.random.Random

class PhysicsEngine(
    private val tankWidth: Float,
    private val tankHeight: Float
) {
    companion object {
        const val GRAVITY = 120f
        const val WATER_RESISTANCE = 0.98f
    }

    fun updateBubbles(bubbles: MutableList<Bubble>, deltaTime: Float) {
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val bubble = iterator.next()
            bubble.wobblePhase += deltaTime * 2f
            bubble.x += sin(bubble.wobblePhase) * bubble.wobbleAmplitude
            bubble.y += bubble.velocityY * deltaTime * 60f

            if (bubble.y < tankHeight * 0.15f) {
                bubble.alpha -= deltaTime * 2f
            }
            bubble.lifetime -= deltaTime * 0.15f

            if (bubble.lifetime <= 0f || bubble.alpha <= 0f || bubble.y < 0f) {
                iterator.remove()
            }
        }
    }

    fun spawnBubble(bubbles: MutableList<Bubble>, nextId: () -> Int) {
        val x = Random.nextFloat() * tankWidth
        val y = tankHeight * 0.9f + Random.nextFloat() * tankHeight * 0.08f
        bubbles.add(
            Bubble(
                id = nextId(),
                x = x,
                y = y,
                radius = Random.nextFloat() * 6f + 2f,
                velocityY = -(Random.nextFloat() * 1.5f + 0.8f),
                wobbleAmplitude = Random.nextFloat() * 1.5f + 0.3f
            )
        )
    }

    fun updateFood(pellets: MutableList<FoodPellet>, deltaTime: Float) {
        pellets.removeAll { it.eaten && it.alpha <= 0f }

        pellets.forEach { pellet ->
            if (pellet.eaten) {
                pellet.alpha -= deltaTime * 3f
                return@forEach
            }

            pellet.velocityY = minOf(pellet.velocityY + GRAVITY * deltaTime, 60f)
            pellet.velocityX *= WATER_RESISTANCE
            pellet.velocityY *= WATER_RESISTANCE

            pellet.x += pellet.velocityX * deltaTime
            pellet.y += pellet.velocityY * deltaTime

            val settleY = tankHeight * 0.85f
            if (pellet.y >= settleY) {
                pellet.y = settleY
                pellet.velocityY = 0f
                pellet.velocityX *= 0.5f
            }

            pellet.x = pellet.x.coerceIn(20f, tankWidth - 20f)
        }
    }

    fun spawnFood(pellets: MutableList<FoodPellet>, tapX: Float, tapY: Float, nextId: () -> Int) {
        repeat(Random.nextInt(3) + 2) { i ->
            val spread = (i - 1) * 15f
            pellets.add(
                FoodPellet(
                    id = nextId(),
                    x = tapX + spread + Random.nextFloat() * 20f - 10f,
                    y = tapY,
                    velocityY = Random.nextFloat() * 20f + 10f,
                    velocityX = Random.nextFloat() * 30f - 15f
                )
            )
        }
    }

    fun updateParticles(particles: MutableList<Particle>, deltaTime: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.lifetime -= deltaTime / p.maxLifetime
            p.x += p.velocityX * deltaTime
            p.y += p.velocityY * deltaTime
            p.velocityY += 10f * deltaTime
            p.alpha = p.lifetime
            p.scale = p.lifetime
            p.rotation += deltaTime * 180f

            if (p.lifetime <= 0f) iterator.remove()
        }
    }
}
