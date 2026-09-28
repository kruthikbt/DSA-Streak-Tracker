package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import com.example.ui.theme.AlgorithmViolet
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicCard(
    topic: TopicEntity,
    problems: List<ProblemEntity>,
    isCurrentActive: Boolean,
    isUnlocked: Boolean,
    onStatusChange: (String) -> Unit,
    onToggleSubtopic: (Int) -> Unit,
    onAddSubtopic: (String) -> Unit,
    onDeleteSubtopic: (Int) -> Unit,
    onAddProblem: (ProblemEntity) -> Unit,
    onBulkImportProblems: (List<ProblemEntity>) -> Unit = {},
    onUpdateProblem: (ProblemEntity) -> Unit,
    onToggleProblemSolved: (ProblemEntity) -> Unit,
    onDeleteProblem: (String) -> Unit,
    onRequestDailyPlan: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(isCurrentActive) }
    var showStatusMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showAddSubtopicDialog by remember { mutableStateOf(false) }
    var showAddProblemDialog by remember { mutableStateOf(false) }
    var showBulkImportSheet by remember { mutableStateOf(false) }
    var editingProblem by remember { mutableStateOf<ProblemEntity?>(null) }
    var problemToDelete by remember { mutableStateOf<ProblemEntity?>(null) }

    val totalProblems = problems.size
    val solvedProblems = problems.count { it.solved }
    val progress = if (totalProblems > 0) {
        solvedProblems.toFloat() / totalProblems.toFloat()
    } else {
        if (topic.status == TopicEntity.STATUS_COMPLETED) 1f else 0f
    }

    val cardAlpha = if (isUnlocked || isCurrentActive) 1f else 0.85f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .testTag("topic_card_${topic.order}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            }
        ),
        border = BorderStroke(
            width = if (isCurrentActive) 2.dp else 1.dp,
            color = when {
                isCurrentActive -> FlamePrimary
                topic.status == TopicEntity.STATUS_COMPLETED -> SolvedGreen.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header Row: Order Number Badge, Title, Status & Expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order Number Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        topic.status == TopicEntity.STATUS_COMPLETED -> SolvedGreen.copy(alpha = 0.2f)
                        isCurrentActive -> FlamePrimary.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = "#%02d".format(topic.order),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            topic.status == TopicEntity.STATUS_COMPLETED -> SolvedGreen
                            isCurrentActive -> FlamePrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Active indicator
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCurrentActive) {
                            Text(
                                text = "Current Topic 🎯",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FlamePrimary
                            )
                        } else if (!isUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Upcoming",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "$solvedProblems/$totalProblems solved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip (clickable to change)
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when (topic.status) {
                            TopicEntity.STATUS_COMPLETED -> SolvedGreen.copy(alpha = 0.15f)
                            TopicEntity.STATUS_IN_PROGRESS -> FlamePrimary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when (topic.status) {
                                TopicEntity.STATUS_COMPLETED -> SolvedGreen
                                TopicEntity.STATUS_IN_PROGRESS -> FlamePrimary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.clickable { showStatusMenu = true }
                    ) {
                        Text(
                            text = topic.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (topic.status) {
                                TopicEntity.STATUS_COMPLETED -> SolvedGreen
                                TopicEntity.STATUS_IN_PROGRESS -> FlamePrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Not Started") },
                            onClick = {
                                onStatusChange(TopicEntity.STATUS_NOT_STARTED)
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("In Progress") },
                            onClick = {
                                onStatusChange(TopicEntity.STATUS_IN_PROGRESS)
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Completed") },
                            onClick = {
                                onStatusChange(TopicEntity.STATUS_COMPLETED)
                                showStatusMenu = false
                            }
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Topic Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 1f) SolvedGreen else FlamePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) SolvedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded Section: Subtopics & Problems
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (onRequestDailyPlan != null) {
                        Button(
                            onClick = onRequestDailyPlan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topic_daily_plan_btn_${topic.order}"),
                            colors = ButtonDefaults.buttonColors(containerColor = AlgorithmViolet),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Daily Plan for this Topic ✨", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Quick Action: Mark Complete Button if not completed
                    if (topic.status != TopicEntity.STATUS_COMPLETED) {
                        OutlinedButton(
                            onClick = { onStatusChange(TopicEntity.STATUS_COMPLETED) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mark_completed_btn_${topic.order}"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SolvedGreen),
                            border = BorderStroke(1.dp, SolvedGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Topic as Completed", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // --- SUBTOPICS SECTION ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subtopics = topic.getSubtopics()
                        val completedSubtopics = subtopics.count { it.completed }
                        Text(
                            text = "Subtopics ($completedSubtopics/${subtopics.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { showAddSubtopicDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Subtopic", fontSize = 12.sp)
                        }
                    }

                    val subtopics = topic.getSubtopics()
                    if (subtopics.isEmpty()) {
                        Text(
                            text = "No subtopics added yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            subtopics.forEachIndexed { index, subtopic ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = subtopic.completed,
                                            onCheckedChange = { onToggleSubtopic(index) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = SolvedGreen,
                                                checkmarkColor = Color.White
                                            ),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = subtopic.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            textDecoration = if (subtopic.completed) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (subtopic.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onDeleteSubtopic(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete subtopic",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- PROBLEMS SECTION ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Problems ($solvedProblems/$totalProblems)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { showBulkImportSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                modifier = Modifier.testTag("bulk_import_btn_${topic.order}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Bulk Import", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { showAddProblemDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                modifier = Modifier.testTag("add_problem_btn_${topic.order}")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (problems.isEmpty()) {
                        Text(
                            text = "No problems added yet. Click '+ Add' or 'Bulk Import' to paste a list of titles!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            problems.forEach { problem ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (problem.solved) SolvedGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = problem.solved,
                                            onCheckedChange = { onToggleProblemSolved(problem) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = SolvedGreen,
                                                checkmarkColor = Color.White
                                            ),
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = problem.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (problem.solved) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (problem.solved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Difficulty Chip
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (problem.difficulty) {
                                                        "Easy" -> SolvedGreen.copy(alpha = 0.2f)
                                                        "Hard" -> HardRed.copy(alpha = 0.2f)
                                                        else -> MediumYellow.copy(alpha = 0.2f)
                                                    }
                                                ) {
                                                    Text(
                                                        text = problem.difficulty,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (problem.difficulty) {
                                                            "Easy" -> SolvedGreen
                                                            "Hard" -> HardRed
                                                            else -> MediumYellow
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                // Platform Chip
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = TechCyan.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = problem.platform,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TechCyan,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                if (problem.solved && !problem.solvedDate.isNullOrBlank()) {
                                                    Text(
                                                        text = "Solved ${problem.solvedDate}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SolvedGreen,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Problem link button
                                        if (problem.link.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(problem.link))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {
                                                        Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                    contentDescription = "Open Problem Link",
                                                    tint = TechCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Edit Problem
                                        IconButton(
                                            onClick = { editingProblem = problem },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Problem",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete Problem
                                        IconButton(
                                            onClick = { problemToDelete = problem },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Problem",
                                                tint = HardRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // Add Subtopic Dialog
    if (showAddSubtopicDialog) {
        var subtopicText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubtopicDialog = false },
            title = { Text("Add Subtopic") },
            text = {
                OutlinedTextField(
                    value = subtopicText,
                    onValueChange = { subtopicText = it },
                    label = { Text("Subtopic Name") },
                    placeholder = { Text("e.g. Dynamic Window Maximum") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subtopicText.isNotBlank()) {
                            onAddSubtopic(subtopicText)
                            showAddSubtopicDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubtopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Problem Dialog
    if (showAddProblemDialog) {
        ProblemFormDialog(
            title = "Add Problem to ${topic.name}",
            initialProblem = ProblemEntity(
                id = UUID.randomUUID().toString(),
                topicId = topic.id,
                title = "",
                difficulty = "Medium",
                platform = "LeetCode",
                link = "",
                solved = false
            ),
            onSave = { problem ->
                onAddProblem(problem)
                showAddProblemDialog = false
            },
            onDismiss = { showAddProblemDialog = false }
        )
    }

    // Edit Problem Dialog
    editingProblem?.let { problem ->
        ProblemFormDialog(
            title = "Edit Problem",
            initialProblem = problem,
            onSave = { updated ->
                onUpdateProblem(updated)
                editingProblem = null
            },
            onDismiss = { editingProblem = null }
        )
    }

    // Delete Problem Confirmation Dialog
    problemToDelete?.let { problem ->
        AlertDialog(
            onDismissRequest = { problemToDelete = null },
            title = { Text("Delete Problem?") },
            text = { Text("Are you sure you want to remove '${problem.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProblem(problem.id)
                        problemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { problemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkImportSheet) {
        BulkImportSheet(
            topic = topic,
            existingProblems = problems,
            onImportProblems = { importedList ->
                onBulkImportProblems(importedList)
            },
            onDismiss = { showBulkImportSheet = false }
        )
    }
}

@Composable
fun ProblemFormDialog(
    title: String,
    initialProblem: ProblemEntity,
    onSave: (ProblemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var titleState by remember { mutableStateOf(initialProblem.title) }
    var difficultyState by remember { mutableStateOf(initialProblem.difficulty) }
    var platformState by remember { mutableStateOf(initialProblem.platform) }
    var linkState by remember { mutableStateOf(initialProblem.link) }
    var solvedState by remember { mutableStateOf(initialProblem.solved) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    label = { Text("Problem Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Difficulty selector
                Text("Difficulty", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val isSelected = difficultyState == diff
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                when (diff) {
                                    "Easy" -> SolvedGreen.copy(alpha = 0.25f)
                                    "Hard" -> HardRed.copy(alpha = 0.25f)
                                    else -> MediumYellow.copy(alpha = 0.25f)
                                }
                            } else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) {
                                    when (diff) {
                                        "Easy" -> SolvedGreen
                                        "Hard" -> HardRed
                                        else -> MediumYellow
                                    }
                                } else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { difficultyState = diff }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = diff,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isSelected) {
                                        when (diff) {
                                            "Easy" -> SolvedGreen
                                            "Hard" -> HardRed
                                            else -> MediumYellow
                                        }
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Platform selector
                Text("Platform", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("LeetCode", "GFG", "Codeforces", "Other").forEach { plat ->
                        val isSelected = platformState == plat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TechCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) TechCyan else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { platformState = plat }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = plat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TechCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = linkState,
                    onValueChange = { linkState = it },
                    label = { Text("Problem Link (optional)") },
                    placeholder = { Text("https://leetcode.com/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { solvedState = !solvedState }
                ) {
                    Checkbox(
                        checked = solvedState,
                        onCheckedChange = { solvedState = it },
                        colors = CheckboxDefaults.colors(checkedColor = SolvedGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Solved", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleState.isNotBlank()) {
                        onSave(
                            initialProblem.copy(
                                title = titleState.trim(),
                                difficulty = difficultyState,
                                platform = platformState,
                                link = linkState.trim(),
                                solved = solvedState,
                                solvedDate = if (solvedState && initialProblem.solvedDate.isNullOrBlank()) {
                                    java.time.LocalDate.now().toString()
                                } else if (!solvedState) {
                                    null
                                } else {
                                    initialProblem.solvedDate
                                }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
