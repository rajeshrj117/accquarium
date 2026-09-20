package com.premium.aquarium.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.premium.aquarium.engine.*
import com.premium.aquarium.model.*
import com.premium.aquarium.sound.SoundManager
import com.premium.aquarium.ui.components.*
import com.premium.aquarium.utils.MathUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun AquariumScreen() {
    val context = LocalContext.current

    var tankWidth by remember { mutableStateOf(0f) }
    var tankHeight by remember { mutableStateOf(0f) }

    val fishList = remember { mutableStateListOf<Fish>() }
    val bubbles = remember { mutableStateListOf<Bubble>() }
    val foodPellets = remember { mutableStateListOf<FoodPellet>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val decorations = remember { mutableStateListOf<Decoration>() }

    var coins by remember { mutableStateOf(500) }
    var waterQuality by remember { mutableStateOf(0.9f) }
    var isMuted by remember { mutableStateOf(false) }
    var selectedFish by remember { mutableStateOf<Fish?>(null) }
    var showShop by remember { mutableStateOf(false) }
    var feedMode by remember { mutableStateOf(false) }

    var fingerX by remember { mutableStateOf<Float?>(null) }
    var fingerY by remember { mutableStateOf<Float?>(null) }

    var nextId by remember { mutableStateOf(1000) }
    fun id(): Int = nextId++

    val soundManager = remember { SoundManager(context) }
    val physicsEngine = remember(tankWidth, tankHeight) {
        if (tankWidth > 0 && tankHeight > 0) PhysicsEngine(tankWidth, tankHeight) else null
    }
    val fishAI = remember(tankWidth, tankHeight) {
        if (tankWidth > 0 && tankHeight > 0) FishAI(tankWidth, tankHeight) else null
    }
    val decorManager = remember(tankWidth, tankHeight) {
        if (tankWidth > 0 && tankHeight > 0) DecorationManager(tankWidth, tankHeight) else null
    }

    // Initialize starter fish + decorations once tank is sized
    LaunchedEffect(tankWidth, tankHeight) {
        if (tankWidth > 0 && tankHeight > 0 && fishList.isEmpty()) {
            fishList.add(Fish(id = id(), type = FishType.CLOWNFISH, x = tankWidth * 0.3f, y = tankHeight * 0.5f))
            fishList.add(Fish(id = id(), type = FishType.GOLDFISH, x = tankWidth * 0.7f, y = tankHeight * 0.4f))
            decorManager?.createDefault()?.forEach { decorations.add(it) }
        }
    }

    // Game loop
    LaunchedEffect(tankWidth, tankHeight) {
        if (tankWidth == 0f || tankHeight == 0f) return@LaunchedEffect
        val ai = fishAI ?: return@LaunchedEffect
        val physics = physicsEngine ?: return@LaunchedEffect

        var lastTime = System.currentTimeMillis()
        var bubbleTimer = 0f
        var coinTimer = 0f

        while (isActive) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = ((currentTime - lastTime) / 1000f).coerceAtMost(0.05f)
            lastTime = currentTime

            fishList.forEach { fish ->
                ai.update(fish, fishList.toList(), foodPellets.toList(), deltaTime, fingerX, fingerY)
            }

            // Detect newly eaten food this frame -> spawn effects, then clear the flag's visuals
            val justEaten = foodPellets.filter { it.eaten && it.alpha > 0.95f }
            justEaten.forEach { food ->
                val nearest = fishList.minByOrNull { MathUtils.distance(it.x, it.y, food.x, food.y) }
                nearest?.let { fish ->
                    particles.addAll(ParticleFactory.createEatSparkles(fish.x, fish.y, fish.type.color) { id() })
                    particles.addAll(ParticleFactory.createHappinessHearts(fish.x, fish.y - 30f) { id() })
                }
                soundManager.playEat()
                coins += 5
            }

            physics.updateBubbles(bubbles, deltaTime)
            physics.updateFood(foodPellets, deltaTime)
            physics.updateParticles(particles, deltaTime)

            bubbleTimer += deltaTime
            if (bubbleTimer > 0.8f + Random.nextFloat() * 1.5f) {
                bubbleTimer = 0f
                if (bubbles.size < 30) {
                    physics.spawnBubble(bubbles) { id() }
                    soundManager.playBubble()
                }
            }

            coinTimer += deltaTime
            if (coinTimer >= 5f) {
                coinTimer = 0f
                coins += fishList.size * 2
            }

            waterQuality = maxOf(0f, waterQuality - deltaTime * 0.001f)

            delay(16L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                tankWidth = size.width.toFloat()
                tankHeight = size.height.toFloat()
            }
            .pointerInput(feedMode) {
                detectTapGestures(
                    onTap = { offset ->
                        val x = offset.x
                        val y = offset.y

                        if (feedMode) {
                            physicsEngine?.spawnFood(foodPellets, x, y) { nextId++ }
                            particles.addAll(ParticleFactory.createRipple(x, y) { nextId++ })
                            particles.addAll(ParticleFactory.createSplash(x, y) { nextId++ })
                            soundManager.playSplash()

                            fishList.forEach { fish ->
                                if (MathUtils.distance(fish.x, fish.y, x, y) < 300f) {
                                    fish.state = FishState.EATING
                                    fish.targetX = x + Random.nextFloat() * 60f - 30f
                                    fish.targetY = y
                                }
                            }
                        } else {
                            val tapped = fishList.firstOrNull { fish ->
                                MathUtils.distance(fish.x, fish.y, x, y) < fish.type.size * 0.7f
                            }
                            selectedFish = if (tapped == selectedFish) null else tapped

                            if (tapped == null) {
                                particles.addAll(ParticleFactory.createRipple(x, y) { nextId++ })
                                fingerX = x
                                fingerY = y
                            }
                        }
                    },
                    onPress = { offset ->
                        fingerX = offset.x
                        fingerY = offset.y
                        tryAwaitRelease()
                        fingerX = null
                        fingerY = null
                    }
                )
            }
    ) {
        WaterBackground(modifier = Modifier.fillMaxSize())

        DecorationsLayer(
            decorations = decorations,
            tankHeight = tankHeight,
            editMode = false,
            onDecorationMoved = { decId, nx, ny ->
                val idx = decorations.indexOfFirst { it.id == decId }
                if (idx >= 0) decorations[idx] = decorations[idx].copy(x = nx, y = ny)
            },
            modifier = Modifier.fillMaxSize()
        )

        ParticleLayer(
            bubbles = bubbles,
            particles = particles.filter { it.type == ParticleType.DUST },
            food = foodPellets,
            modifier = Modifier.fillMaxSize()
        )

        FishLayer(fish = fishList, modifier = Modifier.fillMaxSize())

        ParticleLayer(
            bubbles = emptyList(),
            particles = particles.filter { it.type != ParticleType.DUST },
            food = emptyList(),
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            GlassTopBar(
                coins = coins,
                fishCount = fishList.size,
                waterQuality = waterQuality,
                isMuted = isMuted,
                onToggleMute = {
                    isMuted = !isMuted
                    soundManager.setMuted(isMuted)
                }
            )

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = selectedFish != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                selectedFish?.let { fish ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        FishInfoBubble(fish = fish, onDismiss = { selectedFish = null })
                    }
                }
            }

            GlassBottomDock(
                onFeed = { feedMode = !feedMode },
                onClean = {
                    if (coins >= 50) {
                        waterQuality = minOf(1f, waterQuality + 0.3f)
                        coins -= 50
                        soundManager.playChime()
                    }
                },
                onShop = { showShop = true },
                onStats = { selectedFish = fishList.firstOrNull() }
            )
        }

        AnimatedVisibility(
            visible = feedMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC003366))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Tap to drop food",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        AnimatedVisibility(
            visible = showShop,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ShopBottomSheet(
                coins = coins,
                onBuy = { fishType ->
                    if (fishList.size < 10 && coins >= fishType.price) {
                        coins -= fishType.price
                        fishList.add(
                            Fish(
                                id = nextId++,
                                type = fishType,
                                x = tankWidth * (0.2f + Random.nextFloat() * 0.6f),
                                y = tankHeight * (0.3f + Random.nextFloat() * 0.4f)
                            )
                        )
                        particles.addAll(ParticleFactory.createHappinessHearts(tankWidth / 2, tankHeight / 2) { nextId++ })
                        soundManager.playChime()
                        showShop = false
                    }
                },
                onDismiss = { showShop = false }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }
}
