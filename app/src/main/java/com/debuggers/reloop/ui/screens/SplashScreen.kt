package com.debuggers.reloop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debuggers.reloop.data.repository.MaterialRepository
import com.debuggers.reloop.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ReLoopSplashScreen(
    onSplashFinished: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Connecting Regional Industrial Hubs…") }

    // Entrance Scale & Alpha Animation
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.75f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "SplashScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "SplashAlpha"
    )

    // Continuous smooth rotation for the circular economy loop
    val infiniteTransition = rememberInfiniteTransition(label = "LoopRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IconRotation"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        // Trigger background sync while animating
        MaterialRepository.refreshAll()

        delay(550)
        statusText = "Verifying Material Passports & Lots…"
        delay(600)
        statusText = "Ready • Entering Marketplace"
        delay(350)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandPrimaryDark, // Deep obsidian petrol #08333A
                        BrandPrimary,     // Executive industrial petrol #0D4C55
                        BrandPrimaryDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale)
                .alpha(alpha)
        ) {
            // Animated Circular ReLoop Emblem
            Box(contentAlignment = Alignment.Center) {
                // Outer subtle ambient glow ring
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(BrandLime.copy(alpha = 0.12f), CircleShape)
                )

                // Middle container
                Surface(
                    shape = CircleShape,
                    color = BrandPrimaryDark,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "ReLoop Circular Exchange",
                            tint = BrandLime,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(rotation)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Main Title
            Text(
                text = "ReLoop",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(8.dp))

            // Category Pill Tag
            Surface(
                color = BrandLime.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "INDUSTRIAL MATERIAL EXCHANGE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandLime,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Professional Subtitle
            Text(
                text = "Connecting By-Products & Secondary Feedstocks with Circular Supply Chains",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Loading & Status Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp, start = 40.dp, end = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(3.dp),
                color = BrandLime,
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            Text(
                text = statusText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
