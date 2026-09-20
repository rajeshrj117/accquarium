package com.premium.aquarium.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.premium.aquarium.model.Bubble
import com.premium.aquarium.model.FoodPellet
import com.premium.aquarium.model.Particle
import com.premium.aquarium.model.ParticleType

@Composable
fun ParticleLayer(
    bubbles: List<Bubble>,
    particles: List<Particle>,
    food: List<FoodPellet>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        bubbles.forEach { bubble ->
            drawCircle(
                color = Color(0x44AADDFF),
                radius = bubble.radius,
                center = Offset(bubble.x, bubble.y),
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = bubble.radius * 0.4f,
                center = Offset(bubble.x - bubble.radius * 0.25f, bubble.y - bubble.radius * 0.25f)
            )
        }

        food.filter { !it.eaten }.forEach { pellet ->
            drawCircle(
                color = Color(0x33000000),
                radius = pellet.radius * 0.8f,
                center = Offset(pellet.x + 2f, pellet.y + 2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD4A853), Color(0xFF8B6914)),
                    center = Offset(pellet.x, pellet.y),
                    radius = pellet.radius
                ),
                radius = pellet.radius,
                center = Offset(pellet.x, pellet.y),
                alpha = pellet.alpha
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f * pellet.alpha),
                radius = pellet.radius * 0.3f,
                center = Offset(pellet.x - pellet.radius * 0.3f, pellet.y - pellet.radius * 0.3f)
            )
        }

        particles.forEach { p ->
            withTransform({
                translate(p.x, p.y)
                rotate(p.rotation)
                scale(p.scale)
            }) {
                when (p.type) {
                    ParticleType.SPARKLE -> drawStar(p.color.copy(alpha = p.alpha))
                    ParticleType.RIPPLE -> {
                        val expansionScale = 1f + (1f - p.lifetime) * 4f
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha * 0.5f),
                            radius = 30f * expansionScale,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha * 0.2f),
                            radius = 50f * expansionScale,
                            style = Stroke(width = 1f)
                        )
                    }
                    ParticleType.HAPPINESS -> drawHeart(p.color.copy(alpha = p.alpha), 12f * p.scale)
                    ParticleType.SPLASH, ParticleType.DUST -> {
                        drawCircle(color = p.color.copy(alpha = p.alpha), radius = 4f * p.scale)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(color: Color) {
    val path = Path()
    val outerR = 8f
    val innerR = 4f
    repeat(5) { i ->
        val outerAngle = Math.toRadians(-90.0 + i * 72.0)
        val innerAngle = Math.toRadians(-90.0 + i * 72.0 + 36.0)
        if (i == 0) {
            path.moveTo((outerR * kotlin.math.cos(outerAngle)).toFloat(), (outerR * kotlin.math.sin(outerAngle)).toFloat())
        } else {
            path.lineTo((outerR * kotlin.math.cos(outerAngle)).toFloat(), (outerR * kotlin.math.sin(outerAngle)).toFloat())
        }
        path.lineTo((innerR * kotlin.math.cos(innerAngle)).toFloat(), (innerR * kotlin.math.sin(innerAngle)).toFloat())
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawHeart(color: Color, size: Float) {
    val path = Path().apply {
        moveTo(0f, size * 0.25f)
        cubicTo(-size * 0.5f, -size * 0.1f, -size, size * 0.1f, 0f, size)
        cubicTo(size, size * 0.1f, size * 0.5f, -size * 0.1f, 0f, size * 0.25f)
    }
    drawPath(path, color)
}
