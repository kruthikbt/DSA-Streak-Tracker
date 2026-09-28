package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import com.example.ui.components.TopicCard
import com.example.ui.theme.FlamePrimary
import com.example.ui.theme.SolvedGreen

@Composable
fun RoadmapScreen(
    topics: List<TopicEntity>,
    problems: List<ProblemEntity>,
    activeTopicId: Int?,
    onStatusChange: (TopicEntity, String) -> Unit,
    onToggleSubtopic: (Int, Int) -> Unit,
    onAddSubtopic: (Int, String) -> Unit,
    onDeleteSubtopic: (Int, Int) -> Unit,
    onAddProblem: (ProblemEntity) -> Unit,
    onBulkImportProblems: (List<ProblemEntity>) -> Unit = {},
    onUpdateProblem: (ProblemEntity) -> Unit,
    onToggleProblemSolved: (ProblemEntity) -> Unit,
    onDeleteProblem: (String) -> Unit,
    onRequestDailyPlan: ((TopicEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "In Progress", "Not Started", "Completed"

    val listState = rememberLazyListState()

    // Scroll to active topic if requested
    LaunchedEffect(activeTopicId) {
        if (activeTopicId != null) {
            val index = topics.indexOfFirst { it.id == activeTopicId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    // Identify the current unlock frontier
    val currentActiveTopicIndex = remember(topics) {
        val inProg = topics.indexOfFirst { it.status == TopicEntity.STATUS_IN_PROGRESS }
        if (inProg >= 0) inProg else topics.indexOfFirst { it.status == TopicEntity.STATUS_NOT_STARTED }
    }

    val problemsByTopic = remember(problems) {
        problems.groupBy { it.topicId }
    }

    val filteredTopics = remember(topics, searchQuery, selectedFilter) {
        topics.filter { topic ->
            val matchesFilter = when (selectedFilter) {
                "In Progress" -> topic.status == TopicEntity.STATUS_IN_PROGRESS
                "Not Started" -> topic.status == TopicEntity.STATUS_NOT_STARTED
                "Completed" -> topic.status == TopicEntity.STATUS_COMPLETED
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                topic.name.contains(searchQuery, ignoreCase = true) ||
                        topic.getSubtopics().any { it.title.contains(searchQuery, ignoreCase = true) }
            }
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("roadmap_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("roadmap_search_input"),
            placeholder = { Text("Search 25 topics or subtopics...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "In Progress", "Not Started", "Completed").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FlamePrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Roadmap status text
        val totalSolved = remember(problems) { problems.count { it.solved } }
        val completedTopicsCount = remember(topics) { topics.count { it.status == TopicEntity.STATUS_COMPLETED } }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredTopics.size} Topics",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "$completedTopicsCount/25 Completed • $totalSolved Solved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Topics list
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTopics, key = { it.id }) { topic ->
                val topicIndex = topics.indexOfFirst { it.id == topic.id }
                val isCurrent = topicIndex == currentActiveTopicIndex
                val isUnlocked = topicIndex <= currentActiveTopicIndex || topic.status == TopicEntity.STATUS_COMPLETED

                TopicCard(
                    topic = topic,
                    problems = problemsByTopic[topic.id] ?: emptyList(),
                    isCurrentActive = isCurrent,
                    isUnlocked = isUnlocked,
                    onStatusChange = { newStatus -> onStatusChange(topic, newStatus) },
                    onToggleSubtopic = { subtopicIndex -> onToggleSubtopic(topic.id, subtopicIndex) },
                    onAddSubtopic = { title -> onAddSubtopic(topic.id, title) },
                    onDeleteSubtopic = { subtopicIndex -> onDeleteSubtopic(topic.id, subtopicIndex) },
                    onAddProblem = onAddProblem,
                    onBulkImportProblems = onBulkImportProblems,
                    onUpdateProblem = onUpdateProblem,
                    onToggleProblemSolved = onToggleProblemSolved,
                    onDeleteProblem = onDeleteProblem,
                    onRequestDailyPlan = { onRequestDailyPlan?.invoke(topic) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
