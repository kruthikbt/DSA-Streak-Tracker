package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreakInfo
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.FlamePrimaryLight
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan

@Composable
fun StreakCard(
    streakInfo: StreakInfo,
    onLogClick: () -> Unit,
    onUseFreezeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (streakInfo.currentStreak > 0) FlamePrimary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Status Chip & Freeze Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today status
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (streakInfo.isTodayCompleted) SolvedGreen.copy(alpha = 0.15f)
                    else MediumYellow.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (streakInfo.isTodayCompleted) SolvedGreen else MediumYellow
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (streakInfo.isTodayCompleted) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                            contentDescription = "Status",
                            tint = if (streakInfo.isTodayCompleted) SolvedGreen else MediumYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (streakInfo.isTodayCompleted) "Today: Done ✅" else "Today: Pending ⏳",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (streakInfo.isTodayCompleted) SolvedGreen else MediumYellow
                        )
                    }
                }

                // Freeze status
                Surface(
                    shape = RoundedCornerShape(50),
                    color = TechCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "Streak Freeze",
                            tint = TechCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Freezes: ${streakInfo.freezesRemainingThisMonth}/2",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TechCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Streak Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        FlamePrimary.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Flame Streak Icon",
                            tint = FlamePrimary,
                            modifier = Modifier
                                .size(44.dp)
                                .scale(if (streakInfo.currentStreak > 0) flameScale else 1f)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${streakInfo.currentStreak}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (streakInfo.currentStreak == 1) "day streak" else "days streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlamePrimaryLight,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Text(
                            text = "Best Streak: ${streakInfo.longestStreak} days 🏆",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Streak Warning Banner if Today is Pending
            if (streakInfo.todayWarning) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MediumYellow.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MediumYellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ Keep Your Streak Alive!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MediumYellow
                            )
                            Text(
                                text = "Log at least 1 problem or 30 min today before midnight.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row {
                            if (streakInfo.freezesRemainingThisMonth > 0) {
                                OutlinedButton(
                                    onClick = onUseFreezeClick,
                                    border = BorderStroke(1.dp, TechCyan),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("use_freeze_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AcUnit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Freeze", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Button(
                                onClick = onLogClick,
                                colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("quick_log_today_button")
                            ) {
                                Text("Log Now", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
