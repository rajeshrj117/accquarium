package com.premium.aquarium.utils

import kotlin.math.sqrt

object MathUtils {
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    fun normalize(value: Float, min: Float, max: Float): Float =
        ((value - min) / (max - min)).coerceIn(0f, 1f)
}
