package com.premium.aquarium.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.premium.aquarium.model.Decoration
import com.premium.aquarium.model.DecorationType
import kotlin.math.*

@Composable
fun DecorationsLayer(
    decorations: List<Decoration>,
    tankHeight: Float,
    editMode: Boolean,
    onDecorationMoved: (Int, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val globalSwayPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "sway_phase"
    )

    Canvas(
        modifier = modifier.then(
            if (editMode) {
                Modifier.pointerInput(decorations) {
                    detectDragGestures { change, dragAmount ->
                        val nearestDec = decorations.minByOrNull { dec ->
                            val dx = change.position.x - dec.x
                            val dy = change.position.y - dec.y
                            sqrt(dx * dx + dy * dy)
                        }
                        nearestDec?.let { dec ->
                            val dist = sqrt(
                                (change.position.x - dec.x).pow(2) + (change.position.y - dec.y).pow(2)
                            )
                            if (dist < 80f) {
                                onDecorationMoved(dec.id, dec.x + dragAmount.x, dec.y + dragAmount.y)
                            }
                        }
                    }
                }
            } else Modifier
        )
    ) {
        val sorted = decorations.sortedBy { dec ->
            when (dec.type) {
                DecorationType.ROCK_LARGE, DecorationType.ROCK_SMALL -> 0
                DecorationType.SEAWEED_TALL, DecorationType.SEAWEED_SHORT -> 1
                DecorationType.CORAL_RED, DecorationType.CORAL_PINK -> 2
                DecorationType.ANEMONE -> 3
                else -> 4
            }
        }

        sorted.forEach { decoration ->
            val sway = if (decoration.isSwaying) {
                sin(globalSwayPhase + decoration.swayPhase) * decoration.swayAmplitude
            } else 0f

            withTransform({ translate(decoration.x, decoration.y) }) {
                drawDecoration(decoration, sway)
            }

            if (editMode) {
                drawCircle(
                    color = Color(0x4400FFFF),
                    radius = 40f,
                    center = Offset(decoration.x, decoration.y),
                    style = Stroke(2f)
                )
            }
        }
    }
}

private fun DrawScope.drawDecoration(decoration: Decoration, sway: Float) {
    when (decoration.type) {
        DecorationType.CORAL_RED, DecorationType.CORAL_PINK -> drawCoral(
            color = if (decoration.type == DecorationType.CORAL_RED) Color(0xFFFF4444) else Color(0xFFFF88CC),
            sway = sway
        )
        DecorationType.SEAWEED_TALL -> drawSeaweed(height = 140f, color = Color(0xFF2E8B22), sway = sway)
        DecorationType.SEAWEED_SHORT -> drawSeaweed(height = 80f, color = Color(0xFF4CAF50), sway = sway)
        DecorationType.ANEMONE -> drawAnemone(sway = sway)
        DecorationType.ROCK_LARGE -> drawRock(size = 100f)
        DecorationType.ROCK_SMALL -> drawRock(size = 60f)
        DecorationType.TREASURE_CHEST -> drawTreasureChest()
        DecorationType.CASTLE -> drawCastle()
        DecorationType.SHELL -> drawShell()
        DecorationType.STARFISH -> drawStarfish()
        DecorationType.ANCHOR -> drawAnchor()
    }
}

private fun DrawScope.drawCoral(color: Color, sway: Float) {
    drawRect(color.copy(alpha = 0.8f), topLeft = Offset(-8f + sway, 0f), size = Size(16f, 20f))

    val branchData = listOf(Triple(-20f, -40f, -30f), Triple(0f, -60f, 0f), Triple(20f, -40f, 30f))
    branchData.forEach { (bx, by, curve) ->
        val path = Path().apply {
            moveTo(sway * 0.5f, 0f)
            cubicTo(curve * 0.3f + sway * 0.7f, by * 0.4f, bx + sway * 0.9f, by * 0.7f, bx + sway, by)
        }
        drawPath(path, color.copy(alpha = 0.9f), style = Stroke(width = 8f))
        drawCircle(color, 10f, Offset(bx + sway, by))
        drawCircle(color.copy(alpha = 0.5f), 6f, Offset(bx + sway - 3f, by - 8f))
    }
    drawLine(Color.White.copy(alpha = 0.3f), Offset(sway * 0.8f, -5f), Offset(sway, -55f), strokeWidth = 2f)
}

private fun DrawScope.drawSeaweed(height: Float, color: Color, sway: Float) {
    val segments = 8
    val segHeight = height / segments

    val path = Path().apply {
        moveTo(0f, 0f)
        repeat(segments) { i ->
            val progress = i.toFloat() / segments
            val swayAmount = sway * progress * 2f
            val x = sin(progress * PI.toFloat() * 2f) * 12f + swayAmount
            val y = -(i + 1) * segHeight
            if (i == 0) {
                lineTo(x, y)
            } else {
                val prevX = sin((i - 1).toFloat() / segments * PI.toFloat() * 2f) * 12f +
                    sway * ((i - 1).toFloat() / segments) * 2f
                val prevY = -i * segHeight
                cubicTo(prevX + swayAmount * 0.3f, prevY - segHeight * 0.3f, x - swayAmount * 0.3f, y + segHeight * 0.3f, x, y)
            }
        }
    }

    drawPath(
        path,
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.4f), color, color.copy(alpha = 0.7f)),
            startY = -height, endY = 0f
        ),
        style = Stroke(width = 7f, cap = StrokeCap.Round)
    )

    repeat(segments / 2) { i ->
        val progress = (i * 2 + 1).toFloat() / segments
        val lx = sin(progress * PI.toFloat() * 2f) * 12f + sway * progress * 2f
        val ly = -(i * 2 + 1) * segHeight
        val side = if (i % 2 == 0) 1f else -1f

        val leafPath = Path().apply {
            moveTo(lx, ly)
            cubicTo(lx + side * 20f, ly - 10f, lx + side * 25f, ly - 20f, lx + side * 15f + sway * 0.5f, ly - 30f)
            cubicTo(lx + side * 5f, ly - 20f, lx + side * 3f, ly - 10f, lx, ly)
        }
        drawPath(leafPath, color.copy(alpha = 0.7f))
    }
}

private fun DrawScope.drawAnemone(sway: Float) {
    val baseColor = Color(0xFFFF6B9D)
    val tipColor = Color(0xFFFFB347)

    drawOval(color = Color(0xFFCC5577), topLeft = Offset(-25f, -10f), size = Size(50f, 20f))

    repeat(12) { i ->
        val angle = (i * 30f) * (PI / 180f)
        val baseRadius = 18f
        val tentacleLen = 60f + sin(i * 1.3f) * 15f
        val swayFactor = cos(angle.toFloat()) * sway

        val bx = (cos(angle) * baseRadius).toFloat()
        val by = (sin(angle) * baseRadius * 0.4f - 5f).toFloat()

        val path = Path().apply {
            moveTo(bx, by)
            cubicTo(
                bx + swayFactor * 0.3f, by - tentacleLen * 0.4f,
                bx + swayFactor * 0.7f, by - tentacleLen * 0.7f,
                bx + swayFactor, by - tentacleLen
            )
        }
        drawPath(path, baseColor.copy(alpha = 0.8f), style = Stroke(4f, cap = StrokeCap.Round))
        drawCircle(tipColor, 5f, Offset(bx + swayFactor, by - tentacleLen))
    }
}

private fun DrawScope.drawRock(size: Float) {
    val rockPath = Path().apply {
        moveTo(0f, 0f)
        cubicTo(-size * 0.5f, -size * 0.1f, -size * 0.55f, -size * 0.45f, -size * 0.3f, -size * 0.5f)
        cubicTo(-size * 0.1f, -size * 0.65f, size * 0.15f, -size * 0.6f, size * 0.35f, -size * 0.5f)
        cubicTo(size * 0.55f, -size * 0.35f, size * 0.52f, -size * 0.1f, size * 0.45f, 0f)
        close()
    }

    drawPath(rockPath, Color(0x44000000), style = Fill)
    drawPath(
        rockPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF9E9E9E), Color(0xFF616161), Color(0xFF424242)),
            start = Offset(-size * 0.3f, -size * 0.6f),
            end = Offset(size * 0.3f, 0f)
        )
    )
    drawPath(
        Path().apply {
            moveTo(-size * 0.2f, -size * 0.45f)
            cubicTo(-size * 0.1f, -size * 0.6f, size * 0.1f, -size * 0.55f, size * 0.15f, -size * 0.42f)
        },
        Color.White.copy(alpha = 0.3f),
        style = Stroke(3f)
    )

    listOf(Offset(-size * 0.2f, -size * 0.2f), Offset(size * 0.1f, -size * 0.35f)).forEach { pos ->
        drawOval(Color(0x774CAF50), topLeft = Offset(pos.x - 10f, pos.y - 6f), size = Size(20f, 12f))
    }
}

private fun DrawScope.drawTreasureChest() {
    val w = 90f
    val h = 70f
    val hw = w / 2

    drawOval(Color(0x44000000), topLeft = Offset(-hw, 2f), size = Size(w, 16f))
    drawRoundRect(
        color = Color(0xFF8B4513),
        topLeft = Offset(-hw, -h * 0.5f),
        size = Size(w, h * 0.5f),
        cornerRadius = CornerRadius(4f)
    )

    listOf(-hw + w * 0.3f, hw - w * 0.3f - 6f).forEach { bx ->
        drawRect(Color(0xFFDAA520), topLeft = Offset(bx, -h * 0.5f), size = Size(6f, h * 0.5f))
    }

    val lidPath = Path().apply {
        moveTo(-hw, -h * 0.5f)
        cubicTo(-hw, -h * 0.85f, hw, -h * 0.85f, hw, -h * 0.5f)
        close()
    }
    drawPath(lidPath, Color(0xFF6B3410))
    drawPath(
        Path().apply {
            moveTo(-hw, -h * 0.5f)
            cubicTo(-hw, -h * 0.85f, hw, -h * 0.85f, hw, -h * 0.5f)
        },
        Color(0xFFDAA520),
        style = Stroke(4f)
    )

    drawRect(Color(0xFFDAA520), topLeft = Offset(-8f, -h * 0.58f), size = Size(16f, 14f))
    drawArc(
        Color(0xFFDAA520), startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(-6f, -h * 0.68f), size = Size(12f, 12f), style = Stroke(3f)
    )

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xAAFFD700), Color.Transparent),
            startY = -h * 0.52f, endY = -h * 0.45f
        ),
        topLeft = Offset(-hw + 10f, -h * 0.52f),
        size = Size(w - 20f, 8f)
    )

    repeat(3) { i ->
        drawLine(
            Color(0x336B3410),
            Offset(-hw + 5f, -h * 0.5f + i * 8f + 8f),
            Offset(hw - 5f, -h * 0.5f + i * 8f + 8f),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawCastle() {
    val color = Color(0xFF90A4AE)
    val darkColor = Color(0xFF546E7A)

    drawRect(darkColor, topLeft = Offset(-20f, -130f), size = Size(40f, 130f))
    drawRect(color, topLeft = Offset(-18f, -128f), size = Size(36f, 126f))

    listOf(-45f, 25f).forEach { tx ->
        drawRect(darkColor, topLeft = Offset(tx, -100f), size = Size(30f, 100f))
        drawRect(color, topLeft = Offset(tx + 2f, -98f), size = Size(26f, 96f))
        repeat(3) { i -> drawRect(darkColor, topLeft = Offset(tx + i * 9f, -108f), size = Size(6f, 10f)) }
    }

    repeat(4) { i -> drawRect(darkColor, topLeft = Offset(-22f + i * 12f, -138f), size = Size(8f, 12f)) }

    val gatePath = Path().apply {
        moveTo(-10f, 0f)
        lineTo(-10f, -25f)
        arcTo(
            rect = Rect(-10f, -35f, 10f, -25f),
            startAngleDegrees = 180f, sweepAngleDegrees = -180f, forceMoveTo = false
        )
        lineTo(10f, 0f)
    }
    drawPath(gatePath, darkColor)

    listOf(Offset(-35f, -70f), Offset(35f, -70f), Offset(0f, -90f)).forEach { pos ->
        drawRect(darkColor, topLeft = Offset(pos.x - 5f, pos.y - 8f), size = Size(10f, 14f))
        drawArc(
            darkColor, 180f, -180f, false,
            topLeft = Offset(pos.x - 5f, pos.y - 12f), size = Size(10f, 8f)
        )
    }

    drawRect(Color(0x554CAF50), topLeft = Offset(-20f, -30f), size = Size(40f, 20f))
}

private fun DrawScope.drawShell() {
    val shellPath = Path().apply {
        moveTo(0f, 0f)
        cubicTo(-30f, -10f, -35f, -35f, -15f, -45f)
        cubicTo(5f, -55f, 30f, -45f, 35f, -25f)
        cubicTo(40f, -5f, 20f, 5f, 0f, 0f)
    }

    drawPath(
        shellPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFF8BBD0), Color(0xFFE91E63), Color(0xFFF8BBD0)),
            start = Offset(-35f, -50f), end = Offset(35f, 0f)
        )
    )

    repeat(5) { i ->
        val t = i / 5f
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(
                -10f + t * 20f, -15f - t * 20f,
                -20f + t * 30f, -30f - t * 10f,
                -25f + t * 45f, -40f + t * 30f
            )
        }
        drawPath(path, Color(0xFFAD1457).copy(alpha = 0.4f), style = Stroke(1.5f))
    }

    drawCircle(Color.White.copy(alpha = 0.4f), 6f, Offset(-10f, -30f))
}

private fun DrawScope.drawStarfish() {
    val path = Path()
    val outerR = 28f
    val innerR = 12f
    val arms = 5

    repeat(arms) { i ->
        val outerAngle = (-PI / 2 + i * 2 * PI / arms).toFloat()
        val innerAngle = (-PI / 2 + (i + 0.5f) * 2 * PI / arms).toFloat()

        if (i == 0) {
            path.moveTo(cos(outerAngle) * outerR, sin(outerAngle) * outerR)
        } else {
            path.lineTo(cos(outerAngle) * outerR, sin(outerAngle) * outerR)
        }
        path.lineTo(cos(innerAngle) * innerR, sin(innerAngle) * innerR)
    }
    path.close()

    drawPath(
        path,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF8F00), Color(0xFFE65100)),
            center = Offset.Zero, radius = outerR
        )
    )

    repeat(arms) { i ->
        val angle = (-PI / 2 + i * 2 * PI / arms).toFloat()
        repeat(3) { j ->
            val r = innerR + j * (outerR - innerR) / 3.5f
            drawCircle(Color(0xFFFF6F00).copy(alpha = 0.6f), 3f, Offset(cos(angle) * r, sin(angle) * r))
        }
    }

    drawCircle(Color(0xFFFFB300), 8f)
}

private fun DrawScope.drawAnchor() {
    val color = Color(0xFF455A64)
    val strokeW = 8f

    drawCircle(color, 14f, Offset(0f, -100f), style = Stroke(strokeW))
    drawLine(color, Offset(0f, -86f), Offset(0f, -10f), strokeW)
    drawLine(color, Offset(-30f, -70f), Offset(30f, -70f), strokeW)

    val leftHook = Path().apply {
        moveTo(0f, -10f)
        cubicTo(-10f, 0f, -30f, 0f, -30f, -20f)
    }
    drawPath(leftHook, color, style = Stroke(strokeW, cap = StrokeCap.Round))

    val rightHook = Path().apply {
        moveTo(0f, -10f)
        cubicTo(10f, 0f, 30f, 0f, 30f, -20f)
    }
    drawPath(rightHook, color, style = Stroke(strokeW, cap = StrokeCap.Round))

    drawLine(Color(0xFFB0BEC5), Offset(4f, -85f), Offset(4f, -15f), 2f)

    repeat(3) { i ->
        drawOval(
            color.copy(alpha = 0.7f),
            topLeft = Offset(-8f, -118f - i * 12f),
            size = Size(16f, 10f),
            style = Stroke(3f)
        )
    }
}
