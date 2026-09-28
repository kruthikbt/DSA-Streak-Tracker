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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RevisionProblemItem
import com.example.ui.theme.AlgorithmViolet
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan

@Composable
fun RevisionCard(
    item: RevisionProblemItem,
    onMarkRevised: (String, String?) -> Unit,
    onSetDaysAgo: (String, Int) -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var notesText by remember(item.problem.revisionNotes) { mutableStateOf(item.problem.revisionNotes) }

    val intervalColor = when {
        item.is3DaysAgo -> FlamePrimary
        item.is7DaysAgo -> TechCyan
        item.is30DaysAgo -> AlgorithmViolet
        item.daysAgo in 3L..6L -> FlamePrimary
        item.daysAgo in 7L..29L -> TechCyan
        else -> AlgorithmViolet
    }

    val intervalTitle = when {
        item.is3DaysAgo -> "3 Days Ago (Due for Recall)"
        item.is7DaysAgo -> "7 Days Ago (Weekly Consolidation)"
        item.is30DaysAgo -> "30 Days Ago (Long-term Mastery)"
        item.daysAgo in 3L..6L -> "3-Day Window (${item.daysAgo}d ago)"
        item.daysAgo in 7L..29L -> "7-Day Window (${item.daysAgo}d ago)"
        item.daysAgo >= 30L -> "30-Day Window (${item.daysAgo}d ago)"
        else -> "Solved ${item.daysAgo} days ago"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revision_card_${item.problem.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (item.isRevisedToday) 1.5.dp else 1.dp,
            color = if (item.isRevisedToday) SolvedGreen.copy(alpha = 0.8f) else intervalColor.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Topic badge & Interval Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Topic tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = item.topic?.let { "Topic #${it.order}: ${it.name}" } ?: "DSA Curriculum",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }

                // Spaced Repetition Interval Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = intervalColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, intervalColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = intervalColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                item.is3DaysAgo -> "3d Recall"
                                item.is7DaysAgo -> "7d Recall"
                                item.is30DaysAgo -> "30d Recall"
                                else -> "${item.daysAgo}d ago"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = intervalColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Problem Title & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.problem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Difficulty chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (item.problem.difficulty) {
                        "Easy" -> SolvedGreen.copy(alpha = 0.2f)
                        "Hard" -> HardRed.copy(alpha = 0.2f)
                        else -> MediumYellow.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = item.problem.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (item.problem.difficulty) {
                            "Easy" -> SolvedGreen
                            "Hard" -> HardRed
                            else -> MediumYellow
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Solved date & platform info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solved on ${item.problem.solvedDate ?: "N/A"} • $intervalTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = TechCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.problem.platform,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Revision status indicator if revised today
            if (item.isRevisedToday) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SolvedGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SolvedGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolvedGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Revised Today! Retention reinforced.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SolvedGreen
                        )
                    }
                }
            }

            // Notes preview (if any)
            if (item.problem.revisionNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Recall Notes & Invariants:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Notes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showNotesDialog = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.problem.revisionNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = if (isExpanded) 10 else 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary actions: Link & Notes & Date simulator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.problem.link.isNotBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.problem.link))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open problem URL", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("revision_open_link_${item.problem.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Problem on Platform",
                                tint = TechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("revision_edit_notes_${item.problem.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Revision Notes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDateDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("revision_change_date_${item.problem.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Simulate/Adjust Solved Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Primary Action: Mark Revised Today
                Button(
                    onClick = {
                        onMarkRevised(item.problem.id, null)
                        Toast.makeText(context, "Marked '${item.problem.title}' revised!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isRevisedToday) SolvedGreen.copy(alpha = 0.3f) else intervalColor,
                        contentColor = if (item.isRevisedToday) SolvedGreen else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.testTag("revision_mark_revised_btn_${item.problem.id}")
                ) {
                    Icon(
                        imageVector = if (item.isRevisedToday) Icons.Default.Check else Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isRevisedToday) "Revised ✓" else "Mark Revised",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Notes Dialog
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = {
                Text("Revision Notes: ${item.problem.title}", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    Text(
                        text = "Jot down algorithmic insights, invariants, time/space trade-offs, and tricky pitfalls to test your recall next time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("revision_notes_input_${item.problem.id}"),
                        placeholder = { Text("e.g. Invariant: Maintain two pointers. Watch out for duplicate elements...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNotes(item.problem.id, notesText)
                        showNotesDialog = false
                        Toast.makeText(context, "Notes saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary)
                ) {
                    Text("Save Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Simulator / Adjustment Dialog
    if (showDateDialog) {
        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = {
                Text("Adjust Solved Date / Spaced Interval", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Simulate when you solved '${item.problem.title}' to test how it surfaces in the 3, 7, and 30-day revision cycles:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            onSetDaysAgo(item.problem.id, 3)
                            showDateDialog = false
                            Toast.makeText(context, "Set to 3 days ago!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set to 3 Days Ago (First Recall)")
                    }

                    Button(
                        onClick = {
                            onSetDaysAgo(item.problem.id, 7)
                            showDateDialog = false
                            Toast.makeText(context, "Set to 7 days ago!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set to 7 Days Ago (Weekly Consolidation)")
                    }

                    Button(
                        onClick = {
                            onSetDaysAgo(item.problem.id, 30)
                            showDateDialog = false
                            Toast.makeText(context, "Set to 30 days ago!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlgorithmViolet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set to 30 Days Ago (Long-term Mastery)")
                    }

                    OutlinedButton(
                        onClick = {
                            onSetDaysAgo(item.problem.id, 0)
                            showDateDialog = false
                            Toast.makeText(context, "Set to Today!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set to Today (0 Days Ago)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
