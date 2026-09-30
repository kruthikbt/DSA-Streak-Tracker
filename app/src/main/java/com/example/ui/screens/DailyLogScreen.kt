package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyLogScreen(
    topics: List<TopicEntity>,
    dailyLogs: List<DailyLogEntity>,
    settings: UserSettingsEntity,
    editingLog: DailyLogEntity?,
    onSaveLog: (date: String, topicId: Int, problems: Int, minutes: Int, easy: Int, med: Int, hard: Int, notes: String) -> Unit,
    onOverwriteLog: (DailyLogEntity) -> Unit,
    onDeleteLog: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (DailyLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    var selectedDateStr by remember { mutableStateOf(today.toString()) }
    var selectedTopicId by remember { mutableIntStateOf(1) }
    var problemsCount by remember { mutableIntStateOf(2) }
    var minutesCount by remember { mutableIntStateOf(45) }
    var easyCount by remember { mutableIntStateOf(1) }
    var mediumCount by remember { mutableIntStateOf(1) }
    var hardCount by remember { mutableIntStateOf(0) }
    var notesText by remember { mutableStateOf("") }
    var isTopicDropdownExpanded by remember { mutableStateOf(false) }

    var logToDelete by remember { mutableStateOf<DailyLogEntity?>(null) }

    // Synchronize form when editingLog changes
    LaunchedEffect(editingLog) {
        if (editingLog != null) {
            selectedDateStr = editingLog.date
            selectedTopicId = editingLog.topicId
            problemsCount = editingLog.problemsSolved
            minutesCount = editingLog.minutes
            easyCount = editingLog.easyCount
            mediumCount = editingLog.mediumCount
            hardCount = editingLog.hardCount
            notesText = editingLog.notes
        }
    }

    val selectedTopic = topics.find { it.id == selectedTopicId } ?: topics.firstOrNull()
    val isAlreadyLoggedForSelectedDate = remember(selectedDateStr, dailyLogs) {
        dailyLogs.any { it.date == selectedDateStr }
    }

    val isDayCompleted = (problemsCount >= 1) || (minutesCount >= settings.minMinutes)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("daily_log_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Practice Logging Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_log_form_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (editingLog != null) TechCyan else FlamePrimary.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingLog != null) "Edit Practice Log" else "Log Today's Practice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (editingLog != null) {
                            TextButton(onClick = onCancelEdit) {
                                Text("Cancel", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date selector row
                    Text("Date", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val parsed = try {
                                        LocalDate.parse(selectedDateStr)
                                    } catch (_: Exception) {
                                        today
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val date = LocalDate.of(y, m + 1, d)
                                            selectedDateStr = date.toString()
                                        },
                                        parsed.year,
                                        parsed.monthValue - 1,
                                        parsed.dayOfMonth
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = FlamePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedDateStr,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Quick buttons: Today & Yesterday
                        OutlinedButton(
                            onClick = { selectedDateStr = today.toString() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            colors = if (selectedDateStr == today.toString()) {
                                ButtonDefaults.outlinedButtonColors(containerColor = FlamePrimary.copy(alpha = 0.15f))
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Today", fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }

                        OutlinedButton(
                            onClick = { selectedDateStr = today.minusDays(1).toString() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            colors = if (selectedDateStr == today.minusDays(1).toString()) {
                                ButtonDefaults.outlinedButtonColors(containerColor = FlamePrimary.copy(alpha = 0.15f))
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("Yesterday", fontSize = 11.sp, maxLines = 1, softWrap = false)
                        }
                    }

                    if (isAlreadyLoggedForSelectedDate && editingLog == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TechCyan.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ℹ️ An entry exists for $selectedDateStr. Submitting will merge with the existing day's totals.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechCyan,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Topic Dropdown Selector
                    Text("Topic", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTopicDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedTopic != null) "#%02d. ${selectedTopic.name}".format(selectedTopic.order) else "Select a Topic",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = isTopicDropdownExpanded,
                            onDismissRequest = { isTopicDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            topics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text("#%02d. ${topic.name}".format(topic.order)) },
                                    onClick = {
                                        selectedTopicId = topic.id
                                        isTopicDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Counters Row: Problems Solved & Time Spent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Problems Solved Counter
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Problems Solved", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (problemsCount > 0) problemsCount-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "$problemsCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FlamePrimary
                                    )
                                    IconButton(
                                        onClick = { problemsCount++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Time Spent Counter (minutes)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Time (Minutes)", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (minutesCount >= 15) minutesCount -= 15 },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "${minutesCount}m",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TechCyan
                                    )
                                    IconButton(
                                        onClick = { minutesCount += 15 },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Difficulty Mix Breakdown
                    Text("Difficulty Mix", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Easy
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SolvedGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SolvedGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Easy", color = SolvedGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(onClick = { if (easyCount > 0) easyCount-- }, modifier = Modifier.size(28.dp)) {
                                        Text("-", fontWeight = FontWeight.Bold)
                                    }
                                    Text("$easyCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    IconButton(onClick = { easyCount++ }, modifier = Modifier.size(28.dp)) {
                                        Text("+", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Medium
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MediumYellow.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MediumYellow.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Medium", color = MediumYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(onClick = { if (mediumCount > 0) mediumCount-- }, modifier = Modifier.size(28.dp)) {
                                        Text("-", fontWeight = FontWeight.Bold)
                                    }
                                    Text("$mediumCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    IconButton(onClick = { mediumCount++ }, modifier = Modifier.size(28.dp)) {
                                        Text("+", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Hard
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = HardRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, HardRed.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Hard", color = HardRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(onClick = { if (hardCount > 0) hardCount-- }, modifier = Modifier.size(28.dp)) {
                                        Text("-", fontWeight = FontWeight.Bold)
                                    }
                                    Text("$hardCount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    IconButton(onClick = { hardCount++ }, modifier = Modifier.size(28.dp)) {
                                        Text("+", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes Field
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("What I learned / where I got stuck") },
                        placeholder = { Text("e.g. Used two-pointer technique on sorted array. Tricky edge case with negative numbers.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("practice_log_notes_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Streak completion qualification chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDayCompleted) SolvedGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDayCompleted) Icons.Default.CheckCircle else Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isDayCompleted) SolvedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDayCompleted) {
                                    "Counts as a completed day! Keeps your streak burning 🔥"
                                } else {
                                    "Log at least 1 problem or ${settings.minMinutes} mins to qualify for streak."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDayCompleted) SolvedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (editingLog != null) {
                                onOverwriteLog(
                                    DailyLogEntity(
                                        date = selectedDateStr,
                                        topicId = selectedTopicId,
                                        problemsSolved = problemsCount,
                                        minutes = minutesCount,
                                        easyCount = easyCount,
                                        mediumCount = mediumCount,
                                        hardCount = hardCount,
                                        notes = notesText
                                    )
                                )
                            } else {
                                onSaveLog(
                                    selectedDateStr,
                                    selectedTopicId,
                                    problemsCount,
                                    minutesCount,
                                    easyCount,
                                    mediumCount,
                                    hardCount,
                                    notesText
                                )
                            }
                            // Reset form back to default today
                            selectedDateStr = today.toString()
                            notesText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_practice_log_button")
                    ) {
                        Text(
                            text = if (editingLog != null) "Update Log" else "Save Practice Log 🔥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Section: Past Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Past Practice Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${dailyLogs.size} logs recorded",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (dailyLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No logs yet! 🎯",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log your first practice session above to start your streak.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(dailyLogs, key = { it.date }) { log ->
                val topic = topics.find { it.id == log.topicId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_log_item_${log.date}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.date,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = topic?.let { "#%02d. ${it.name}".format(it.order) } ?: "Topic #${log.topicId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FlamePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onStartEdit(log) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit log",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { logToDelete = log },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete log",
                                        tint = HardRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FlamePrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${log.problemsSolved} problems",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FlamePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TechCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${log.minutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TechCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (log.easyCount > 0) {
                                Text(
                                    text = "${log.easyCount}E",
                                    color = SolvedGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (log.mediumCount > 0) {
                                Text(
                                    text = "${log.mediumCount}M",
                                    color = MediumYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (log.hardCount > 0) {
                                Text(
                                    text = "${log.hardCount}H",
                                    color = HardRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "“${log.notes}”",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete Log Confirmation Dialog
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Log?") },
            text = { Text("Are you sure you want to remove the practice log for ${log.date}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLog(log.date)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
