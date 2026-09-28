package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon as AndroidIcon
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import kotlinx.coroutines.launch

@Composable
fun StatsSettingsScreen(
    topics: List<TopicEntity>,
    problems: List<ProblemEntity>,
    dailyLogs: List<DailyLogEntity>,
    freezes: List<StreakFreezeEntity>,
    settings: UserSettingsEntity,
    onToggleDarkMode: () -> Unit,
    onUpdateSettings: (dailyGoal: Int, minMinutes: Int, streakFreezeEnabled: Boolean) -> Unit,
    onLoadDemoData: () -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: suspend (String) -> Boolean,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showInstallGuideDialog by remember { mutableStateOf(false) }

    // Stats calculations
    val totalProblemsSolved = remember(problems, dailyLogs) {
        val fromProblems = problems.count { it.solved }
        val fromLogs = dailyLogs.sumOf { it.problemsSolved }
        maxOf(fromProblems, fromLogs)
    }

    val totalMinutes = remember(dailyLogs) {
        dailyLogs.sumOf { it.minutes }
    }
    val totalHours = String.format("%.1f", totalMinutes / 60.0)

    val averageProblemsPerDay = remember(dailyLogs) {
        if (dailyLogs.isNotEmpty()) {
            String.format("%.1f", dailyLogs.sumOf { it.problemsSolved }.toDouble() / dailyLogs.size)
        } else "0.0"
    }

    val bestDayLog = remember(dailyLogs) {
        dailyLogs.maxByOrNull { it.problemsSolved }
    }

    // Difficulty breakdown
    val easySolved = remember(problems, dailyLogs) {
        val pCount = problems.count { it.solved && it.difficulty == "Easy" }
        val lCount = dailyLogs.sumOf { it.easyCount }
        maxOf(pCount, lCount)
    }
    val mediumSolved = remember(problems, dailyLogs) {
        val pCount = problems.count { it.solved && it.difficulty == "Medium" }
        val lCount = dailyLogs.sumOf { it.mediumCount }
        maxOf(pCount, lCount)
    }
    val hardSolved = remember(problems, dailyLogs) {
        val pCount = problems.count { it.solved && it.difficulty == "Hard" }
        val lCount = dailyLogs.sumOf { it.hardCount }
        maxOf(pCount, lCount)
    }
    val totalDifficultyCount = maxOf(1, easySolved + mediumSolved + hardSolved)

    val easyPct = (easySolved.toFloat() / totalDifficultyCount * 100).toInt()
    val medPct = (mediumSolved.toFloat() / totalDifficultyCount * 100).toInt()
    val hardPct = 100 - easyPct - medPct

    // Topic ranking breakdown
    val topicsByProblems = remember(topics, problems, dailyLogs) {
        topics.map { topic ->
            val solvedInProblems = problems.count { it.topicId == topic.id && it.solved }
            val solvedInLogs = dailyLogs.filter { it.topicId == topic.id }.sumOf { it.problemsSolved }
            Pair(topic, maxOf(solvedInProblems, solvedInLogs))
        }.sortedByDescending { it.second }
    }
    val maxTopicCount = maxOf(1, topicsByProblems.firstOrNull()?.second ?: 1)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("stats_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- STATS SECTION ---
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = FlamePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analytics & Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Summary Metric Cards 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Problems
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Solved", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$totalProblemsSolved",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = FlamePrimary
                            )
                            Text("All topics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Total Hours
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Practiced", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${totalHours}h",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TechCyan
                            )
                            Text("${totalMinutes} minutes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Average per Day
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Daily Average", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = averageProblemsPerDay,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolvedGreen
                            )
                            Text("Problems / day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Best Day
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Best Record", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (bestDayLog != null) "${bestDayLog.problemsSolved} in 1d" else "0",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MediumYellow
                            )
                            Text(
                                text = bestDayLog?.date ?: "None yet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Difficulty Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Difficulty Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-color segmented bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        if (easySolved > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(easySolved.toFloat())
                                    .fillMaxHeight()
                                    .background(SolvedGreen)
                            )
                        }
                        if (mediumSolved > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(mediumSolved.toFloat())
                                    .fillMaxHeight()
                                    .background(MediumYellow)
                            )
                        }
                        if (hardSolved > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(hardSolved.toFloat())
                                    .fillMaxHeight()
                                    .background(HardRed)
                            )
                        }
                        if (easySolved == 0 && mediumSolved == 0 && hardSolved == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Easy
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(SolvedGreen, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Easy: $easySolved ($easyPct%)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        // Medium
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(MediumYellow, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Med: $mediumSolved ($medPct%)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        // Hard
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(HardRed, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Hard: $hardSolved ($hardPct%)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Problems per Topic Card (Top 5 practiced topics)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Problems by Topic (Top Focus)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val topTopics = topicsByProblems.take(5)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topTopics.forEach { (topic, count) ->
                            val fraction = (count.toFloat() / maxTopicCount.toFloat()).coerceIn(0.05f, 1f)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#%02d. %s".format(topic.order, topic.name),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$count solved",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FlamePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = TechCyan,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SETTINGS SECTION ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = TechCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Habit & App Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Theme", fontWeight = FontWeight.Bold)
                            Text("Switch between dark and light appearance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                imageVector = if (settings.isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                                contentDescription = "Toggle Theme",
                                tint = FlamePrimary
                            )
                        }
                    }

                    // Daily Goal Slider
                    var goalState by remember(settings.dailyGoal) { mutableIntStateOf(settings.dailyGoal) }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Daily Goal", fontWeight = FontWeight.Bold)
                            Text("$goalState problems/day", color = FlamePrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = goalState.toFloat(),
                            onValueChange = { goalState = it.toInt() },
                            onValueChangeFinished = {
                                onUpdateSettings(goalState, settings.minMinutes, settings.streakFreezeEnabled)
                            },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = FlamePrimary,
                                activeTrackColor = FlamePrimary
                            )
                        )
                    }

                    // Minimum Minutes Slider
                    var minutesState by remember(settings.minMinutes) { mutableIntStateOf(settings.minMinutes) }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Minimum Minutes for Streak", fontWeight = FontWeight.Bold)
                            Text("${minutesState} mins", color = TechCyan, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = minutesState.toFloat(),
                            onValueChange = { minutesState = it.toInt() },
                            onValueChangeFinished = {
                                onUpdateSettings(settings.dailyGoal, minutesState, settings.streakFreezeEnabled)
                            },
                            valueRange = 10f..120f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = TechCyan,
                                activeTrackColor = TechCyan
                            )
                        )
                    }

                    // Streak Freeze Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Streak Freeze Protection", fontWeight = FontWeight.Bold)
                            Text(
                                text = "2 freezes per month to protect streak when life happens.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.streakFreezeEnabled,
                            onCheckedChange = {
                                onUpdateSettings(settings.dailyGoal, settings.minMinutes, it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TechCyan)
                        )
                    }

                    // Reminder Time Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Practice Reminder", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${settings.reminderTime} Daily",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FlamePrimary
                        )
                    }
                }
            }
        }

        // --- MOBILE INSTALL & HOME SCREEN SECTION ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mobile Install & Home Screen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_install_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TechCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Install on Phone / Home Screen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Native Android App (Kotlin & Jetpack Compose)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Because this is a native Android app, you can install the APK directly onto your Android device for 100% offline access, home screen shortcuts, and fast performance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                                    if (shortcutManager?.isRequestPinShortcutSupported == true) {
                                        val pinShortcutInfo = ShortcutInfo.Builder(context, "dsa_tracker_pinned_shortcut")
                                            .setShortLabel("DSA Tracker")
                                            .setLongLabel("DSA Streak Tracker & Roadmap")
                                            .setIcon(AndroidIcon.createWithResource(context, com.example.R.mipmap.ic_launcher))
                                            .setIntent(
                                                Intent(context, com.example.MainActivity::class.java).apply {
                                                    action = Intent.ACTION_MAIN
                                                }
                                            )
                                            .build()
                                        shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                                        Toast.makeText(context, "Pinned shortcut requested to Home Screen!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Pinning not supported by this launcher", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Requires Android 8.0+", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("pin_to_homescreen_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pin to Home Screen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showInstallGuideDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("install_guide_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install Guide", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Open DSA Streak Tracker in your phone browser: https://ais-pre-znfy2xowlhczi6i5s33z45-963663864856.asia-southeast1.run.app")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share App Link"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_app_link_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link to Open on Mobile Browser", fontSize = 12.sp)
                    }
                }
            }
        }

        // --- DATA MANAGEMENT SECTION ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data & Backup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Load Demo Data
                    Button(
                        onClick = {
                            onLoadDemoData()
                            Toast.makeText(context, "Loaded demo data! Check Dashboard & Heatmap.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("load_demo_data_button")
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Demo Data (Preview App UI)", fontWeight = FontWeight.Bold)
                    }

                    // Export JSON
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                exportContent = onExportJson()
                                showExportDialog = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_json_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Data as JSON")
                    }

                    // Import JSON
                    OutlinedButton(
                        onClick = {
                            importInputText = ""
                            showImportDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("import_json_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import from JSON")
                    }

                    // Reset Data
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HardRed.copy(alpha = 0.15f), contentColor = HardRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reset_all_data_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Data (JSON)") },
            text = {
                Column {
                    Text(
                        text = "Copy this JSON to backup or transfer your DSA progress.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportContent,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("DSA Tracker Backup", exportContent))
                        Toast.makeText(context, "Copied JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data (JSON)") },
            text = {
                Column {
                    Text(
                        text = "Paste valid JSON exported from DSA Streak Tracker.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("{ \"topics\": [...], \"logs\": [...] }") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = onImportJson(importInputText)
                            if (success) {
                                Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Failed to parse JSON. Please check format.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Data?") },
            text = {
                Text("This will erase all daily logs, problem solves, and reset the 25 topics to initial state. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "All data reset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Install Guide Dialog
    if (showInstallGuideDialog) {
        AlertDialog(
            onDismissRequest = { showInstallGuideDialog = false },
            title = {
                Text("How to Install on Your Android Phone", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This application is built as a native Android APK (Kotlin & Jetpack Compose):\n\n" +
                                "1. In Google AI Studio Build, tap the Settings/Export menu in the top right.\n\n" +
                                "2. Select 'Download APK' or 'Export Project' to generate the APK file.\n\n" +
                                "3. Transfer or download the APK file to your Android phone.\n\n" +
                                "4. Open the APK file in your phone's File Manager and tap 'Install'.\n\n" +
                                "5. The DSA Tracker app icon will appear directly on your phone's home screen with 100% offline Room database storage and instant startup!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInstallGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}
