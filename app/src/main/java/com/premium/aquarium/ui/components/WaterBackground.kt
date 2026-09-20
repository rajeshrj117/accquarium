package com.premium.aquarium.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*

@Composable
fun WaterBackground(modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition(label = "water")

    val causticOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "caustic"
    )

    val lightRayAlpha by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lightray"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF001B3A),
                    0.3f to Color(0xFF003366),
                    0.6f to Color(0xFF004080),
                    0.85f to Color(0xFF003355),
                    1.0f to Color(0xFF001428)
                )
            ),
            size = size
        )

        drawDepthLayers()
        drawLightRays(lightRayAlpha)
        drawCaustics(causticOffset, lightRayAlpha * 0.7f)
        drawSurfaceShimmer(waveOffset)
        drawBottom()
        drawGlassEdges()
    }
}

private fun DrawScope.drawDepthLayers() {
    val layerCount = 5
    repeat(layerCount) { i ->
        val y = size.height * i / layerCount
        val alpha = 0.04f - i * 0.006f
        drawRect(
            color = Color(0xFF004488).copy(alpha = alpha.coerceAtLeast(0f)),
            topLeft = Offset(0f, y),
            size = Size(size.width, size.height / layerCount)
        )
    }
}

private fun DrawScope.drawLightRays(alpha: Float) {
    val rayCount = 7
    val topWidth = 80f

    repeat(rayCount) { i ->
        val startX = size.width * (i + 1) / (rayCount + 1)
        val endX = startX + (size.width * 0.15f * (i % 3 - 1))

        val path = Path().apply {
            moveTo(startX - topWidth / 2, 0f)
            lineTo(startX + topWidth / 2, 0f)
            lineTo(endX + 120f, size.height)
            lineTo(endX - 120f, size.height)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8F4FD).copy(alpha = alpha),
                    Color(0xFF90CAF9).copy(alpha = alpha * 0.5f),
                    Color.Transparent
                )
            )
        )
    }
}

private fun DrawScope.drawCaustics(offset: Float, alpha: Float) {
    val cellSize = 60f
    val cols = (size.width / cellSize).toInt() + 2
    val rows = (size.height / cellSize).toInt() + 2

    repeat(rows) { row ->
        repeat(cols) { col ->
            val x = col * cellSize + (offset * cellSize * 0.5f) % cellSize
            val y = row * cellSize + (offset * cellSize * 0.3f) % cellSize

            val phase = (col * 0.7f + row * 0.5f + offset * 3f) % (2f * PI.toFloat())
            val wave = sin(phase) * 0.5f + 0.5f

            if (wave > 0.6f) {
                drawCircle(
                    color = Color(0xFFB3E5FC).copy(alpha = alpha * wave * 0.4f),
                    radius = cellSize * 0.3f * wave,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private fun DrawScope.drawSurfaceShimmer(waveOffset: Float) {
    val shimmerHeight = 40f
    val waveCount = 8

    val path = Path().apply {
        moveTo(0f, shimmerHeight)
        repeat(waveCount) { i ->
            val x2 = size.width * (2 * i + 1) / (waveCount * 2)
            val x3 = size.width * (2 * i + 2) / (waveCount * 2)
            val waveY = shimmerHeight + sin(waveOffset + i * 0.8f) * 8f
            quadraticBezierTo(x2, waveY - 12f, x3, shimmerHeight)
        }
        lineTo(size.width, 0f)
        lineTo(0f, 0f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x33FFFFFF), Color(0x11AADDFF), Color.Transparent),
            startY = 0f,
            endY = shimmerHeight
        )
    )
}

private fun DrawScope.drawBottom() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x22C4A86A), Color(0x55A0894A)),
            startY = size.height * 0.85f,
            endY = size.height
        ),
        size = size
    )

    val pebbles = listOf(
        Offset(size.width * 0.1f, size.height * 0.93f) to 12f,
        Offset(size.width * 0.25f, size.height * 0.95f) to 8f,
        Offset(size.width * 0.45f, size.height * 0.92f) to 15f,
        Offset(size.width * 0.65f, size.height * 0.94f) to 10f,
        Offset(size.width * 0.82f, size.height * 0.93f) to 13f
    )

    pebbles.forEach { (pos, r) ->
        drawCircle(color = Color(0xFF8B7355).copy(alpha = 0.5f), radius = r, center = pos)
        drawCircle(
            color = Color(0xFFBBA97A).copy(alpha = 0.3f),
            radius = r * 0.6f,
            center = Offset(pos.x - r * 0.2f, pos.y - r * 0.2f)
        )
    }
}

private fun DrawScope.drawGlassEdges() {
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0x33FFFFFF), Color.Transparent),
            startX = 0f, endX = 30f
        ),
        size = size
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color(0x22FFFFFF)),
            startX = size.width - 30f, endX = size.width
        ),
        size = size
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x55FFFFFF), Color.Transparent),
            startY = 0f, endY = 8f
        ),
        size = size
    )
    drawRect(
        color = Color(0x44AADDFF),
        style = Stroke(width = 2f)
    )
}
