package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.model.RevisionFilter
import com.example.data.model.RevisionProblemItem
import com.example.ui.components.RevisionCard
import com.example.ui.theme.AlgorithmViolet
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.SolvedGreen
import com.example.ui.theme.TechCyan

@Composable
fun RevisionScreen(
    revisionItems: List<RevisionProblemItem>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onMarkRevised: (String, String?) -> Unit,
    onSetDaysAgo: (String, Int) -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    onSeedSampleRevisions: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTheoryInfo by remember { mutableStateOf(false) }

    // Counts for 3, 7, and 30 days
    val count3Days = remember(revisionItems) {
        revisionItems.count { it.is3DaysAgo || it.daysAgo in 3L..6L }
    }
    val count7Days = remember(revisionItems) {
        revisionItems.count { it.is7DaysAgo || it.daysAgo in 7L..29L }
    }
    val count30Days = remember(revisionItems) {
        revisionItems.count { it.is30DaysAgo || it.daysAgo >= 30L }
    }
    val countTotalDue = remember(revisionItems) {
        revisionItems.count { it.isDue }
    }

    // Filtered items
    val displayedItems = remember(revisionItems, selectedFilter) {
        when (selectedFilter) {
            "3_DAYS" -> revisionItems.filter { it.is3DaysAgo || it.daysAgo in 3L..6L }
            "7_DAYS" -> revisionItems.filter { it.is7DaysAgo || it.daysAgo in 7L..29L }
            "30_DAYS" -> revisionItems.filter { it.is30DaysAgo || it.daysAgo >= 30L }
            "ALL_SOLVED" -> revisionItems
            else -> revisionItems.filter { it.isDue }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("revision_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Spaced Revision Overview Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revision_hero_card"),
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = FlamePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Spaced Revision",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "3 • 7 • 30 Day Recall Cycle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Total due badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (countTotalDue > 0) FlamePrimary.copy(alpha = 0.2f) else SolvedGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (countTotalDue > 0) FlamePrimary else SolvedGreen)
                        ) {
                            Text(
                                text = if (countTotalDue > 0) "$countTotalDue Due" else "All Done ✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (countTotalDue > 0) FlamePrimary else SolvedGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Resurfacing problems you solved 3, 7, and 30 days ago to overcome the Ebbinghaus forgetting curve and lock algorithmic invariants into permanent intuition.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Interactive Metric Cards (3 Days, 7 Days, 30 Days)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3 Days Ago
                        MetricIntervalCard(
                            label = "3 Days Ago",
                            sublabel = "First Recall",
                            count = count3Days,
                            accentColor = FlamePrimary,
                            isSelected = selectedFilter == "3_DAYS",
                            onClick = { onFilterSelected("3_DAYS") },
                            modifier = Modifier.weight(1f)
                        )

                        // 7 Days Ago
                        MetricIntervalCard(
                            label = "7 Days Ago",
                            sublabel = "Consolidation",
                            count = count7Days,
                            accentColor = TechCyan,
                            isSelected = selectedFilter == "7_DAYS",
                            onClick = { onFilterSelected("7_DAYS") },
                            modifier = Modifier.weight(1f)
                        )

                        // 30 Days Ago
                        MetricIntervalCard(
                            label = "30 Days Ago",
                            sublabel = "Mastery",
                            count = count30Days,
                            accentColor = AlgorithmViolet,
                            isSelected = selectedFilter == "30_DAYS",
                            onClick = { onFilterSelected("30_DAYS") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Filter Tabs
        item {
            val filterScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RevisionFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter.id) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (filter) {
                                RevisionFilter.THREE_DAYS -> FlamePrimary.copy(alpha = 0.2f)
                                RevisionFilter.SEVEN_DAYS -> TechCyan.copy(alpha = 0.2f)
                                RevisionFilter.THIRTY_DAYS -> AlgorithmViolet.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (filter) {
                                RevisionFilter.THREE_DAYS -> FlamePrimary
                                RevisionFilter.SEVEN_DAYS -> TechCyan
                                RevisionFilter.THIRTY_DAYS -> AlgorithmViolet
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        modifier = Modifier.testTag("revision_filter_chip_${filter.id}")
                    )
                }
            }
        }

        // 3. Section Title & Item Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedFilter) {
                        "3_DAYS" -> "Problems Solved 3 Days Ago ($count3Days)"
                        "7_DAYS" -> "Problems Solved 7 Days Ago ($count7Days)"
                        "30_DAYS" -> "Problems Solved 30 Days Ago ($count30Days)"
                        "ALL_SOLVED" -> "All Solved Problems (${revisionItems.size})"
                        else -> "All Spaced Revisions Due ($countTotalDue)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick Seed button if user wants to generate sample 3d/7d/30d problems
                TextButton(
                    onClick = onSeedSampleRevisions,
                    modifier = Modifier.testTag("seed_sample_revisions_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Seed 3/7/30d", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Empty State or Problem List
        if (displayedItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revision_empty_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SolvedGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SolvedGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No problems found for this interval",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "As you mark problems solved in the Roadmap, they will automatically resurface here exactly 3, 7, and 30 days later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSeedSampleRevisions,
                                colors = ButtonDefaults.buttonColors(containerColor = FlamePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load 3, 7, 30d Problems", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToRoadmap,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Go to Roadmap", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            items(displayedItems, key = { it.problem.id }) { item ->
                RevisionCard(
                    item = item,
                    onMarkRevised = onMarkRevised,
                    onSetDaysAgo = onSetDaysAgo,
                    onUpdateNotes = onUpdateNotes
                )
            }
        }

        // 5. Why 3, 7, 30 Days Theory Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTheoryInfo = !showTheoryInfo },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Why 3, 7, and 30 Days Spacing?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (showTheoryInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showTheoryInfo) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "According to Hermann Ebbinghaus's forgetting curve, humans lose 70% of new algorithmic concepts within 48 hours without active retrieval practice:\n\n" +
                                        "• Day 3 (First Recall): Interrupts rapid memory decay, forcing active retrieval of the core algorithmic invariant.\n\n" +
                                        "• Day 7 (Weekly Consolidation): Integrates the pattern with related data structures, reinforcing edge-case handling.\n\n" +
                                        "• Day 30 (Long-Term Mastery): Moves the algorithmic pattern from working memory into automatic intuitive problem recognition for high-pressure technical interviews.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricIntervalCard(
    label: String,
    sublabel: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else accentColor.copy(alpha = 0.3f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
