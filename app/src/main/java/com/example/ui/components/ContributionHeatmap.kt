package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ContributionHeatmap(
    dailyLogs: List<DailyLogEntity>,
    problems: List<ProblemEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Automatically scroll to the end (today's latest weeks)
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val today = remember { LocalDate.now() }
    val problemsCountByDate = remember(dailyLogs, problems) {
        val dateMap = mutableMapOf<String, Int>()
        dailyLogs.groupBy { it.date }.forEach { (date, logs) ->
            dateMap[date] = logs.sumOf { it.problemsSolved }
        }
        problems.filter { it.solved && !it.solvedDate.isNullOrBlank() }
            .groupBy { it.solvedDate!! }
            .forEach { (date, plist) ->
                val existing = dateMap[date] ?: 0
                dateMap[date] = maxOf(existing, plist.size)
            }
        dateMap
    }
    val minutesCountByDate = remember(dailyLogs) {
        dailyLogs.groupBy { it.date }
            .mapValues { (_, dayLogs) -> dayLogs.sumOf { it.minutes } }
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Generate 52 weeks of dates ending today (arranged Monday to Sunday)
    val weeks = remember(today) {
        val totalDays = 52 * 7
        val startDate = today.minusDays((totalDays - 1).toLong())
        // Adjust start to previous Monday
        val dayOfWeekValue = startDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val adjustedStart = startDate.minusDays((dayOfWeekValue - 1).toLong())

        val list = mutableListOf<List<LocalDate>>()
        var curr = adjustedStart
        while (!curr.isAfter(today)) {
            val week = mutableListOf<LocalDate>()
            for (i in 0 until 7) {
                week.add(curr)
                curr = curr.plusDays(1)
            }
            list.add(week)
        }
        list
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contribution_heatmap"),
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
                        text = "Practice Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last 12 months activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total solved count in past year
                val totalYearProblems = remember(problemsCountByDate, weeks) {
                    val allDates = weeks.flatten().map { it.toString() }.toSet()
                    problemsCountByDate.filterKeys { it in allDates }.values.sum()
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$totalYearProblems solved",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Grid Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Day of week labels (M, W, F)
                Column(
                    modifier = Modifier.padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(modifier = Modifier.height(18.dp)) // Space for month headers
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier.size(13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index % 2 == 0) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Weeks columns
                Column {
                    // Month Headers Row
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var lastMonth = -1
                        weeks.forEach { week ->
                            val firstDay = week.first()
                            val month = firstDay.monthValue
                            val showMonth = month != lastMonth && firstDay.dayOfMonth <= 7
                            if (showMonth) {
                                lastMonth = month
                            }
                            Box(modifier = Modifier.width(13.dp)) {
                                if (showMonth) {
                                    Text(
                                        text = firstDay.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 7 Days Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        weeks.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { date ->
                                    val isFuture = date.isAfter(today)
                                    val problems = problemsCountByDate[date.toString()] ?: 0
                                    val isSelected = selectedDate == date

                                    val cellColor = when {
                                        isFuture -> Color.Transparent
                                        problems == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        problems == 1 -> Color(0xFF047857) // Deep Emerald
                                        problems in 2..3 -> Color(0xFF059669) // Emerald 600
                                        problems in 4..5 -> Color(0xFF10B981) // Emerald 500
                                        else -> Color(0xFF34D399) // Bright Emerald 400
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .background(
                                                color = cellColor,
                                                shape = RoundedCornerShape(2.5.dp)
                                            )
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(2.5.dp)
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable(enabled = !isFuture) {
                                                selectedDate = if (selectedDate == date) null else date
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive selected info
                if (selectedDate != null) {
                    val count = problemsCountByDate[selectedDate.toString()] ?: 0
                    val mins = minutesCountByDate[selectedDate.toString()] ?: 0
                    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    Text(
                        text = "${selectedDate!!.format(formatter)}: $count solved ($mins m)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Tap cell to view details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Less",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF047857), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF059669), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF34D399), RoundedCornerShape(2.dp)))
                    Text(
                        text = "More",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
