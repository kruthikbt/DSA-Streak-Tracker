package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntity
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.SolvedGreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyBarChart(
    dailyLogs: List<DailyLogEntity>,
    dailyGoal: Int = 2,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val problemsCountByDate = remember(dailyLogs) {
        dailyLogs.groupBy { it.date }
            .mapValues { (_, logs) -> logs.sumOf { it.problemsSolved } }
    }

    // Past 7 days (today-6 to today)
    val past7Days = remember(today) {
        (6 downTo 0).map { offset ->
            today.minusDays(offset.toLong())
        }
    }

    val dailyCounts = remember(past7Days, problemsCountByDate) {
        past7Days.map { date ->
            problemsCountByDate[date.toString()] ?: 0
        }
    }

    val maxCount = remember(dailyCounts, dailyGoal) {
        maxOf(dailyCounts.maxOrNull() ?: 1, dailyGoal + 1, 4)
    }

    val totalWeekProblems = remember(dailyCounts) {
        dailyCounts.sum()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last 7 Days Practice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Daily goal: $dailyGoal problems",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FlamePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$totalWeekProblems problems",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FlamePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                past7Days.forEachIndexed { index, date ->
                    val count = dailyCounts[index]
                    val isToday = date == today
                    val meetsGoal = count >= dailyGoal

                    val barFraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.06f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Count label above bar
                        Text(
                            text = if (count > 0) "$count" else "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (meetsGoal) SolvedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(barFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    when {
                                        count == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                        meetsGoal -> SolvedGreen
                                        else -> FlamePrimary
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day label (e.g., "Mon", "Tue")
                        Text(
                            text = if (isToday) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) FlamePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
