package com.premium.aquarium.engine

import com.premium.aquarium.model.*
import com.premium.aquarium.utils.MathUtils
import kotlin.math.*
import kotlin.random.Random

class FishAI(
    private val tankWidth: Float,
    private val tankHeight: Float
) {

    companion object {
        const val EDGE_MARGIN = 80f
        const val FOOD_DETECT_RADIUS = 200f
        const val MIN_FISH_DISTANCE = 50f
    }

    fun update(
        fish: Fish,
        allFish: List<Fish>,
        foodPellets: List<FoodPellet>,
        deltaTime: Float,
        fingerX: Float?,
        fingerY: Float?
    ) {
        updateTimers(fish, deltaTime)
        updateStateLogic(fish, allFish, foodPellets, fingerX, fingerY)
        updateMovement(fish, deltaTime)
        updateHunger(fish, deltaTime)
        clampToBounds(fish)
    }

    private fun updateTimers(fish: Fish, deltaTime: Float) {
        fish.stateTimer -= deltaTime
        fish.idleTimer -= deltaTime
        fish.tailWagPhase += deltaTime * getWagSpeed(fish)
        fish.finWavePhase += deltaTime * 1.2f
    }

    private fun getWagSpeed(fish: Fish): Float = when (fish.state) {
        FishState.EATING -> 8f
        FishState.FLEEING -> 12f
        FishState.CURIOUS -> 6f
        FishState.SWIMMING -> 4f + fish.type.speed
        FishState.IDLE -> 2f
    }

    private fun updateStateLogic(
        fish: Fish,
        allFish: List<Fish>,
        foodPellets: List<FoodPellet>,
        fingerX: Float?,
        fingerY: Float?
    ) {
        val nearestFood = findNearestFood(fish, foodPellets)
        val fingerNear = fingerX != null && fingerY != null &&
            MathUtils.distance(fish.x, fish.y, fingerX, fingerY) < FOOD_DETECT_RADIUS

        when (fish.state) {
            FishState.IDLE -> {
                if (fish.idleTimer <= 0 || nearestFood != null) {
                    transitionToSwimming(fish, allFish)
                }
                if (nearestFood != null && fish.hunger > 0.3f) {
                    transitionToEating(fish, nearestFood)
                }
            }

            FishState.SWIMMING -> {
                if (nearestFood != null && fish.hunger > 0.2f) {
                    transitionToEating(fish, nearestFood)
                    return
                }
                if (fingerNear && fingerX != null && fingerY != null) {
                    transitionToCurious(fish, fingerX, fingerY)
                    return
                }
                if (fish.stateTimer <= 0 && Random.nextFloat() < 0.3f) {
                    transitionToIdle(fish)
                    return
                }
                if (hasReachedTarget(fish)) {
                    setNewSwimTarget(fish, allFish)
                }
            }

            FishState.CURIOUS -> {
                if (nearestFood != null) {
                    transitionToEating(fish, nearestFood)
                    return
                }
                if (!fingerNear || fingerX == null || fingerY == null) {
                    transitionToSwimming(fish, allFish)
                    return
                }
                fish.targetX = fingerX + Random.nextFloat() * 40f - 20f
                fish.targetY = fingerY + Random.nextFloat() * 40f - 20f
            }

            FishState.EATING -> {
                val food = foodPellets.firstOrNull {
                    !it.eaten && MathUtils.distance(fish.x, fish.y, it.x, it.y) < 30f
                }
                if (food != null) {
                    food.eaten = true
                    fish.hunger = maxOf(0f, fish.hunger - 0.4f)
                    fish.happiness = minOf(1f, fish.happiness + 0.2f)
                    fish.experience += 10f
                    transitionToSwimming(fish, allFish)
                } else if (nearestFood == null) {
                    transitionToSwimming(fish, allFish)
                } else {
                    fish.targetX = nearestFood.x
                    fish.targetY = nearestFood.y
                }
            }

            FishState.FLEEING -> {
                if (fish.stateTimer <= 0) {
                    transitionToSwimming(fish, allFish)
                }
            }
        }
    }

    private fun transitionToSwimming(fish: Fish, allFish: List<Fish>) {
        fish.state = FishState.SWIMMING
        fish.stateTimer = Random.nextFloat() * 4f + 2f
        setNewSwimTarget(fish, allFish)
    }

    private fun transitionToIdle(fish: Fish) {
        fish.state = FishState.IDLE
        fish.idleTimer = Random.nextFloat() * 2f + 0.5f
        fish.stateTimer = fish.idleTimer
        fish.targetX = fish.x
        fish.targetY = fish.y
    }

    private fun transitionToEating(fish: Fish, food: FoodPellet) {
        fish.state = FishState.EATING
        fish.stateTimer = 5f
        fish.targetX = food.x
        fish.targetY = food.y
        generateCubicPath(fish, fish.targetX, fish.targetY)
    }

    private fun transitionToCurious(fish: Fish, tx: Float, ty: Float) {
        fish.state = FishState.CURIOUS
        fish.stateTimer = 3f
        fish.targetX = tx
        fish.targetY = ty
        generateCubicPath(fish, tx, ty)
    }

    private fun setNewSwimTarget(fish: Fish, allFish: List<Fish>) {
        var attempts = 0
        var tx: Float
        var ty: Float

        do {
            tx = Random.nextFloat() * (tankWidth - 2 * EDGE_MARGIN) + EDGE_MARGIN
            ty = Random.nextFloat() * (tankHeight - 2 * EDGE_MARGIN) + EDGE_MARGIN
            attempts++
        } while (isTooCloseToOtherFish(tx, ty, fish, allFish) && attempts < 5)

        fish.targetX = tx
        fish.targetY = ty
        fish.pathProgress = 0f
        generateCubicPath(fish, tx, ty)
    }

    private fun generateCubicPath(fish: Fish, tx: Float, ty: Float) {
        val dx = tx - fish.x
        val dy = ty - fish.y

        fish.controlX1 = fish.x + dx * 0.25f + (Random.nextFloat() - 0.5f) * 100f
        fish.controlY1 = fish.y + dy * 0.25f + (Random.nextFloat() - 0.5f) * 100f
        fish.controlX2 = fish.x + dx * 0.75f + (Random.nextFloat() - 0.5f) * 100f
        fish.controlY2 = fish.y + dy * 0.75f + (Random.nextFloat() - 0.5f) * 100f
        fish.pathProgress = 0f
    }

    private fun updateMovement(fish: Fish, deltaTime: Float) {
        if (fish.state == FishState.IDLE) {
            fish.x += sin(fish.tailWagPhase * 0.5f) * 0.3f
            fish.y += cos(fish.tailWagPhase * 0.3f) * 0.2f
            return
        }

        val speed = getStateSpeed(fish) * deltaTime

        if (fish.pathProgress < 1f) {
            fish.pathProgress = minOf(1f, fish.pathProgress + speed * 0.5f)
            val t = fish.pathProgress
            val mt = 1f - t

            val newX = mt * mt * mt * fish.x +
                3f * mt * mt * t * fish.controlX1 +
                3f * mt * t * t * fish.controlX2 +
                t * t * t * fish.targetX

            val newY = mt * mt * mt * fish.y +
                3f * mt * mt * t * fish.controlY1 +
                3f * mt * t * t * fish.controlY2 +
                t * t * t * fish.targetY

            val dx = newX - fish.x
            val dy = newY - fish.y
            if (abs(dx) + abs(dy) > 0.5f) {
                val targetRotation = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                fish.rotation = lerpAngle(fish.rotation, targetRotation, 0.15f)
            }

            fish.velocityX = dx
            fish.velocityY = dy
            fish.x = newX
            fish.y = newY
        }
    }

    private fun getStateSpeed(fish: Fish): Float = when (fish.state) {
        FishState.FLEEING -> fish.type.speed * 3f
        FishState.EATING -> fish.type.speed * 2f
        FishState.CURIOUS -> fish.type.speed * 1.5f
        FishState.SWIMMING -> fish.type.speed
        FishState.IDLE -> 0f
    }

    private fun updateHunger(fish: Fish, deltaTime: Float) {
        fish.hunger = minOf(1f, fish.hunger + deltaTime * 0.002f)
        if (fish.hunger > 0.8f) {
            fish.happiness = maxOf(0f, fish.happiness - deltaTime * 0.01f)
        }
    }

    private fun hasReachedTarget(fish: Fish): Boolean =
        fish.pathProgress >= 0.98f ||
        MathUtils.distance(fish.x, fish.y, fish.targetX, fish.targetY) < 20f

    private fun findNearestFood(fish: Fish, pellets: List<FoodPellet>): FoodPellet? =
        pellets
            .filter { !it.eaten && it.y > 50f }
            .minByOrNull { MathUtils.distance(fish.x, fish.y, it.x, it.y) }
            ?.takeIf { MathUtils.distance(fish.x, fish.y, it.x, it.y) < FOOD_DETECT_RADIUS }

    private fun isTooCloseToOtherFish(
        x: Float, y: Float,
        self: Fish, others: List<Fish>
    ): Boolean = others.any { other ->
        other.id != self.id &&
        MathUtils.distance(x, y, other.x, other.y) < MIN_FISH_DISTANCE
    }

    private fun clampToBounds(fish: Fish) {
        fish.x = fish.x.coerceIn(EDGE_MARGIN, tankWidth - EDGE_MARGIN)
        fish.y = fish.y.coerceIn(EDGE_MARGIN, tankHeight - EDGE_MARGIN)
    }

    private fun lerpAngle(current: Float, target: Float, t: Float): Float {
        var diff = ((target - current + 180f) % 360f) - 180f
        return current + diff * t
    }
}
