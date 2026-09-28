package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.ProblemEntity
import com.example.data.model.RevisionProblemItem
import com.example.data.model.StreakInfo
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import com.example.ui.components.ContributionHeatmap
import com.example.ui.components.StreakCard
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.AlgorithmViolet
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan

@Composable
fun DashboardScreen(
    streakInfo: StreakInfo,
    topics: List<TopicEntity>,
    problems: List<ProblemEntity>,
    dailyLogs: List<DailyLogEntity>,
    settings: UserSettingsEntity,
    revisionItems: List<RevisionProblemItem> = emptyList(),
    onNavigateToTopic: (Int) -> Unit,
    onNavigateToDailyLog: () -> Unit,
    onNavigateToRevision: () -> Unit = {},
    onUseFreeze: () -> Unit,
    onDailyPlanClick: ((TopicEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Find current active topic (first IN_PROGRESS topic, or first NOT_STARTED topic)
    val currentTopic = remember(topics) {
        topics.find { it.status == TopicEntity.STATUS_IN_PROGRESS }
            ?: topics.find { it.status == TopicEntity.STATUS_NOT_STARTED }
            ?: topics.firstOrNull()
    }

    val completedTopicsCount = remember(topics) {
        topics.count { it.status == TopicEntity.STATUS_COMPLETED }
    }
    val roadmapProgress = if (topics.isNotEmpty()) {
        completedTopicsCount.toFloat() / topics.size.toFloat()
    } else 0f

    val totalSolvedProblems = remember(problems) {
        problems.count { it.solved }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Streak Card
            StreakCard(
                streakInfo = streakInfo,
                onLogClick = onNavigateToDailyLog,
                onUseFreezeClick = onUseFreeze
            )

            // 2. Today's Focus Card
            currentTopic?.let { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todays_focus_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = TechCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = TechCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Today's Focus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = TechCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "#%02d".format(topic.order),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TechCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Target: Solve at least ${settings.dailyGoal} problems in this topic to build deep pattern recognition.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onDailyPlanClick?.invoke(topic) },
                                colors = ButtonDefaults.buttonColors(containerColor = AlgorithmViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("dashboard_daily_plan_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Daily Plan ✨", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = onNavigateToDailyLog,
                                border = BorderStroke(1.dp, FlamePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FlamePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .testTag("dashboard_log_practice_btn")
                            ) {
                                Text("Log", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onNavigateToTopic(topic.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("dashboard_go_to_topic_btn")
                            ) {
                                Text("Roadmap", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Spaced Revision Section Card
            val due3Days = remember(revisionItems) { revisionItems.count { it.is3DaysAgo || it.daysAgo in 3L..6L } }
            val due7Days = remember(revisionItems) { revisionItems.count { it.is7DaysAgo || it.daysAgo in 7L..29L } }
            val due30Days = remember(revisionItems) { revisionItems.count { it.is30DaysAgo || it.daysAgo >= 30L } }
            val totalDue = remember(revisionItems) { revisionItems.count { it.isDue } }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_spaced_revision_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, FlamePrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = FlamePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = FlamePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Spaced Revision",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Revisit problems solved 3, 7, and 30 days ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (totalDue > 0) FlamePrimary.copy(alpha = 0.15f) else SolvedGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (totalDue > 0) "$totalDue Due" else "Up to date ✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalDue > 0) FlamePrimary else SolvedGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Interval Badges row (3d, 7d, 30d)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RevisionDashboardBadge(
                            label = "3d Recall",
                            count = due3Days,
                            color = FlamePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        RevisionDashboardBadge(
                            label = "7d Weekly",
                            count = due7Days,
                            color = TechCyan,
                            modifier = Modifier.weight(1f)
                        )
                        RevisionDashboardBadge(
                            label = "30d Mastery",
                            count = due30Days,
                            color = AlgorithmViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Show top due problems preview if any
                    val topDueProblems = remember(revisionItems) {
                        revisionItems.filter { it.isDue }.take(2)
                    }

                    if (topDueProblems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        topDueProblems.forEach { dueItem ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dueItem.problem.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = dueItem.topic?.name ?: "Solved ${dueItem.daysAgo}d ago",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            dueItem.is3DaysAgo -> FlamePrimary.copy(alpha = 0.15f)
                                            dueItem.is7DaysAgo -> TechCyan.copy(alpha = 0.15f)
                                            else -> AlgorithmViolet.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                dueItem.is3DaysAgo -> "3d due"
                                                dueItem.is7DaysAgo -> "7d due"
                                                else -> "${dueItem.daysAgo}d due"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                dueItem.is3DaysAgo -> FlamePrimary
                                                dueItem.is7DaysAgo -> TechCyan
                                                else -> AlgorithmViolet
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToRevision,
                        colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_open_revision_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalDue > 0) "Review $totalDue Problems Now" else "Open Revision Section",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 4. Overall Roadmap Progress Ring Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roadmap_progress_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { roadmapProgress },
                            modifier = Modifier.size(86.dp),
                            color = FlamePrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(roadmapProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roadmap Completion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedTopicsCount of ${topics.size} Topics Completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SolvedGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$totalSolvedProblems Total Problems Solved 🚀",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SolvedGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Contribution Heatmap (GitHub style)
            ContributionHeatmap(dailyLogs = dailyLogs)

            // 5. Weekly Bar Chart (Last 7 Days)
            WeeklyBarChart(
                dailyLogs = dailyLogs,
                dailyGoal = settings.dailyGoal
            )

            // Bottom padding for FAB space
            Spacer(modifier = Modifier.height(64.dp))
        }

        // Quick "Log Today" FAB
        ExtendedFloatingActionButton(
            onClick = onNavigateToDailyLog,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Log Today", fontWeight = FontWeight.Bold) },
            containerColor = FlamePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("dashboard_log_today_fab")
        )
    }
}

@Composable
private fun RevisionDashboardBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

