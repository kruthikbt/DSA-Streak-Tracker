package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.HardRed
import com.example.ui.theme.MediumYellow
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan
import java.util.UUID

data class ParsedProblemLine(
    val rawLine: String,
    val title: String,
    val difficulty: String,
    val platform: String,
    val isDuplicate: Boolean
)

fun parseProblemLines(
    rawText: String,
    defaultDifficulty: String,
    defaultPlatform: String,
    existingProblems: List<ProblemEntity>
): List<ParsedProblemLine> {
    val existingTitles = existingProblems.map { it.title.trim().lowercase() }.toSet()
    val lines = rawText.lines()

    return lines.mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank()) return@mapNotNull null

        var title = trimmed
        var difficulty = defaultDifficulty
        var platform = defaultPlatform

        // Check for TSV (from Google Sheets / Excel copy-paste)
        if (trimmed.contains("\t")) {
            val parts = trimmed.split("\t").map { it.trim() }
            if (parts.isNotEmpty()) title = parts[0]
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                val candidateDiff = parts[1].lowercase()
                when {
                    candidateDiff.contains("easy") -> difficulty = "Easy"
                    candidateDiff.contains("hard") -> difficulty = "Hard"
                    candidateDiff.contains("med") -> difficulty = "Medium"
                }
            }
            if (parts.size > 2 && parts[2].isNotEmpty()) {
                val candidatePlat = parts[2].lowercase()
                when {
                    candidatePlat.contains("leetcode") -> platform = "LeetCode"
                    candidatePlat.contains("gfg") || candidatePlat.contains("geeks") -> platform = "GFG"
                    candidatePlat.contains("codeforces") -> platform = "Codeforces"
                    else -> platform = parts[2]
                }
            }
        } else {
            // Check trailing parenthetical difficulty e.g. "Two Sum (Easy)" or "Two Sum - Medium"
            val diffMatch = Regex("""(?i)[\(\[\-\s]+(Easy|Medium|Hard|Med)[\)\]\s]*$""").find(trimmed)
            if (diffMatch != null) {
                val matchedDiff = diffMatch.groupValues[1].lowercase()
                difficulty = when {
                    matchedDiff.startsWith("e") -> "Easy"
                    matchedDiff.startsWith("h") -> "Hard"
                    else -> "Medium"
                }
                title = trimmed.substring(0, diffMatch.range.first).trim().trimEnd('-', '(', '[')
            }
        }

        // Clean leading numbering like "1. ", "2) ", etc.
        val cleanTitle = title.replace(Regex("""^\d+[\.\)\s\-]+\s*"""), "").trim()
        val finalTitle = if (cleanTitle.isNotBlank()) cleanTitle else title

        val isDuplicate = existingTitles.contains(finalTitle.lowercase())

        ParsedProblemLine(
            rawLine = trimmed,
            title = finalTitle,
            difficulty = difficulty,
            platform = platform,
            isDuplicate = isDuplicate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkImportSheet(
    topic: TopicEntity,
    existingProblems: List<ProblemEntity>,
    onImportProblems: (List<ProblemEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var rawInputText by remember { mutableStateOf("") }
    var selectedDefaultDifficulty by remember { mutableStateOf("Medium") }
    var selectedDefaultPlatform by remember { mutableStateOf("LeetCode") }
    var skipDuplicates by remember { mutableStateOf(true) }

    val parsedLines by remember(rawInputText, selectedDefaultDifficulty, selectedDefaultPlatform, existingProblems) {
        derivedStateOf {
            parseProblemLines(
                rawText = rawInputText,
                defaultDifficulty = selectedDefaultDifficulty,
                defaultPlatform = selectedDefaultPlatform,
                existingProblems = existingProblems
            )
        }
    }

    val importableLines by remember(parsedLines, skipDuplicates) {
        derivedStateOf {
            if (skipDuplicates) parsedLines.filter { !it.isDuplicate } else parsedLines
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("bulk_import_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TechCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sheet Import (Bulk Add)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Topic #${topic.order}: ${topic.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FlamePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Sheet")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action helpers row (Paste clipboard, Sample data, Clear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            rawInputText = if (rawInputText.isBlank()) clip else "$rawInputText\n$clip"
                            Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("paste_clipboard_btn")
                ) {
                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paste", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        rawInputText = when (topic.order) {
                            2 -> "Two Sum (Easy)\nBest Time to Buy and Sell Stock (Easy)\nMaximum Subarray (Medium)\nSubarray Sums Divisible by K (Medium)\nTrapping Rain Water (Hard)"
                            3 -> "Valid Anagram (Easy)\nValid Palindrome (Easy)\nLongest Common Prefix (Easy)\nGroup Anagrams (Medium)\nMinimum Window Substring (Hard)"
                            4 -> "Contains Duplicate (Easy)\nSubarray Sum Equals K (Medium)\nLongest Consecutive Sequence (Medium)\nInsert Delete GetRandom O(1) (Medium)"
                            5 -> "Valid Palindrome II (Easy)\nTwo Sum II - Input Array Is Sorted (Medium)\n3Sum (Medium)\nContainer With Most Water (Medium)\nTrapping Rain Water (Hard)"
                            6 -> "Maximum Average Subarray I (Easy)\nLongest Substring Without Repeating Characters (Medium)\nLongest Repeating Character Replacement (Medium)\nSliding Window Maximum (Hard)"
                            else -> "${topic.name} Problem 1 (Easy)\n${topic.name} Core Pattern 2 (Medium)\n${topic.name} Advanced Application (Hard)"
                        }
                        Toast.makeText(context, "Loaded example problem list", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("load_sample_sheet_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load Sample", fontSize = 12.sp)
                }

                if (rawInputText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { rawInputText = "" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multiline Input Field
            OutlinedTextField(
                value = rawInputText,
                onValueChange = { rawInputText = it },
                label = { Text("Problem Titles (one per line)") },
                placeholder = {
                    Text(
                        "Paste a list from Google Sheets or text:\n\n" +
                                "Two Sum\n" +
                                "Best Time to Buy and Sell Stock\n" +
                                "Maximum Subarray\n" +
                                "3Sum\n\n" +
                                "(Optional: includes 'Easy/Med/Hard' or tab-separated sheets data)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                },
                minLines = 5,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bulk_import_input_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Selectors: Default Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Default Level:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    val isSelected = selectedDefaultDifficulty == diff
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDefaultDifficulty = diff },
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
                        modifier = Modifier.testTag("bulk_diff_chip_$diff")
                    )
                }
            }

            // Preset Selectors: Default Platform
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Platform:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                listOf("LeetCode", "GFG", "Codeforces", "Other").forEach { plat ->
                    val isSelected = selectedDefaultPlatform == plat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDefaultPlatform = plat },
                        label = { Text(plat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                            selectedLabelColor = TechCyan
                        ),
                        modifier = Modifier.testTag("bulk_platform_chip_$plat")
                    )
                }
            }

            // Deduplication toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { skipDuplicates = !skipDuplicates }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = skipDuplicates,
                    onCheckedChange = { skipDuplicates = it },
                    colors = CheckboxDefaults.colors(checkedColor = FlamePrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Skip existing duplicates",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Avoid re-adding problems already in this topic",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parsed Preview Section
            if (parsedLines.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Preview (${importableLines.size} of ${parsedLines.size} ready)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (parsedLines.any { it.isDuplicate } && skipDuplicates) {
                                Text(
                                    text = "${parsedLines.count { it.isDuplicate }} duplicates skipped",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HardRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            parsedLines.take(10).forEachIndexed { idx, parsed ->
                                val willImport = !parsed.isDuplicate || !skipDuplicates

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (willImport) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (willImport) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else HardRed.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${idx + 1}.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = parsed.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (willImport) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (parsed.isDuplicate) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = HardRed.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (skipDuplicates) "Duplicate (Skip)" else "Duplicate",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = HardRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (parsed.difficulty) {
                                                    "Easy" -> SolvedGreen.copy(alpha = 0.2f)
                                                    "Hard" -> HardRed.copy(alpha = 0.2f)
                                                    else -> MediumYellow.copy(alpha = 0.2f)
                                                }
                                            ) {
                                                Text(
                                                    text = parsed.difficulty,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = when (parsed.difficulty) {
                                                        "Easy" -> SolvedGreen
                                                        "Hard" -> HardRed
                                                        else -> MediumYellow
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (parsedLines.size > 10) {
                                Text(
                                    text = "+ ${parsedLines.size - 10} more problems...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bottom Actions: Import and Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val newEntities = importableLines.map { parsed ->
                            val link = if (parsed.platform.equals("LeetCode", ignoreCase = true)) {
                                val slug = parsed.title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                                "https://leetcode.com/problems/$slug/"
                            } else ""

                            ProblemEntity(
                                id = UUID.randomUUID().toString(),
                                topicId = topic.id,
                                title = parsed.title,
                                difficulty = parsed.difficulty,
                                platform = parsed.platform,
                                link = link,
                                solved = false
                            )
                        }

                        if (newEntities.isNotEmpty()) {
                            onImportProblems(newEntities)
                            Toast.makeText(context, "Successfully imported ${newEntities.size} problems into ${topic.name}!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "No valid problems to import", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = importableLines.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("confirm_bulk_import_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (importableLines.isNotEmpty()) "Import ${importableLines.size} Problems" else "Import Problems",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
