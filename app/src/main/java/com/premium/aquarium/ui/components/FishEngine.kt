package com.premium.aquarium.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.premium.aquarium.model.Fish
import com.premium.aquarium.model.FishState
import com.premium.aquarium.model.FishType
import kotlin.math.sin

@Composable
fun FishLayer(fish: List<Fish>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        fish.sortedBy { it.y }.forEach { f -> drawFish(f) }
    }
}

private fun DrawScope.drawFish(fish: Fish) {
    val tailWag = sin(fish.tailWagPhase) * getWagDegrees(fish)
    val finWave = sin(fish.finWavePhase) * 8f
    val facingLeft = fish.rotation > 90f || fish.rotation < -90f

    withTransform({
        translate(fish.x, fish.y)
        rotate(fish.rotation, Offset.Zero)
        scale(if (facingLeft) -1f else 1f, 1f, pivot = Offset.Zero)
        val s = fish.scale * (fish.type.size / 60f)
        scale(s, s, Offset.Zero)
    }) {
        drawFishShadow()
        drawFishBody(fish, tailWag, finWave)
    }
}

private fun getWagDegrees(fish: Fish): Float = when (fish.state) {
    FishState.FLEEING -> 20f
    FishState.EATING -> 25f
    FishState.IDLE -> 5f
    else -> 15f
}

private fun DrawScope.drawFishShadow() {
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(-25f, 10f),
        size = Size(50f, 12f)
    )
}

private fun DrawScope.drawFishBody(fish: Fish, tailWag: Float, finWave: Float) {
    val bodyColor = fish.type.color
    val accentColor = fish.type.accentColor
    val size = fish.type.size

    // Tail fin
    val tailPath = Path().apply {
        moveTo(-size * 0.35f, 0f)
        cubicTo(
            -size * 0.55f, -size * 0.35f - tailWag,
            -size * 0.7f, -size * 0.25f,
            -size * 0.6f, 0f
        )
        cubicTo(
            -size * 0.7f, size * 0.25f,
            -size * 0.55f, size * 0.35f + tailWag,
            -size * 0.35f, 0f
        )
    }
    drawPath(
        path = tailPath,
        brush = Brush.linearGradient(
            colors = listOf(bodyColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.6f)),
            start = Offset(-size * 0.7f, 0f),
            end = Offset(-size * 0.3f, 0f)
        )
    )

    // Main body
    val bodyPath = Path().apply {
        moveTo(size * 0.4f, 0f)
        cubicTo(size * 0.35f, -size * 0.28f, -size * 0.1f, -size * 0.32f, -size * 0.38f, 0f)
        cubicTo(-size * 0.1f, size * 0.32f, size * 0.35f, size * 0.28f, size * 0.4f, 0f)
    }
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                bodyColor.copy(alpha = 0.95f),
                bodyColor,
                bodyColor.copy(red = (bodyColor.red * 0.85f).coerceIn(0f, 1f))
            ),
            startY = -size * 0.3f,
            endY = size * 0.3f
        )
    )

    // Belly highlight
    drawPath(
        path = Path().apply {
            moveTo(size * 0.3f, size * 0.05f)
            cubicTo(size * 0.2f, size * 0.22f, -size * 0.1f, size * 0.25f, -size * 0.25f, size * 0.1f)
        },
        color = Color.White.copy(alpha = 0.2f),
        style = Stroke(width = size * 0.06f)
    )

    drawFishMarkings(fish, size)

    // Dorsal fin
    val dorsalPath = Path().apply {
        moveTo(size * 0.2f, -size * 0.25f)
        cubicTo(
            size * 0.1f + finWave * 0.5f, -size * 0.5f,
            -size * 0.1f + finWave * 0.3f, -size * 0.48f,
            -size * 0.2f, -size * 0.22f
        )
        close()
    }
    drawPath(dorsalPath, color = bodyColor.copy(alpha = 0.7f))

    // Pectoral fin
    val pectoralPath = Path().apply {
        moveTo(size * 0.1f, size * 0.1f)
        cubicTo(
            0f + finWave, size * 0.35f,
            -size * 0.15f + finWave * 0.5f, size * 0.3f,
            -size * 0.1f, size * 0.12f
        )
        close()
    }
    drawPath(pectoralPath, color = accentColor.copy(alpha = 0.5f))

    // Eye
    val eyeX = size * 0.22f
    val eyeRadius = size * 0.1f
    drawCircle(Color.White, eyeRadius, Offset(eyeX, -size * 0.06f))
    drawCircle(Color(0xFF1A1A2E), eyeRadius * 0.55f, Offset(eyeX + 1f, -size * 0.06f))
    drawCircle(Color.White, eyeRadius * 0.2f, Offset(eyeX + eyeRadius * 0.25f, -size * 0.1f))

    // Mouth
    val mouthState = if (fish.state == FishState.EATING) 0.8f else 0.3f
    drawArc(
        color = Color(0x88FF6688),
        startAngle = -20f,
        sweepAngle = 40f * mouthState,
        useCenter = false,
        topLeft = Offset(size * 0.33f, -size * 0.06f),
        size = Size(size * 0.1f, size * 0.12f),
        style = Stroke(width = 1.5f)
    )

    // Outline
    drawPath(
        bodyPath,
        color = bodyColor.copy(
            red = (bodyColor.red * 0.7f).coerceIn(0f, 1f),
            green = (bodyColor.green * 0.7f).coerceIn(0f, 1f),
            blue = (bodyColor.blue * 0.7f).coerceIn(0f, 1f),
            alpha = 0.4f
        ),
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawFishMarkings(fish: Fish, size: Float) {
    when (fish.type) {
        FishType.CLOWNFISH -> {
            listOf(-0.05f, 0.15f).forEach { xOff ->
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(xOff * size - 4f, -size * 0.28f),
                    size = Size(9f, size * 0.56f)
                )
            }
        }
        FishType.BETTA -> {
            repeat(4) { row ->
                repeat(6) { col ->
                    val sx = (col - 2.5f) * size * 0.12f
                    val sy = (row - 1.5f) * size * 0.1f
                    drawCircle(
                        color = fish.type.accentColor.copy(alpha = 0.2f + col * 0.05f),
                        radius = size * 0.055f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }
        FishType.ANGELFISH -> {
            listOf(-0.1f, 0.1f).forEach { xOff ->
                drawLine(
                    color = Color(0x55333333),
                    start = Offset(xOff * size, -size * 0.25f),
                    end = Offset(xOff * size, size * 0.25f),
                    strokeWidth = 3f
                )
            }
        }
        else -> {
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = size * 0.12f,
                center = Offset(-size * 0.1f, -size * 0.05f)
            )
        }
    }
}
