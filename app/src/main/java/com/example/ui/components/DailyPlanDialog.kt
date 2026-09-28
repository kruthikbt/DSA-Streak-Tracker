package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.ai.SuggestedProblem
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import com.example.ui.theme.AlgorithmViolet
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import java.util.UUID

@Composable
fun DailyPlanDialog(
    topic: TopicEntity,
    isLoading: Boolean,
    suggestedProblems: List<SuggestedProblem>,
    isFromAI: Boolean,
    infoMessage: String?,
    selectedDifficulty: String,
    onDifficultyChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onAddProblemToTopic: (ProblemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var addedTitles by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("daily_plan_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AlgorithmViolet.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AlgorithmViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Practice Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isFromAI) "Powered by Gemini 3.5 Flash ✨" else "Pattern Recommendations",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFromAI) AlgorithmViolet else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isLoading) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_daily_plan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Plan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Topic #${topic.order}: ${topic.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = FlamePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Difficulty selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Level:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val isSelected = selectedDifficulty.equals(diff, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDifficultyChanged(diff) },
                            label = { Text(diff, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (diff) {
                                    "Easy" -> SolvedGreen.copy(alpha = 0.2f)
                                    "Hard" -> HardRed.copy(alpha = 0.2f)
                                    else -> MediumYellow.copy(alpha = 0.2f)
                                },
                                selectedLabelColor = when (diff) {
                                    "Easy" -> SolvedGreen
                                    "Hard" -> HardRed
                                    else -> MediumYellow
                                }
                            ),
                            modifier = Modifier.testTag("difficulty_chip_$diff")
                        )
                    }
                }

                if (!infoMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = infoMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = AlgorithmViolet,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Consulting Gemini for 3 targeted problems...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tailoring to $selectedDifficulty level & solved history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (suggestedProblems.isEmpty()) {
                    Text(
                        text = "No suggestions available. Tap refresh to generate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedProblems) { item ->
                            val isAdded = addedTitles.contains(item.title)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAdded) SolvedGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Difficulty Chip
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (item.difficulty) {
                                                "Easy" -> SolvedGreen.copy(alpha = 0.2f)
                                                "Hard" -> HardRed.copy(alpha = 0.2f)
                                                else -> MediumYellow.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = item.difficulty,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (item.difficulty) {
                                                    "Easy" -> SolvedGreen
                                                    "Hard" -> HardRed
                                                    else -> MediumYellow
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Reason
                                    if (item.reason.isNotBlank()) {
                                        Text(
                                            text = item.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Action buttons row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Platform tag
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TechCyan.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = item.platform,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TechCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (item.link.isNotBlank()) {
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                                                            context.startActivity(intent)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = "Open Link",
                                                        tint = TechCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }

                                            Button(
                                                onClick = {
                                                    onAddProblemToTopic(
                                                        ProblemEntity(
                                                            id = UUID.randomUUID().toString(),
                                                            topicId = topic.id,
                                                            title = item.title,
                                                            difficulty = item.difficulty,
                                                            platform = item.platform,
                                                            link = item.link,
                                                            solved = false
                                                        )
                                                    )
                                                    addedTitles = addedTitles + item.title
                                                    Toast.makeText(context, "Added '${item.title}' to ${topic.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                enabled = !isAdded,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isAdded) SolvedGreen else FlamePrimary,
                                                    disabledContainerColor = SolvedGreen.copy(alpha = 0.3f),
                                                    disabledContentColor = SolvedGreen
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                                modifier = Modifier.testTag("add_suggested_problem_${item.title.hashCode()}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isAdded) "Added" else "Add to Topic",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
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
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("close_daily_plan_btn")
            ) {
                Text("Close")
            }
        }
    )
}
