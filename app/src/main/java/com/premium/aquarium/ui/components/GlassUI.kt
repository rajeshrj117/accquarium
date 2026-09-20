package com.premium.aquarium.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premium.aquarium.model.Fish
import com.premium.aquarium.model.FishType

@Composable
fun GlassTopBar(
    coins: Int,
    fishCount: Int,
    waterQuality: Float,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x22FFFFFF))
                .border(width = 1.dp, color = Color(0x44FFFFFF), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = coins.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83D\uDC20", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("$fishCount", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = getWaterColor(waterQuality), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${(waterQuality * 100).toInt()}%", color = getWaterColor(waterQuality), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                IconButton(onClick = onToggleMute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassBottomDock(
    onFeed: () -> Unit,
    onClean: () -> Unit,
    onShop: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(-1) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x33000000))
                .border(width = 1.dp, color = Color(0x66FFFFFF), shape = RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                DockButton("\uD83C\uDF5E", "Feed", selectedTab == 0) { selectedTab = 0; onFeed() }
                DockButton("\uD83E\uDDF9", "Clean", selectedTab == 1) { selectedTab = 1; onClean() }
                DockButton("\uD83C\uDFEA", "Shop", selectedTab == 2) { selectedTab = 2; onShop() }
                DockButton("\uD83D\uDCCA", "Stats", selectedTab == 3) { selectedTab = 3; onStats() }
            }
        }
    }
}

@Composable
private fun DockButton(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dock_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Color(0x55FFFFFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            if (selected) {
                Spacer(Modifier.height(2.dp))
                Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FishInfoBubble(fish: Fish, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC001428))
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onDismiss() }
            .padding(12.dp)
    ) {
        Column {
            Text(fish.name, color = fish.type.color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(fish.type.displayName, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            InfoBar("Happy", fish.happiness, Color(0xFF4CAF50))
            InfoBar("Hunger", fish.hunger, Color(0xFFFF9800))
            InfoBar("Health", fish.health, Color(0xFFF44336))
        }
    }
}

@Composable
private fun InfoBar(label: String, value: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, modifier = Modifier.width(56.dp))
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x44FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .background(color)
            )
        }
    }
}

private fun getWaterColor(quality: Float): Color = when {
    quality > 0.7f -> Color(0xFF4CAF50)
    quality > 0.4f -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

@Composable
fun ShopBottomSheet(
    coins: Int,
    onBuy: (FishType) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xEE001428))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color(0x44FFFFFF), Color(0x22FFFFFF), Color(0x44FFFFFF))),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x66FFFFFF))
                    .align(Alignment.CenterHorizontally)
                    .clickable { onDismiss() }
            )

            Spacer(Modifier.height(16.dp))

            Text("\uD83C\uDFEA Fish Shop", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Coins: $coins", color = Color(0xFFFFD700), fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            FishType.values().toList().chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { fishType ->
                        FishShopCard(
                            fishType = fishType,
                            canAfford = coins >= fishType.price,
                            onBuy = { onBuy(fishType) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FishShopCard(
    fishType: FishType,
    canAfford: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(fishType.color.copy(alpha = 0.15f))
            .border(1.dp, fishType.color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(enabled = canAfford) { onBuy() }
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(fishType.color, fishType.accentColor)))
            )
            Spacer(Modifier.height(8.dp))
            Text(fishType.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (fishType.price == 0) {
                Text("FREE", color = Color(0xFF4CAF50), fontSize = 11.sp)
            } else {
                Text("${fishType.price}", color = Color(0xFFFFD700), fontSize = 11.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (canAfford || fishType.price == 0) "Add" else "Need more",
                color = if (canAfford) Color.White else Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
