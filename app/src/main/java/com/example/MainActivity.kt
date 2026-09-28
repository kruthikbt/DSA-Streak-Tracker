package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.DailyPlanDialog
import com.example.ui.components.MilestoneDialog
import com.example.ui.screens.DailyLogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RevisionScreen
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.StatsSettingsScreen
import com.example.ui.theme.DsaTrackerTheme
import com.example.ui.theme.FlamePrimary
import com.example.ui.viewmodel.DsaTrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DsaTrackerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
            val topics by viewModel.topics.collectAsStateWithLifecycle()
            val problems by viewModel.problems.collectAsStateWithLifecycle()
            val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
            val freezes by viewModel.freezes.collectAsStateWithLifecycle()
            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
            val milestoneToCelebrate by viewModel.milestoneToCelebrate.collectAsStateWithLifecycle()
            val activeTopicId by viewModel.activeTopicId.collectAsStateWithLifecycle()
            val editingLog by viewModel.editingLog.collectAsStateWithLifecycle()
            val dailyPlanDialogTopic by viewModel.dailyPlanDialogTopic.collectAsStateWithLifecycle()
            val isPlanLoading by viewModel.isPlanLoading.collectAsStateWithLifecycle()
            val suggestedProblems by viewModel.suggestedProblems.collectAsStateWithLifecycle()
            val planIsFromAI by viewModel.planIsFromAI.collectAsStateWithLifecycle()
            val planMessage by viewModel.planMessage.collectAsStateWithLifecycle()
            val planDifficulty by viewModel.planDifficulty.collectAsStateWithLifecycle()
            val revisionItems by viewModel.revisionItems.collectAsStateWithLifecycle()
            val selectedRevisionFilter by viewModel.selectedRevisionFilter.collectAsStateWithLifecycle()

            DsaTrackerTheme(darkTheme = settings.isDarkMode) {
                // If on secondary tab, back press navigates back to Dashboard (tab 0)
                BackHandler(enabled = selectedTab != 0) {
                    viewModel.selectTab(0)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "DSA Streak Tracker",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    // Streak pill
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = FlamePrimary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, FlamePrimary.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clickable { viewModel.selectTab(0) }
                                            .testTag("top_bar_streak_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Whatshot,
                                                contentDescription = "Streak Fire",
                                                tint = FlamePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${streakInfo.currentStreak}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = FlamePrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Dark/Light mode toggle
                                    IconButton(
                                        onClick = { viewModel.toggleDarkMode() },
                                        modifier = Modifier.testTag("theme_toggle_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (settings.isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                                            contentDescription = "Toggle Dark/Light Mode",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                val navItems = listOf(
                                    NavigationItem("Dashboard", Icons.Default.Dashboard, 0),
                                    NavigationItem("Roadmap", Icons.Default.AccountTree, 1),
                                    NavigationItem("Revision", Icons.Default.Psychology, 2),
                                    NavigationItem("Daily Log", Icons.Default.EditCalendar, 3),
                                    NavigationItem("Stats", Icons.Default.BarChart, 4)
                                )

                                navItems.forEach { item ->
                                    val isSelected = selectedTab == item.index
                                    val dueCount = remember(revisionItems) { revisionItems.count { it.isDue } }

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.selectTab(item.index) },
                                        icon = {
                                            if (item.index == 2 && dueCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = FlamePrimary,
                                                            contentColor = Color.White
                                                        ) {
                                                            Text(
                                                                text = "$dueCount",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.label
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = FlamePrimary,
                                            selectedTextColor = FlamePrimary,
                                            indicatorColor = FlamePrimary.copy(alpha = 0.2f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${item.index}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "tab_content_transition"
                            ) { tab ->
                                when (tab) {
                                    0 -> DashboardScreen(
                                        streakInfo = streakInfo,
                                        topics = topics,
                                        problems = problems,
                                        dailyLogs = dailyLogs,
                                        settings = settings,
                                        revisionItems = revisionItems,
                                        onNavigateToTopic = { topicId ->
                                            viewModel.navigateToRoadmapTopic(topicId)
                                        },
                                        onNavigateToDailyLog = {
                                            viewModel.selectTab(3)
                                        },
                                        onNavigateToRevision = {
                                            viewModel.selectTab(2)
                                        },
                                        onUseFreeze = {
                                            viewModel.useStreakFreezeToday()
                                        },
                                        onDailyPlanClick = { topic ->
                                            viewModel.requestDailyPlan(topic)
                                        }
                                    )

                                    1 -> RoadmapScreen(
                                        topics = topics,
                                        problems = problems,
                                        activeTopicId = activeTopicId,
                                        onStatusChange = { topic, newStatus ->
                                            viewModel.updateTopicStatus(topic, newStatus)
                                        },
                                        onToggleSubtopic = { topicId, index ->
                                            viewModel.toggleSubtopic(topicId, index)
                                        },
                                        onAddSubtopic = { topicId, title ->
                                            viewModel.addSubtopic(topicId, title)
                                        },
                                        onDeleteSubtopic = { topicId, index ->
                                            viewModel.deleteSubtopic(topicId, index)
                                        },
                                        onAddProblem = { problem ->
                                            viewModel.addProblem(problem)
                                        },
                                        onBulkImportProblems = { problemsToAdd ->
                                            viewModel.bulkAddProblems(problemsToAdd)
                                        },
                                        onUpdateProblem = { problem ->
                                            viewModel.updateProblem(problem)
                                        },
                                        onToggleProblemSolved = { problem ->
                                            viewModel.toggleProblemSolved(problem)
                                        },
                                        onDeleteProblem = { id ->
                                            viewModel.deleteProblem(id)
                                        },
                                        onRequestDailyPlan = { topic ->
                                            viewModel.requestDailyPlan(topic)
                                        }
                                    )

                                    2 -> RevisionScreen(
                                        revisionItems = revisionItems,
                                        selectedFilter = selectedRevisionFilter,
                                        onFilterSelected = { filter ->
                                            viewModel.selectRevisionFilter(filter)
                                        },
                                        onMarkRevised = { id, notes ->
                                            viewModel.markProblemRevised(id, notes)
                                        },
                                        onSetDaysAgo = { id, daysAgo ->
                                            viewModel.setProblemSolvedDaysAgo(id, daysAgo)
                                        },
                                        onUpdateNotes = { id, notes ->
                                            viewModel.updateProblemRevisionNotes(id, notes)
                                        },
                                        onSeedSampleRevisions = {
                                            viewModel.seedSampleRevisions()
                                        },
                                        onNavigateToRoadmap = {
                                            viewModel.selectTab(1)
                                        }
                                    )

                                    3 -> DailyLogScreen(
                                        topics = topics,
                                        dailyLogs = dailyLogs,
                                        settings = settings,
                                        editingLog = editingLog,
                                        onSaveLog = { date, topicId, pCount, mins, easy, med, hard, notes ->
                                            viewModel.logPractice(date, topicId, pCount, mins, easy, med, hard, notes)
                                        },
                                        onOverwriteLog = { updatedLog ->
                                            viewModel.overwriteLog(updatedLog)
                                        },
                                        onDeleteLog = { date ->
                                            viewModel.deleteLog(date)
                                        },
                                        onCancelEdit = {
                                            viewModel.setEditingLog(null)
                                        },
                                        onStartEdit = { log ->
                                            viewModel.setEditingLog(log)
                                        }
                                    )

                                    4 -> StatsSettingsScreen(
                                        topics = topics,
                                        problems = problems,
                                        dailyLogs = dailyLogs,
                                        freezes = freezes,
                                        settings = settings,
                                        onToggleDarkMode = {
                                            viewModel.toggleDarkMode()
                                        },
                                        onUpdateSettings = { goal, mins, freezeEnabled ->
                                            viewModel.updateSettings(goal, mins, freezeEnabled)
                                        },
                                        onLoadDemoData = {
                                            viewModel.loadDemoData()
                                        },
                                        onExportJson = {
                                            viewModel.exportJson()
                                        },
                                        onImportJson = { jsonStr ->
                                            viewModel.importJson(jsonStr)
                                        },
                                        onResetAllData = {
                                            viewModel.resetAllData()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Milestone Celebration Overlay & Dialog
                    milestoneToCelebrate?.let { milestone ->
                        ConfettiOverlay()
                        MilestoneDialog(
                            milestoneDays = milestone,
                            onDismiss = { viewModel.dismissMilestone() }
                        )
                    }

                    // Gemini AI Daily Plan Dialog
                    dailyPlanDialogTopic?.let { topic ->
                        DailyPlanDialog(
                            topic = topic,
                            isLoading = isPlanLoading,
                            suggestedProblems = suggestedProblems,
                            isFromAI = planIsFromAI,
                            infoMessage = planMessage,
                            selectedDifficulty = planDifficulty,
                            onDifficultyChanged = { diff ->
                                viewModel.requestDailyPlan(topic, diff)
                            },
                            onRefresh = {
                                viewModel.requestDailyPlan(topic, planDifficulty)
                            },
                            onAddProblemToTopic = { problem ->
                                viewModel.addProblem(problem)
                            },
                            onDismiss = {
                                viewModel.dismissDailyPlanDialog()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)
