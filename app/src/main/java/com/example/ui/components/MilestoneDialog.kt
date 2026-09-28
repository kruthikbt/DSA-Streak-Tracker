package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float
)

@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }

    val particles = remember {
        val colors = listOf(
            FlamePrimary,
            TechCyan,
            SolvedGreen,
            Color(0xFFEAB308),
            Color(0xFFEC4899),
            Color(0xFF8B5CF6)
        )
        List(70) {
            ConfettiParticle(
                x = Random.nextFloat(),
                initialY = -Random.nextFloat() * 200f,
                speed = 600f + Random.nextFloat() * 800f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (p in particles) {
            val currentY = p.initialY + (p.speed * progress.value)
            if (currentY in 0f..canvasHeight) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x * canvasWidth, currentY),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}

@Composable
fun MilestoneDialog(
    milestoneDays: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milestone_confirm_button")
            ) {
                Text("Keep Crushing It! 🔥", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FlamePrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (milestoneDays >= 30) Icons.Default.EmojiEvents else Icons.Default.Whatshot,
                            contentDescription = "Milestone Icon",
                            tint = FlamePrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🔥 $milestoneDays-Day Streak Achieved!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = when (milestoneDays) {
                    7 -> "Incredible dedication! You've formed a solid daily DSA habit for 7 consecutive days. You are in the top 10% of consistent learners!"
                    30 -> "30 Days of Code! A whole month of unstoppable algorithm practice. Your problem-solving reflexes are transforming!"
                    50 -> "Halfway to 100! 50 consecutive days of mastery. You have the discipline of a top-tier software engineer."
                    100 -> "100-Day Centurion! Triple-digit consistency. Companies dream of candidates with this level of grit."
                    365 -> "365 DAYS! A complete YEAR of DSA. Legendary consistency! You are an absolute master."
                    else -> "You've unlocked the $milestoneDays-day streak badge! Incredible work, keep pushing forward."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
